/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.pdf

import models.*
import models.land.LandSelectMeasurementUnit
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import services.pdf.PdfFormSupport.*
import services.pdf.SdltPdfFields.*
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using

@Singleton
class SdltReturnPdf4 @Inject()(
                                 pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  def fillPdf(land: Option[Land], fullReturn: FullReturn, firstTimeThrough: Boolean, variant: PdfVariant, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturn][${variant}][fillPdf] Filling ${variant} for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    val templateBytes: Array[Byte] = pdfTemplateLoader.load(variant.template)

    Using.Manager { use =>
      val doc    = use(Loader.loadPDF(templateBytes))
      val form   = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf4")

      if(firstTimeThrough) fillTransactionFields(writer, fullReturn)

      if (variant == Sdlt4) {
        val l = land.getOrElse(throw new IllegalArgumentException("SDLT4 requires land"))
        fillLandFields(writer, l)
        fillLeaseFields(writer, fullReturn)
      }
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray

    }.fold(
      err => {
        logger.error(s"[SdltReturn${variant}][fillPdf] Failed to fill ${variant} PDF", err)
        throw new SdltPdfFillException(s"Failed to fill $variant PDF", err)
      },
      identity
    )
  }
  
  private def fillTransactionFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainLandId: Option[String] = r.returnInfo.flatMap(_.mainLandID)
    val mainLand = r.land.flatMap(_.find(land => mainLandId.equals(land.landID)))

    r.transaction.foreach { t =>
      val postRulingApplied = t.postTransRulingApplied.map(_.equalsIgnoreCase("YES"))

      w.selectMultiple(
        Seq(
          (TRANSACTION_BUSINESS_SALE_STOCK, t.includesStock),
          (TRANSACTION_BUSINESS_SALE_GOODWILL, t.includesGoodwill),
          (TRANSACTION_BUSINESS_SALE_CHATTEL, t.includesChattel),
          (TRANSACTION_BUSINESS_SALE_OTHER, t.includesOther)
        )
      )
      w.wholeDecimal(TRANSACTION_TOTAL_CONSIDERATION, t.totalConsiderationBusiness)
      w.selectMultiple(
        Seq(
          (TRANSACTION_COMMERCIAL_USE_OFFICE, t.usedAsOffice),
          (TRANSACTION_COMMERCIAL_USE_SHOP, t.usedAsShop),
          (TRANSACTION_COMMERCIAL_USE_FACTORY, t.usedAsFactory),
          (TRANSACTION_COMMERCIAL_USE_HOTEL, t.usedAsHotel),
          (TRANSACTION_COMMERCIAL_USE_WAREHOUSE, t.usedAsWarehouse),
          (TRANSACTION_COMMERCIAL_USE_INDUSTRIAL, t.usedAsIndustrial),
          (TRANSACTION_COMMERCIAL_USE_OTHER, t.usedAsOther)
        )
      )
      w.yesNo(
        TRANSACTION_POST_TRANSACTION_RULING_YES,
        TRANSACTION_POST_TRANSACTION_RULING_NO,
        postRulingApplied
      )
      if(postRulingApplied.contains(true)) {
        checkRulingFollowed(
        w,
        TRANSACTION_RULING_FOLLOWED_YES,
        TRANSACTION_RULING_FOLLOWED_NO,
        TRANSACTION_RULING_FOLLOWED_RULING_NOT_RECEIVED,
        t.postTransRulingFollowed
        )
      }
      w.yesNo(
        TRANSACTION_DEPENDS_FUTURE_EVENT_YES,
        TRANSACTION_DEPENDS_FUTURE_EVENT_NO,
        t.isDependantOnFutureEvent.map(_.equalsIgnoreCase("YES"))
      )
      w.yesNo(
        TRANSACTION_PAY_DEFERRED_YES,
        TRANSACTION_PAY_DEFERRED_NO,
        t.agreedToDeferPayment.map(_.equalsIgnoreCase("YES"))
      )
    }

    mainLand.foreach { land =>
      w.text(LAND_MINERAL_RIGHTS, land.mineralRights)
    }

    r.companyDetails.foreach { c =>
      w.fillSelectedCodes(
        w,
        Seq(
          c.companyTypeBuilder -> "01",
          c.companyTypeSoletrader -> "02",
          c.companyTypeIndividual -> "03",
          c.companyTypePartnership -> "04",
          c.companyTypeLocalauth -> "05",
          c.companyTypeCentgov -> "06",
          c.companyTypePubliccorp -> "07",
          c.companyTypeProperty -> "08",
          c.companyTypeBank -> "09",
          c.companyTypeBuildsoc -> "10",
          c.companyTypeInsurance -> "11",
          c.companyTypePensionfund -> "12",
          c.companyTypeOtherfinancial -> "13",
          c.companyTypeOthercompany -> "14",
          c.companyTypeOthercharity -> "15"
        ),
        Seq(
          PURCHASER_DESCRIPTION_1,
          PURCHASER_DESCRIPTION_2,
          PURCHASER_DESCRIPTION_3,
          PURCHASER_DESCRIPTION_4
        )
      )
    }
  }

  private def fillLandFields(w: PdfFieldWriter, l: Land): Unit = {
    w.text(LAND_TYPE_PROPERTY, l.propertyType)
    w.postcode(LAND_POSTCODE_1, LAND_POSTCODE_2, l.postcode)
    w.text(LAND_HOUSE_NUMBER,  l.houseNumber)
    w.text(LAND_ADDRESS_LINE1, l.address1)
    w.text(LAND_ADDRESS_LINE2, l.address2)
    w.text(LAND_ADDRESS_LINE3, l.address3)
    w.text(LAND_ADDRESS_LINE4, l.address4)
    w.text(LAND_LOCAL_AUTHORITY_NUMBER, l.localAuthorityNumber)
    w.text(LAND_TITLE_NUMBER, l.titleNumber)
    w.text(LAND_NLPG_UPRN, l.NLPGUPRN)
    l.areaUnit match {
      case Some(LandSelectMeasurementUnit.Hectares.toString) =>
        w.check(LAND_AREA_TYPE_HECTARES)
        w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
      case Some(LandSelectMeasurementUnit.Sqms.toString) =>
        w.check(LAND_AREA_TYPE_SQUARE_METRES)
        w.uncheck(LAND_AREA_TYPE_HECTARES)
      case _ =>
        w.uncheck(LAND_AREA_TYPE_HECTARES)
        w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
    }
    w.text(LAND_AREA, l.landArea)
    w.yesNo(
      LAND_PLAN_ATTACHED_YES,
      LAND_PLAN_ATTACHED_NO,
      l.willSendPlanByPost.map(_.equalsIgnoreCase("YES"))
    )
    w.text(LAND_ESTATE_OR_INTEREST_TRANSFERRED, l.interestCreatedTransferred)
  }

  private def fillLeaseFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val tc = r.taxCalculation
    r.lease.foreach { l =>
      w.text(LEASE_LEASE_TYPE, l.leaseType)
      w.dateStr(
        LEASE_CONTRACT_START_DATE_DAY,
        LEASE_CONTRACT_START_DATE_MONTH,
        LEASE_CONTRACT_START_DATE_YEAR,
        l.contractStartDate
      )
      w.dateStr(
        LEASE_CONTRACT_END_DATE_DAY,
        LEASE_CONTRACT_END_DATE_MONTH,
        LEASE_CONTRACT_END_DATE_YEAR,
        l.contractEndDate
      )
      w.text(LEASE_RENT_FREE_PERIOD, l.rentFreePeriod)
      w.wholeDecimal(LEASE_STARTING_RENT, l.startingRent)
      w.dateStr(
        LEASE_STARTING_RENT_END_DATE_DAY,
        LEASE_STARTING_RENT_END_DATE_MONTH,
        LEASE_STARTING_RENT_END_DATE_YEAR,
        l.startingRentEndDate
      )
      w.yesNo(
        LEASE_STARTING_RENT_LATER_KNOWN_YES,
        LEASE_STARTING_RENT_LATER_KNOWN_NO,
        l.laterRentKnown.map(_.equalsIgnoreCase("YES"))
      )
      w.wholeDecimal(LEASE_VAT_AMOUNT, l.VATAmount)
      w.wholeDecimal(LEASE_PREMIUM_PAID, l.totalPremiumPayable)
      w.wholeDecimal(LEASE_NET_PRESENT_VALUE, l.netPresentValue)
    }
    
    w.wholeDecimal(LEASE_TOTAL_PREMIUM_TAX, tc.flatMap(_.taxDuePremium))
    w.wholeDecimal(LEASE_TOTAL_NPV_TAX, tc.flatMap(_.taxDueNPV))

    r.lease.foreach { l =>
      w.text(LEASE_TERMS_SURRENDERED, l.termsSurrendered)
      w.selectOne(
        l.breakClauseType,
        Map(
          "landlord" -> LEASE_BREAK_CLAUSE_LANDLORD,
          "tenant"   -> LEASE_BREAK_CLAUSE_TENANT,
          "either"   -> LEASE_BREAK_CLAUSE_EITHER
        )
      )
      w.dateStr(
        LEASE_BREAK_CLAUSE_DAY,
        LEASE_BREAK_CLAUSE_MONTH,
        LEASE_BREAK_CLAUSE_YEAR,
        l.breakClauseDate
      )
      w.selectMultiple(
        Seq(
          (LEASE_OPTION_TO_RENEW, l.optionToRenew),
          (LEASE_MARKET_RENT, l.marketRent),
          (LEASE_TURNOVER_RENT, l.turnoverRent),
          (LEASE_UNASCERTAINABLE_RENT, l.unasertainableRent),
          (LEASE_CONTINGENT_RESERVED_RENT, l.leaseContReservedRent)
        )
      )
      w.text(LEASE_REVIEW_FREQUENCY, l.rentReviewFrequency)
      w.dateStr(
        LEASE_FIRST_REVIEW_DATE_DAY,
        LEASE_FIRST_REVIEW_DATE_MONTH,
        LEASE_FIRST_REVIEW_DATE_YEAR,
        l.firstReviewDate
      )
      w.selectOne(
        l.reviewClauseType,
        Map(
          "Open market" -> LEASE_REVIEW_CLAUSE_OPEN,
          "RPI"         -> LEASE_REVIEW_CLAUSE_RPI,
          "Other"       -> LEASE_REVIEW_CLAUSE_OTHER
        )
      )
      w.dateStr(
        LEASE_RENT_CHARGE_DATE_DAY,
        LEASE_RENT_CHARGE_DATE_MONTH,
        LEASE_RENT_CHARGE_DATE_YEAR,
        l.rentChargeDate
      )
      w.text(LEASE_SERVICE_CHARGE, l.serviceCharge)
      w.selectOne(
        l.serviceChargeFrequency,
        Map(
          "Monthly"   -> LEASE_SERVICE_CHARGE_FREQUENCY_MONTHLY,
          "Quarterly" -> LEASE_SERVICE_CHARGE_FREQUENCY_QUARTERLY,
          "Anually"   -> LEASE_SERVICE_CHARGE_ANNUALLY,
          "Other"     -> LEASE_SERVICE_CHARGE_OTHER
        )
      )
      w.fillSelectedCodes(
        w,
        Seq(
          l.considToLndlrdDebt        -> "01",
          l.considToLndlrdBuild       -> "02",
          l.considToLndlrdEmploy      -> "03",
          l.considToLndlrdOther       -> "04",
          l.considToLndlrdSharedQTD   -> "05",
          l.considToLndlrdSharedUNQTD -> "06",
          l.considToLndlrdLand        -> "07",
          l.considToLndlrdServices    -> "08",
          l.considToLndlrdContin      -> "09"
        ),
        Seq(
          LEASE_TO_LANDLORD_1,
          LEASE_TO_LANDLORD_2,
          LEASE_TO_LANDLORD_3,
          LEASE_TO_LANDLORD_4
        )
      )
      w.fillSelectedCodes(
        w,
        Seq(
          l.considToTenantBuild -> "02",
          l.considToTenantEmploy -> "03",
          l.considToTenantOther -> "04",
          l.considToTenantSharesQTD -> "05",
          l.considToTenantSharesUNQTD -> "06",
          l.considToTenantLand -> "07",
          l.considToTenantServices -> "08",
          l.considToTenantContin -> "09"
        ),
        Seq(
          LEASE_TO_TENANT_1,
          LEASE_TO_TENANT_2,
          LEASE_TO_TENANT_3,
          LEASE_TO_TENANT_4
        )
      )
    }
  }

  private def checkRulingFollowed(w: PdfFieldWriter, yesField: String, noField: String, rulingNotReceived: String, value: Option[String]): Unit =
    value match {
      case Some(v) if v.equalsIgnoreCase("YES") =>
        w.check(yesField)
      case Some(v) if v.equalsIgnoreCase("NO") =>
        w.check(noField)
      case Some(v) if v.equalsIgnoreCase("RulingNotReceived") =>
        w.check(rulingNotReceived)
      case Some(_) | None =>
        w.uncheck(yesField)
        w.uncheck(noField)
        w.uncheck(rulingNotReceived)
    }

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}
