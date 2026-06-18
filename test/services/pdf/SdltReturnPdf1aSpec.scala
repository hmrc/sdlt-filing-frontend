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

import base.SpecBase
import models.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDTextField, PDCheckBox}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf1aSpec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN",
    "land_typeProperty",
    "transaction_description",
    "land_estateOrInterestTransfered",
    "transaction_effectiveDate_day", "transaction_effectiveDate_month", "transaction_effectiveDate_year",
    "transaction_restrictionsAffectingDetails_1", "transaction_restrictionsAffectingDetails_2",
    "transaction_contractDate_day", "transaction_contractDate_month", "transaction_contractDate_year",
    "transaction_landExchangedPostcode_1", "transaction_landExchangedPostcode_2",
    "transaction_landExchangedHouseNumber",
    "transaction_landExchangedAddressLine1", "transaction_landExchangedAddressLine2",
    "transaction_landExchangedAddressLine3", "transaction_landExchangedAddressLine4",
    "calculation_claimingReliefReason", "calculation_claimingReliefSchemeNumber",
    "calculation_claimingReliefAmount",
    "calculation_totalConsideration",
    "calculation_totalConsideration_1", "calculation_totalConsideration_2",
    "calculation_totalConsideration_3", "calculation_totalConsideration_4",
    "calculation_totalConsiderationVatAmount",
    "calculation_linkedTransactionTotalConsideration",
    "calculation_taxDueUserEntered",
    "calculation_amountPaid",
    "lease_leaseType",
    "lease_contractStartDate_day", "lease_contractStartDate_month", "lease_contractStartDate_year",
    "lease_contractEndDate_day", "lease_contractEndDate_month", "lease_contractEndDate_year",
    "lease_rentFreePeriod", "lease_startingRent",
    "lease_startingRentEndDate_day", "lease_startingRentEndDate_month", "lease_startingRentEndDate_year",
    "lease_vatAmount", "lease_premiumPaid", "lease_netPresentValue", "lease_totalPremiumTax", "lease_totalNpvTax",
    "land_numberProperties",
    "land_postcode_1", "land_postcode_2",
    "land_houseNumber",
    "land_addressLine1", "land_addressLine2", "land_addressLine3", "land_addressLine4",
    "land_localAuthorityNumber", "land_titleNumber", "land_nlpgUprn",
    "land_landArea",
    "vendor_numberVendors",
    "vendor_title", "vendor_name",
    "vendor_forename1", "vendor_forename2",
    "vendor_postcode_1", "vendor_postcode_2",
    "vendor_houseNumber",
    "vendor_addressLine1", "vendor_addressLine2", "vendor_addressLine3", "vendor_addressLine4",
    "vendor_agentName"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "transaction_restrictionsAffecting_yes", "transaction_restrictionsAffecting_no",
    "transaction_landExchanged_yes", "transaction_landExchanged_no",
    "transaction_pursuantToOption_yes", "transaction_pursuantToOption_no",
    "calculation_claimingRelief_yes", "calculation_claimingRelief_no",
    "calculation_linkedTransaction_yes", "calculation_linkedTransaction_no",
    "calculation_amountPaidIncludesPenalties_yes", "calculation_amountPaidIncludesPenalties_no",
    "lease_startingRentLaterKnown_yes", "lease_startingRentLaterKnown_no",
    "land_certificateForEach_yes", "land_certificateForEach_no",
    "land_landAreaType_Hectares", "land_landAreaType_Square_Metres",
    "land_planAttached_yes", "land_planAttached_no"
  )

  private def buildTemplatePdf(): Array[Byte] = {
    val doc   = new PDDocument()
    val page  = new org.apache.pdfbox.pdmodel.PDPage()
    doc.addPage(page)

    val acroForm = new PDAcroForm(doc)
    doc.getDocumentCatalog.setAcroForm(acroForm)

    allTextFields.foreach { name =>
      val field = new PDTextField(acroForm)
      field.setPartialName(name)
      acroForm.getFields.add(field)
    }

    allCheckboxFields.foreach { name =>
      val cb = new PDCheckBox(acroForm)
      cb.setPartialName(name)
      acroForm.getFields.add(cb)
    }

    val out = new ByteArrayOutputStream()
    doc.save(out)
    doc.close()
    out.toByteArray
  }

  private def fill(r: FullReturn) = buildFiller().fillPdf(r, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf1a = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT1a.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf1a(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId           = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private def withTransaction(t: Transaction): FullReturn =
    baseReturn.copy(transaction = Some(t))

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private def withLand(lands: Land*): FullReturn =
    baseReturn.copy(land = Some(lands.toSeq))

  private def withVendor(vendors: Vendor*): FullReturn =
    baseReturn.copy(vendor = Some(vendors.toSeq))

  private def withLease(l: Lease): FullReturn =
    baseReturn.copy(lease = Some(l))
  
  "SdltReturnPdf1a" - {

    "fillPdf" - {

      "must return a non-empty byte array" in {
        val result = fill(baseReturn)
        result must not be empty
      }

      "must return bytes starting with the %PDF header" in {
        val result = fill(baseReturn)
        new String(result.take(4)) mustBe "%PDF"
      }

      "must throw SdltPdfFillException when template has no AcroForm" in {
        val loader = mock[PdfTemplateLoader]
        val bare = {
          val d = new PDDocument()
          d.addPage(new org.apache.pdfbox.pdmodel.PDPage())
          val out = new ByteArrayOutputStream()
          d.save(out); d.close()
          out.toByteArray
        }
        when(loader.load("SDLT1a.pdf")).thenReturn(bare)
        val filler = new SdltReturnPdf1a(loader)
        intercept[SdltPdfFillException] {
          filler.fillPdf(baseReturn)
        }
      }

      "must write UTRN from submission" in {
        val r      = withSubmission("UTR-9999")
        val result = fill(r)
        readField(result, "UTRN") mustBe Some("UTR-9999")
      }

      "must write property type from first land" in {
        val r      = withLand(Land(propertyType = Some("01")))
        val result = fill(r)
        readField(result, "land_typeProperty") mustBe Some("01")
      }

      "must write transaction description" in {
        val r      = withTransaction(Transaction(transactionDescription = Some("F")))
        val result = fill(r)
        readField(result, "transaction_description") mustBe Some("F")
      }

      "must write interest created or transferred as 2 character string" in {
        val r = withLand(Land(interestCreatedTransferred = Some("FGS")))
        val result = fill(r)
        readField(result, "land_estateOrInterestTransfered") mustBe Some("FG")
      }

      "must split effective date into day / month / year fields" in {
        val r      = withTransaction(Transaction(effectiveDate = Some("15/06/2024")))
        val result = fill(r)
        readField(result, "transaction_effectiveDate_day")   mustBe Some("15")
        readField(result, "transaction_effectiveDate_month") mustBe Some("06")
        readField(result, "transaction_effectiveDate_year")  mustBe Some("2024")
      }

      "must leave date fields blank when effective date is absent" in {
        val result = fill(baseReturn)
        readField(result, "transaction_effectiveDate_day")   mustBe Some("")
        readField(result, "transaction_effectiveDate_month") mustBe Some("")
        readField(result, "transaction_effectiveDate_year")  mustBe Some("")
      }

      "must fill yes checkbox when transaction restrictions is yes" in {
        val r = withTransaction(Transaction(restrictionsAffectInterest = Some("YES")))
        val result = fill(r)
        readField(result, "transaction_restrictionsAffecting_yes") mustBe Some("Yes")
        readField(result, "transaction_restrictionsAffecting_no") mustBe Some("Off")
      }

      "must split contract date into day / month / year fields" in {
        val r      = withTransaction(Transaction(contractDate = Some("01/04/2024")))
        val result = fill(r)
        readField(result, "transaction_contractDate_day")   mustBe Some("01")
        readField(result, "transaction_contractDate_month") mustBe Some("04")
        readField(result, "transaction_contractDate_year")  mustBe Some("2024")
      }

      "must split restriction details across two lines when over 39 characters" in {
        val longDetail = "This is a restriction detail that is definitely over 39 characters long"
        val r          = withTransaction(Transaction(restrictionDetails = Some(longDetail)))
        val result = fill(r)
        val line1      = readField(result, "transaction_restrictionsAffectingDetails_1").getOrElse("")
        val line2      = readField(result, "transaction_restrictionsAffectingDetails_2").getOrElse("")
        line1 must not be empty
        line2 must not be empty
        (line1 + " " + line2).trim mustBe longDetail
      }

      "must write land exchanged address fields" in {
        val r = withTransaction(Transaction(
          exchangedLandPostcode    = Some("SW1A 2AA"),
          exchangedLandHouseNumber = Some("10"),
          exchangedLandAddress1    = Some("Downing Street")
        ))
        val result = fill(r)
        readField(result, "transaction_landExchangedPostcode_1")  mustBe Some("SW1A")
        readField(result, "transaction_landExchangedPostcode_2")  mustBe Some("2AA")
        readField(result, "transaction_landExchangedHouseNumber") mustBe Some("10")
        readField(result, "transaction_landExchangedAddressLine1") mustBe Some("Downing Street")
      }

      "must split a standard postcode with space into two fields" in {
        val r      = withLand(Land(postcode = Some("ST16 3AB")))
        val result = fill(r)
        readField(result, "land_postcode_1") mustBe Some("ST16")
        readField(result, "land_postcode_2") mustBe Some("3AB")
      }

      "must put a postcode with no space entirely in field 1" in {
        val r      = withLand(Land(postcode = Some("ST163AB")))
        val result = fill(r)
        readField(result, "land_postcode_1") mustBe Some("ST163AB")
        readField(result, "land_postcode_2") mustBe Some("")
      }

      "must write total consideration as whole number string" in {
        val r      = withTransaction(Transaction(totalConsideration = Some("250000.00")))
        val result = fill(r)
        readField(result, "calculation_totalConsideration") mustBe Some("250000")
      }

      "must write forms of consideration as 2 digit codes" in {
        val r      = withTransaction(Transaction(
          considerationCash = Some("YES"),
          considerationDebt = Some("YES"),
          considerationLand = Some("YES"),
          considerationContingent = Some("YES")
        ))
        val result = fill(r)
        readField(result, "calculation_totalConsideration_1") mustBe Some("30")
        readField(result, "calculation_totalConsideration_2") mustBe Some("31")
        readField(result, "calculation_totalConsideration_3") mustBe Some("37")
        readField(result, "calculation_totalConsideration_4") mustBe Some("39")
      }

      "must not write forms of consideration when not present or NO" in {
        val r = withTransaction(Transaction(
          considerationCash = Some("NO")
        ))
        val result = fill(r)
        readField(result, "calculation_totalConsideration_1") mustBe Some("")
        readField(result, "calculation_totalConsideration_2") mustBe Some("")
        readField(result, "calculation_totalConsideration_3") mustBe Some("")
        readField(result, "calculation_totalConsideration_4") mustBe Some("")
      }

      "must write lease section taxCalculation fields" in {
        val r = baseReturn.copy(taxCalculation = Some(TaxCalculation(
          taxDue = Some("12500.00"),
          amountPaid = Some("1300.00"),
          taxDuePremium = Some("1400.00"),
          taxDueNPV = Some("1500.00")
        )))
        val result = fill(r)
        readField(result, "calculation_taxDueUserEntered") mustBe Some("12500")
        readField(result, "calculation_amountPaid") mustBe Some("1300")
        readField(result, "lease_totalPremiumTax") mustBe Some("1400")
        readField(result, "lease_totalNpvTax") mustBe Some("1500")
      }

      "must write amount paid from taxCalculation" in {
        val r = baseReturn.copy(taxCalculation = Some(TaxCalculation(amountPaid = Some("12500"))))
        val result = fill(r)
        readField(result, "calculation_amountPaid") mustBe Some("12500")
      }

      "must write lease type when lease is present" in {
        val r      = withLease(Lease(leaseType = Some("R")))
        val result = fill(r)
        readField(result, "lease_leaseType") mustBe Some("R")
      }

      "must split lease start date into day / month / year" in {
        val r      = withLease(Lease(contractStartDate = Some("01/01/2024")))
        val result = fill(r)
        readField(result, "lease_contractStartDate_day")   mustBe Some("01")
        readField(result, "lease_contractStartDate_month") mustBe Some("01")
        readField(result, "lease_contractStartDate_year")  mustBe Some("2024")
      }

      "must split lease end date into day / month / year" in {
        val r = withLease(Lease(contractEndDate = Some("01/01/2026")))
        val result = fill(r)
        readField(result, "lease_contractEndDate_day")   mustBe Some("01")
        readField(result, "lease_contractEndDate_month") mustBe Some("01")
        readField(result, "lease_contractEndDate_year")  mustBe Some("2026")
      }

      "must not write lease fields when no lease is present" in {
        val result = fill(baseReturn)
        readField(result, "lease_leaseType") mustBe Some("")
      }

      "must write land count as number of properties" in {
        val r      = withLand(Land(landID = Some("L1")), Land(landID = Some("L2")))
        val result = fill(r)
        readField(result, "land_numberProperties") mustBe Some("2")
      }

      "must write 0 for number of properties when no land is present" in {
        val result = fill(baseReturn)
        readField(result, "land_numberProperties") mustBe Some("0")
      }

      "must write primary land address fields from first land" in {
        val r = withLand(Land(
          houseNumber = Some("42"),
          address1    = Some("Main Street"),
          address2    = Some("Stafford"),
          postcode    = Some("ST16 3AB")
        ))
        val result = fill(r)
        readField(result, "land_houseNumber")  mustBe Some("42")
        readField(result, "land_addressLine1") mustBe Some("Main Street")
        readField(result, "land_addressLine2") mustBe Some("Stafford")
      }

      "must write local authority number and title number" in {
        val r = withLand(Land(
          localAuthorityNumber = Some("5678"),
          titleNumber          = Some("MX123456")
        ))
        val result = fill(r)
        readField(result, "land_localAuthorityNumber") mustBe Some("5678")
        readField(result, "land_titleNumber")          mustBe Some("MX123456")
      }

      "must write area of land in hectares" in {
        val r = withLand(Land(
          areaUnit = Some("Hectares"),
          landArea = Some("100.123")
        ))
        val result = fill(r)
        readField(result, "land_landAreaType_Hectares") mustBe Some("Yes")
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100.123")
      }

      "must write area of land in square metres" in {
        val r = withLand(Land(
          areaUnit = Some("SquareMetres"),
          landArea = Some("100.000")
        ))
        val result = fill(r)
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Yes")
        readField(result, "land_landAreaType_Hectares") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100.000")
      }

      "must write vendor count as number of vendors" in {
        val r      = withVendor(Vendor(vendorID = Some("V1")), Vendor(vendorID = Some("V2")))
        val result = fill(r)
        readField(result, "vendor_numberVendors") mustBe Some("2")
      }

      "must write 0 for number of vendors when none are present" in {
        val result = fill(baseReturn)
        readField(result, "vendor_numberVendors") mustBe Some("0")
      }

      "must write first vendor name and forenames" in {
        val r = withVendor(Vendor(
          name      = Some("Smith"),
          forename1 = Some("John"),
          forename2 = Some("Paul")
        ))
        val result = fill(r)
        readField(result, "vendor_name")     mustBe Some("Smith")
        readField(result, "vendor_forename1") mustBe Some("John")
        readField(result, "vendor_forename2") mustBe Some("Paul")
      }

      "must write first vendor address and postcode" in {
        val r = withVendor(Vendor(
          houseNumber = Some("5"),
          address1    = Some("High Street"),
          postcode    = Some("M1 2AB")
        ))
        val result = fill(r)
        readField(result, "vendor_houseNumber")  mustBe Some("5")
        readField(result, "vendor_addressLine1") mustBe Some("High Street")
        readField(result, "vendor_postcode_1")   mustBe Some("M1")
        readField(result, "vendor_postcode_2")   mustBe Some("2AB")
      }

      "must write agent name from vendor returnAgent" in {
        val r = baseReturn.copy(returnAgent = Some(Seq(ReturnAgent(name = Some("Jones & Co"), agentType = Some("VENDOR")))))
        val result = fill(r)
        readField(result, "vendor_agentName") mustBe Some("Jones & Co")
      }

      "must leave vendor agent name blank when no return agent of type vendor is present" in {
        val r = baseReturn.copy(returnAgent = Some(Seq(ReturnAgent(name = Some("Jones & Co"), agentType = Some("PURCHASER")))))
        val result = fill(r)
        readField(result, "vendor_agentName") mustBe Some("")
      }

      "must leave vendor agent name blank when no return agent is present" in {
        val result = fill(baseReturn)
        readField(result, "vendor_agentName") mustBe Some("")
      }

      "must handle a completely populated return without throwing" in {
        val r = FullReturn(
          stornId           = "STORN999",
          returnResourceRef = "RRF-999",
          returnInfo        = Some(ReturnInfo(returnID = Some("RET999"), landCertForEachProp = Some("YES"))),
          submission        = Some(Submission(UTRN = Some("UTR-1234"))),
          transaction       = Some(Transaction(
            transactionDescription  = Some("Freehold"),
            effectiveDate           = Some("25/12/2024"),
            contractDate            = Some("01/11/2024"),
            totalConsideration      = Some("500000"),
            isLinked                = Some("NO"),
            restrictionsAffectInterest = Some("NO"),
            isLandExchanged         = Some("NO"),
            isPursuantToPreviousOption = Some("NO"),
            claimingRelief          = Some("NO")
          )),
          taxCalculation = Some(TaxCalculation(taxDue = Some("15000"), amountPaid = Some("15000"), includesPenalty = Some("NO"))),
          land           = Some(Seq(Land(
            propertyType         = Some("01"),
            houseNumber          = Some("1"),
            address1             = Some("Test Street"),
            postcode             = Some("ST1 1AA"),
            localAuthorityNumber = Some("1234"),
            titleNumber          = Some("AB123"),
            areaUnit             = Some("Hectares"),
            landArea             = Some("0.5"),
            willSendPlanByPost   = Some("NO")
          ))),
          vendor         = Some(Seq(Vendor(name = Some("Vendor One"), postcode = Some("ST2 2BB")))),
          returnAgent    = Some(Seq(ReturnAgent(name = Some("Agent Ltd"))))
        )
        noException mustBe thrownBy(buildFiller().fillPdf(r))
      }
    }
  }
}