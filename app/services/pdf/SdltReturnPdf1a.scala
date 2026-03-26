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
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.util.{Try, Using}

@Singleton
class SdltReturnPdf1a @Inject()(
                                 pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  import SdltPdfFields.*

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT1a.pdf")

  def fillPdf(fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf1a][fillPdf] Filling SDLT1 for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc    = use(Loader.loadPDF(templateBytes))
      val form   = getAcroForm(doc)
      val writer = new FieldWriter(form)

      fillPage1(writer, fullReturn)
      fillPage2(writer, fullReturn)
      fillPage3(writer, fullReturn)

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

  // ---------------------------------------------------------------------------
  // Page 1 — About the Transaction (Boxes 1-8)
  // Field source: fullReturn.transaction
  // ---------------------------------------------------------------------------

  private def fillPage1(w: FieldWriter, r: FullReturn): Unit = {
    val t = r.transaction.getOrElse(Transaction())

    // UTRN from submission
    w.text(UTRN, r.submission.flatMap(_.UTRN).orNull)

    // Box 1: Type of property — first land's propertyType
    w.text(LAND_TYPE_PROPERTY, r.land.flatMap(_.headOption).flatMap(_.propertyType).orNull)

    // Box 2: Description of transaction
    w.text(TRANSACTION_DESCRIPTION, t.transactionDescription.orNull)

    // Box 3: Interest/estate transferred — first land
    w.text(LAND_ESTATE_OR_INTEREST_TRANSFERRED,
      r.land.flatMap(_.headOption).flatMap(_.interestCreatedTransferred).orNull)

    // Box 4: Effective date (split day/month/year)
    w.dateStr(
      TRANSACTION_EFFECTIVE_DATE_DAY,
      TRANSACTION_EFFECTIVE_DATE_MONTH,
      TRANSACTION_EFFECTIVE_DATE_YEAR,
      t.effectiveDate.orNull
    )

    // Box 5: Restrictions/covenants
    w.yesNo(
      TRANSACTION_RESTRICTIONS_YES,
      TRANSACTION_RESTRICTIONS_NO,
      t.restrictionsAffectInterest.contains("YES")
    )
    val (details1, details2) = splitLines(t.restrictionDetails.getOrElse(""), 60)
    w.text(TRANSACTION_RESTRICTIONS_DETAILS_1, details1)
    w.text(TRANSACTION_RESTRICTIONS_DETAILS_2, details2)

    // Box 6: Date of contract (split day/month/year)
    w.dateStr(
      TRANSACTION_CONTRACT_DATE_DAY,
      TRANSACTION_CONTRACT_DATE_MONTH,
      TRANSACTION_CONTRACT_DATE_YEAR,
      t.contractDate.orNull
    )

    // Box 7: Land exchanged
    w.yesNo(
      TRANSACTION_LAND_EXCHANGED_YES,
      TRANSACTION_LAND_EXCHANGED_NO,
      t.isLandExchanged.contains("YES")
    )
    if (t.isLandExchanged.contains("YES")) {
      val (pc1, pc2) = splitPostcode(t.exchangedLandPostcode.getOrElse(""))
      w.text(TRANSACTION_LAND_EXCHANGED_POSTCODE_1,   pc1)
      w.text(TRANSACTION_LAND_EXCHANGED_POSTCODE_2,   pc2)
      w.text(TRANSACTION_LAND_EXCHANGED_HOUSE_NUMBER, t.exchangedLandHouseNumber.orNull)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_1,    t.exchangedLandAddress1.orNull)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_2,    t.exchangedLandAddress2.orNull)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_3,    t.exchangedLandAddress3.orNull)
      w.text(TRANSACTION_LAND_EXCHANGED_ADDRESS_4,    t.exchangedLandAddress4.orNull)
    }

    // Box 8: Pursuant to previous option agreement
    w.yesNo(
      TRANSACTION_PURSUANT_TO_OPTION_YES,
      TRANSACTION_PURSUANT_TO_OPTION_NO,
      t.isPursuantToPreviousOption.contains("YES")
    )
  }

  // ---------------------------------------------------------------------------
  // Page 2 — Tax Calculation (Boxes 9-15) + New Leases (Boxes 16-25)
  // Field sources: fullReturn.transaction, fullReturn.taxCalculation, fullReturn.lease
  // ---------------------------------------------------------------------------

  private def fillPage2(w: FieldWriter, r: FullReturn): Unit = {
    val t  = r.transaction.getOrElse(Transaction())
    val tc = r.taxCalculation.getOrElse(TaxCalculation())

    // Relief claimed
    val claimingRelief = t.claimingRelief.contains("YES")
    w.yesNo(CALCULATION_CLAIMING_RELIEF_YES, CALCULATION_CLAIMING_RELIEF_NO, claimingRelief)
    if (claimingRelief) {
      w.text(CALCULATION_CLAIMING_RELIEF_REASON,        t.reliefReason.orNull)
      w.text(CALCULATION_CLAIMING_RELIEF_SCHEME_NUMBER, t.reliefSchemeNumber.orNull)
      w.bigDecimal(CALCULATION_CLAIMING_RELIEF_AMOUNT,  t.reliefAmount)
    }

    // Box 9: Total consideration — also fill the split digit-group fields
    w.bigDecimal(CALCULATION_TOTAL_CONSIDERATION, t.totalConsideration)
    fillSplitConsideration(w, t.totalConsideration)

    // Box 10: VAT in consideration
    w.bigDecimal(CALCULATION_TOTAL_CONSIDERATION_VAT, t.considerationVAT)

    // Box 12: Linked transactions
    w.yesNo(
      CALCULATION_LINKED_TRANSACTION_YES,
      CALCULATION_LINKED_TRANSACTION_NO,
      t.isLinked.contains("YES")
    )

    // Box 13: Total linked consideration
    w.bigDecimal(CALCULATION_LINKED_TRANSACTION_TOTAL, t.totalConsiderationLinked)

    // Box 14: Tax due
    w.text(CALCULATION_TAX_DUE, tc.taxDue.orNull)

    // Box 15: Amount paid
    w.text(CALCULATION_AMOUNT_PAID, tc.amountPaid.orNull)

    // Does amount include penalties?
    w.yesNo(
      CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_YES,
      CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_NO,
      tc.includesPenalty.contains("YES")
    )

    // Boxes 16-25: Lease fields (only if a lease is present)
    r.lease.foreach(fillLeaseFields(w, _))
  }

  private def fillLeaseFields(w: FieldWriter, l: Lease): Unit = {
    // Box 16: Lease type
    w.text(LEASE_LEASE_TYPE, l.leaseType.orNull)

    // Box 17: Contract start date
    w.dateStr(
      LEASE_CONTRACT_START_DATE_DAY,
      LEASE_CONTRACT_START_DATE_MONTH,
      LEASE_CONTRACT_START_DATE_YEAR,
      l.contractStartDate.orNull
    )

    // Box 18: Contract end date
    w.dateStr(
      LEASE_CONTRACT_END_DATE_DAY,
      LEASE_CONTRACT_END_DATE_MONTH,
      LEASE_CONTRACT_END_DATE_YEAR,
      l.contractEndDate.orNull
    )

    // Box 19: Rent-free period (months)
    w.text(LEASE_RENT_FREE_PERIOD, l.rentFreePeriod.orNull)

    // Box 20: Annual starting rent
    w.text(LEASE_STARTING_RENT, l.startingRent.orNull)

    // Box 21: End date for starting rent
    w.dateStr(
      LEASE_STARTING_RENT_END_DATE_DAY,
      LEASE_STARTING_RENT_END_DATE_MONTH,
      LEASE_STARTING_RENT_END_DATE_YEAR,
      l.startingRentEndDate.orNull
    )

    // Box 22: Later rent known
    w.yesNo(
      LEASE_STARTING_RENT_LATER_KNOWN_YES,
      LEASE_STARTING_RENT_LATER_KNOWN_NO,
      l.laterRentKnown.contains("YES")
    )

    // Box 23: VAT amount
    w.text(LEASE_VAT_AMOUNT, l.VATAmount.orNull)

    // Box 24: Total premium payable
    w.text(LEASE_PREMIUM_PAID, l.totalPremiumPayable.orNull)

    // Box 25: Net present value
    w.text(LEASE_NET_PRESENT_VALUE, l.netPresentValue.orNull)
  }

  // ---------------------------------------------------------------------------
  // Page 3 — About the Land (26-33) + About the Vendor (34-39)
  // Field sources: fullReturn.land.head, fullReturn.vendor.head, fullReturn.returnAgent
  // ---------------------------------------------------------------------------

  private def fillPage3(w: FieldWriter, r: FullReturn): Unit = {
    val lands   = r.land.getOrElse(Seq.empty)
    val vendors = r.vendor.getOrElse(Seq.empty)
    val agents  = r.returnAgent.getOrElse(Seq.empty)

    // Box 26: Number of properties
    w.text(LAND_NUMBER_PROPERTIES, lands.size.toString)

    // Box 27: Certificate for each property (only if > 1 land)
    if (lands.size > 1) {
      w.yesNo(
        LAND_CERTIFICATE_FOR_EACH_YES,
        LAND_CERTIFICATE_FOR_EACH_NO,
        r.returnInfo.flatMap(_.landCertForEachProp).contains("YES")
      )
    }

    // Box 28: Primary land address (first land in list)
    lands.headOption.foreach { land =>
      val (pc1, pc2) = splitPostcode(land.postcode.getOrElse(""))
      w.text(LAND_POSTCODE_1,    pc1)
      w.text(LAND_POSTCODE_2,    pc2)
      w.text(LAND_HOUSE_NUMBER,  land.houseNumber.orNull)
      w.text(LAND_ADDRESS_LINE1, land.address1.orNull)
      w.text(LAND_ADDRESS_LINE2, land.address2.orNull)
      w.text(LAND_ADDRESS_LINE3, land.address3.orNull)
      w.text(LAND_ADDRESS_LINE4, land.address4.orNull)

      // Box 29: Address continued on SDLT3 (true if more than 1 land)
      w.yesNo(
        LAND_ADDRESS_ON_SDLT3_YES,
        LAND_ADDRESS_ON_SDLT3_NO,
        lands.size > 1
      )

      // Box 30: Local authority number
      w.text(LAND_LOCAL_AUTHORITY_NUMBER, land.localAuthorityNumber.orNull)

      // Box 31: Title number
      w.text(LAND_TITLE_NUMBER, land.titleNumber.orNull)

      // Box 32: NLPG UPRN
      w.text(LAND_NLPG_UPRN, land.NLPGUPRN.orNull)

      // Box 33: Land area / plan attached
      land.areaUnit match {
        case Some("Hectares")      => w.check(LAND_AREA_TYPE_HECTARES);      w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
        case Some("Square metres") => w.uncheck(LAND_AREA_TYPE_HECTARES);    w.check(LAND_AREA_TYPE_SQUARE_METRES)
        case _                     => w.uncheck(LAND_AREA_TYPE_HECTARES);    w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
      }
      w.text(LAND_AREA, land.landArea.orNull)
      w.yesNo(
        LAND_PLAN_ATTACHED_YES,
        LAND_PLAN_ATTACHED_NO,
        land.willSendPlanByPost.contains("YES")
      )
    }

    // Box 34: Number of vendors
    w.text(VENDOR_NUMBER_VENDORS, vendors.size.toString)

    // Boxes 35-38: First vendor
    vendors.headOption.foreach { vendor =>
      w.text(VENDOR_TITLE,    vendor.title.orNull)
      w.text(VENDOR_NAME,     vendor.name.orNull)

      // Split forenames — forename1 and forename2 are already separate fields
      w.text(VENDOR_FORENAME_1, vendor.forename1.orNull)
      w.text(VENDOR_FORENAME_2, vendor.forename2.orNull)

      val (pc1, pc2) = splitPostcode(vendor.postcode.getOrElse(""))
      w.text(VENDOR_POSTCODE_1,    pc1)
      w.text(VENDOR_POSTCODE_2,    pc2)
      w.text(VENDOR_HOUSE_NUMBER,  vendor.houseNumber.orNull)
      w.text(VENDOR_ADDRESS_LINE1, vendor.address1.orNull)
      w.text(VENDOR_ADDRESS_LINE2, vendor.address2.orNull)
      w.text(VENDOR_ADDRESS_LINE3, vendor.address3.orNull)
      w.text(VENDOR_ADDRESS_LINE4, vendor.address4.orNull)
    }

    // Box 39: Agent name — from the first returnAgent
    w.text(VENDOR_AGENT_NAME, agents.headOption.flatMap(_.name).orNull)
  }

  // ---------------------------------------------------------------------------
  // Split consideration across the four digit-group fields (_1 leftmost, _4 rightmost)
  // Each box holds ~3 digits. Left-pad the full pound amount to 12 chars.
  // ---------------------------------------------------------------------------

  private def fillSplitConsideration(w: FieldWriter, amount: Option[BigDecimal]): Unit =
    amount.foreach { bd =>
      val padded = bd.bigDecimal.toString.reverse.padTo(12, '0').reverse
      w.text(CALCULATION_TOTAL_CONSIDERATION_1, padded.substring(0, 3))
      w.text(CALCULATION_TOTAL_CONSIDERATION_2, padded.substring(3, 6))
      w.text(CALCULATION_TOTAL_CONSIDERATION_3, padded.substring(6, 9))
      w.text(CALCULATION_TOTAL_CONSIDERATION_4, padded.substring(9, 12))
    }

  // ---------------------------------------------------------------------------
  // Utilities
  // ---------------------------------------------------------------------------

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))

  /** Split "SW1A 2AA" → ("SW1A", "2AA"). Single-part postcodes go entirely into field 1. */
  private def splitPostcode(postcode: String): (String, String) =
    Option(postcode).map(_.trim).filter(_.nonEmpty) match {
      case None => ("", "")
      case Some(pc) =>
        val parts = pc.split(" ", 2)
        if (parts.length == 2) (parts(0), parts(1)) else (pc, "")
    }

  /** Split a long string at a word boundary near maxLen for the two details fields. */
  private def splitLines(s: String, maxLen: Int): (String, String) =
    if (s.length <= maxLen) (s, "")
    else {
      val cut = s.lastIndexOf(' ', maxLen)
      if (cut > 0) (s.substring(0, cut), s.substring(cut + 1))
      else (s.substring(0, maxLen), s.substring(maxLen))
    }

  // ---------------------------------------------------------------------------
  // Inner FieldWriter — defensive wrapper around PDAcroForm
  // ---------------------------------------------------------------------------

  private class FieldWriter(form: PDAcroForm) {

    /** Set a text field. Null/blank values silently clear the field. */
    def text(fieldName: String, value: String): Unit = {
      val safe = Option(value).map(_.trim).getOrElse("")
      Try(form.getField(fieldName)).toOption match {
        case Some(f: PDTextField) =>
          Try(f.setValue(safe)).failed.foreach { e =>
            logger.warn(s"[SdltReturnPdf1a][FieldWriter] Could not set '$fieldName': ${e.getMessage}")
          }
        case Some(_) =>
          logger.warn(s"[SdltReturnPdf1a][FieldWriter] '$fieldName' is not a text field")
        case None =>
          logger.debug(s"[SdltReturnPdf1a][FieldWriter] '$fieldName' not found in template — skipping")
      }
    }

    /** Set a Yes/No checkbox pair from a boolean. */
    def yesNo(yesField: String, noField: String, isYes: Boolean): Unit =
      if (isYes) { check(yesField); uncheck(noField) }
      else       { uncheck(yesField); check(noField) }

    def check(fieldName: String): Unit   = setCheckbox(fieldName, checked = true)
    def uncheck(fieldName: String): Unit = setCheckbox(fieldName, checked = false)

    private def setCheckbox(fieldName: String, checked: Boolean): Unit =
      Try(form.getField(fieldName)).toOption match {
        case Some(cb: PDCheckBox) =>
          Try(if (checked) cb.check() else cb.unCheck()).failed.foreach { e =>
            logger.warn(s"[SdltReturnPdf1a][FieldWriter] Could not set checkbox '$fieldName': ${e.getMessage}")
          }
        case Some(_) =>
          logger.warn(s"[SdltReturnPdf1a][FieldWriter] '$fieldName' is not a checkbox")
        case None =>
          logger.debug(s"[SdltReturnPdf1a][FieldWriter] '$fieldName' not found in template — skipping")
      }

    /**
     * Set a BigDecimal money field as a plain string.
     * The PDF template renders the £ symbol and decimal separator as static content.
     */
    def bigDecimal(fieldName: String, amount: Option[BigDecimal]): Unit =
      text(fieldName, amount.map(_.setScale(2, BigDecimal.RoundingMode.DOWN).toString).orNull)

    /**
     * Parse a stored date string ("dd/MM/yyyy") and split into three fields.
     * Tolerant of null — leaves fields blank.
     */
    def dateStr(dayField: String, monthField: String, yearField: String, stored: String): Unit =
      Option(stored).map(_.trim).filter(_.nonEmpty) match {
        case None =>
          text(dayField, ""); text(monthField, ""); text(yearField, "")
        case Some(d) =>
          Try {
            val fmt  = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDate.parse(d, fmt)
            text(dayField,   f"${date.getDayOfMonth}%02d")
            text(monthField, f"${date.getMonthValue}%02d")
            text(yearField,   date.getYear.toString)
          }.failed.foreach { e =>
            logger.warn(s"[SdltReturnPdf1a][FieldWriter] Could not parse date '$d': ${e.getMessage}")
            text(dayField, d); text(monthField, ""); text(yearField, "")
          }
      }
  }
}

class SdltPdfFillException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)