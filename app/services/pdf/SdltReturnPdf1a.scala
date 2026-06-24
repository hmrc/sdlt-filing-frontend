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
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using
import SdltPdfFields.*
import PdfFormSupport.*
import models.land.LandSelectMeasurementUnit
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding

@Singleton
class SdltReturnPdf1a @Inject()(
                                 pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT1a.pdf")

  def fillPdf(fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf1a][fillPdf] Filling SDLT1 for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc    = use(Loader.loadPDF(templateBytes))
      val form   = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf1a")

      fillTransactionFields(writer, fullReturn)
      fillTaxCalculationFields(writer, fullReturn)
      fillLeaseFields(writer, fullReturn)
      fillLandFields(writer, fullReturn)
      fillVendorFields(writer, fullReturn)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray

    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf1a][fillPdf] Failed to fill SDLT1 PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT1 PDF", err)
      },
      identity
    )
  }
  
  private def fillTransactionFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainLandId: Option[String] = r.returnInfo.flatMap(_.mainLandID)
    val mainLand = r.land.flatMap(_.find(land => mainLandId.equals(land.landID)))

    mainLand.foreach { land =>
      w.text(LAND_TYPE_PROPERTY, land.propertyType)
      w.text(LAND_ESTATE_OR_INTEREST_TRANSFERRED, land.interestCreatedTransferred.map(_.substring(0, 2)))
    }
    r.transaction.foreach { t =>
      w.text(TRANSACTION_DESCRIPTION, t.transactionDescription)
      w.dateStr(
        TRANSACTION_EFFECTIVE_DATE_DAY,
        TRANSACTION_EFFECTIVE_DATE_MONTH,
        TRANSACTION_EFFECTIVE_DATE_YEAR,
        t.effectiveDate
      )
      w.yesNo(
        TRANSACTION_RESTRICTIONS_YES,
        TRANSACTION_RESTRICTIONS_NO,
        t.restrictionsAffectInterest.map(_.equalsIgnoreCase("YES"))
      )
      val (details1, details2) = splitLines(t.restrictionDetails, 39)
      w.text(TRANSACTION_RESTRICTIONS_DETAILS_1, details1)
      w.text(TRANSACTION_RESTRICTIONS_DETAILS_2, details2)
      w.dateStr(
        TRANSACTION_CONTRACT_DATE_DAY,
        TRANSACTION_CONTRACT_DATE_MONTH,
        TRANSACTION_CONTRACT_DATE_YEAR,
        t.contractDate
      )
      w.yesNo(
        TRANSACTION_LAND_EXCHANGED_YES,
        TRANSACTION_LAND_EXCHANGED_NO,
        t.isLandExchanged.map(_.equalsIgnoreCase("YES"))
      )
      w.postcode(TRANSACTION_LAND_EXCHANGED_POSTCODE_1, TRANSACTION_LAND_EXCHANGED_POSTCODE_2, t.exchangedLandPostcode)
      w.text(TRANSACTION_LAND_EXCHANGED_HOUSE_NUMBER, t.exchangedLandHouseNumber)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_1, t.exchangedLandAddress1)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_2, t.exchangedLandAddress2)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_3, t.exchangedLandAddress3)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_4, t.exchangedLandAddress4)
      w.yesNo(
        TRANSACTION_PURSUANT_TO_OPTION_YES,
        TRANSACTION_PURSUANT_TO_OPTION_NO,
        t.isPursuantToPreviousOption.map(_.equalsIgnoreCase("YES"))
      )
    }
  }

  private def fillTaxCalculationFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    r.transaction.foreach { t =>
      val claimingRelief = t.claimingRelief.map(_.equalsIgnoreCase("YES"))
      w.yesNo(CALCULATION_CLAIMING_RELIEF_YES, CALCULATION_CLAIMING_RELIEF_NO, claimingRelief)
      w.text(CALCULATION_CLAIMING_RELIEF_REASON, t.reliefReason)
      w.text(CALCULATION_CLAIMING_RELIEF_SCHEME_NUMBER, t.reliefSchemeNumber)
      w.wholeDecimal(CALCULATION_CLAIMING_RELIEF_AMOUNT, t.reliefAmount)
      w.wholeDecimal(CALCULATION_TOTAL_CONSIDERATION, t.totalConsideration)
      w.wholeDecimal(CALCULATION_TOTAL_CONSIDERATION_VAT, t.considerationVAT)
      fillFormsOfConsideration(w,
        Seq(
          t.considerationCash -> 30,
          t.considerationDebt -> 31,
          t.considerationBuild -> 32,
          t.considerationEmploy -> 33,
          t.considerationOther -> 34,
          t.considerationSharesQTD -> 35,
          t.considerationSharesUNQTD -> 36,
          t.considerationLand -> 37,
          t.considerationServices -> 38,
          t.considerationContingent -> 39
        )
      )
      w.yesNo(
        CALCULATION_LINKED_TRANSACTION_YES,
        CALCULATION_LINKED_TRANSACTION_NO,
        t.isLinked.map(_.equalsIgnoreCase("YES"))
      )
      w.wholeDecimal(CALCULATION_LINKED_TRANSACTION_TOTAL, t.totalConsiderationLinked)
    }
    r.taxCalculation.foreach { tc =>
      w.wholeDecimal(CALCULATION_TAX_DUE, tc.taxDue)
      w.wholeDecimal(CALCULATION_AMOUNT_PAID, tc.amountPaid)
      w.yesNo(
        CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_YES,
        CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_NO,
        tc.includesPenalty.map(_.equalsIgnoreCase("YES"))
      )
    }
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
  }

  private def fillLandFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainLandId: Option[String] = r.returnInfo.flatMap(_.mainLandID)
    val mainLand = r.land.flatMap(_.find(land => mainLandId.equals(land.landID)))

    w.text(LAND_NUMBER_PROPERTIES, Some(r.land.getOrElse(Seq.empty).size.toString))
    w.yesNo(
      LAND_CERTIFICATE_FOR_EACH_YES,
      LAND_CERTIFICATE_FOR_EACH_NO,
      r.returnInfo.flatMap(_.landCertForEachProp).map(_.equalsIgnoreCase("YES"))
    )
    mainLand.foreach { land =>
      w.postcode(LAND_POSTCODE_1, LAND_POSTCODE_2, land.postcode)
      w.text(LAND_HOUSE_NUMBER,  land.houseNumber)
      w.text(LAND_ADDRESS_LINE1, land.address1)
      w.text(LAND_ADDRESS_LINE2, land.address2)
      w.text(LAND_ADDRESS_LINE3, land.address3)
      w.text(LAND_ADDRESS_LINE4, land.address4)
      w.check(LAND_ADDRESS_ON_SDLT3_NO)
      w.text(LAND_LOCAL_AUTHORITY_NUMBER, land.localAuthorityNumber)
      w.text(LAND_TITLE_NUMBER, land.titleNumber)
      w.text(LAND_NLPG_UPRN, land.NLPGUPRN)
      land.areaUnit match {
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
      w.text(LAND_AREA, land.landArea)
      w.yesNo(
        LAND_PLAN_ATTACHED_YES,
        LAND_PLAN_ATTACHED_NO,
        land.willSendPlanByPost.map(_.equalsIgnoreCase("YES"))
      )
    }
  }

  private def fillVendorFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainVendorId: Option[String] = r.returnInfo.flatMap(_.mainVendorID)
    val mainVendor = r.vendor.flatMap(_.find(vendor => mainVendorId.equals(vendor.vendorID)))
    val vendorAgent = r.returnAgent.flatMap(_.find(_.agentType.contains(AgentType.Vendor.toString)))

    w.text(VENDOR_NUMBER_VENDORS, Some(r.vendor.getOrElse(Seq.empty).size.toString))
    mainVendor.foreach { vendor =>
      w.text(VENDOR_TITLE,    vendor.title)
      w.text(VENDOR_NAME,     vendor.name)
      w.text(VENDOR_FORENAME_1, vendor.forename1)
      w.text(VENDOR_FORENAME_2, vendor.forename2)

      w.postcode(VENDOR_POSTCODE_1, VENDOR_POSTCODE_2, vendor.postcode)
      w.text(VENDOR_HOUSE_NUMBER,  vendor.houseNumber)
      w.text(VENDOR_ADDRESS_LINE1, vendor.address1)
      w.text(VENDOR_ADDRESS_LINE2, vendor.address2)
      w.text(VENDOR_ADDRESS_LINE3, vendor.address3)
      w.text(VENDOR_ADDRESS_LINE4, vendor.address4)
    }
    w.text(VENDOR_AGENT_NAME, vendorAgent.flatMap(_.name))
  }

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def fillFormsOfConsideration(w: PdfFieldWriter, formsOfConsideration: Seq[(Option[String], Int)]): Unit = {
    var codesIdx: Int = 0
    val codes = Array.fill(4)("")
    formsOfConsideration.foreach { case (formOfConsideration, code) =>
      if (formOfConsideration.exists(_.equalsIgnoreCase("YES"))) {
        codes(codesIdx) = code.toString
        codesIdx += 1
      }
    }
    w.text(CALCULATION_FORM_OF_CONSIDERATION_1, Some(codes(0)))
    w.text(CALCULATION_FORM_OF_CONSIDERATION_2, Some(codes(1)))
    w.text(CALCULATION_FORM_OF_CONSIDERATION_3, Some(codes(2)))
    w.text(CALCULATION_FORM_OF_CONSIDERATION_4, Some(codes(3)))
  }

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}

class SdltPdfFillException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)