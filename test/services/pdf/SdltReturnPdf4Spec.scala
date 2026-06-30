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
import org.apache.pdfbox.cos.{COSDictionary, COSName}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.annotation.{PDAppearanceDictionary, PDAppearanceStream}
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf4Spec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN",
    "transaction_totalConsideration",
    "land_mineralRights",
    "purchaser_description_1", "purchaser_description_2", "purchaser_description_3", "purchaser_description_4",
    "land_typeProperty",
    "land_localAuthorityNumber",
    "land_titleNumber",
    "land_nlpgUprn",
    "land_houseNumber",
    "land_addressLine1", "land_addressLine2", "land_addressLine3", "land_addressLine4",
    "land_postcode_1", "land_postcode_2",
    "land_landArea",
    "land_estateOrInterestTransfered",
    "lease_leaseType",
    "lease_contractStartDate_day", "lease_contractStartDate_month", "lease_contractStartDate_year",
    "lease_contractEndDate_day", "lease_contractEndDate_month", "lease_contractEndDate_year",
    "lease_rentFreePeriod", "lease_startingRent",
    "lease_startingRentEndDate_day", "lease_startingRentEndDate_month", "lease_startingRentEndDate_year",
    "lease_vatAmount",
    "lease_premiumPaid",
    "lease_netPresentValue",
    "lease_totalPremiumTax",
    "lease_totalNpvTax",
    "lease_termsSurrendered",
    "lease_breakClauseDate_day", "lease_breakClauseDate_month", "lease_breakClauseDate_year",
    "lease_reviewFrequency",
    "lease_firstReviewDate_day", "lease_firstReviewDate_month", "lease_firstReviewDate_year",
    "lease_rentChargeDate_day", "lease_rentChargeDate_month", "lease_rentChargeDate_year",
    "lease_serviceCharge",
    "lease_toLandlord_1", "lease_toLandlord_2", "lease_toLandlord_3", "lease_toLandlord_4",
    "lease_toTenant_1", "lease_toTenant_2", "lease_toTenant_3", "lease_toTenant_4"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "transaction_businessSaleStock", "transaction_businessSaleGoodwill", "transaction_businessSaleChattel", "transaction_businessSaleOther",
    "transaction_commercialUseOffice", "transaction_commercialUseShop", "transaction_commercialUseFactory", "transaction_commercialUseHotel", "transaction_commercialUseWarehouse", "transaction_commercialUseIndustrial", "transaction_commercialUseOther",
    "transaction_postTransactionRuling_yes", "transaction_postTransactionRuling_no",
    "transaction_rulingFollowed_yes", "transaction_rulingFollowed_no", "transaction_rulingFollowed_ruling",
    "transaction_dependsFutureEvent_yes", "transaction_dependsFutureEvent_no",
    "transaction_payDeferred_yes", "transaction_payDeferred_no",
    "land_landAreaType_Hectares", "land_landAreaType_Square_Metres",
    "land_planAttached_yes", "land_planAttached_no",
    "lease_startingRentLaterKnown_yes", "lease_startingRentLaterKnown_no",
    "lease_breakClause_landlord", "lease_breakClause_tenant", "lease_breakClause_either",
    "lease_optionToRenew", "lease_marketRent", "lease_turnoverRent", "lease_unascertainableRent", "lease_contingentReservedRent",
    "lease_reviewClause_open", "lease_reviewClause_rpi", "lease_reviewClause_other",
    "lease_serviceChargeFrequency_monthly", "lease_serviceChargeFrequency_quarterly", "lease_serviceChargeFrequency_annually", "lease_serviceChargeFrequency_other"
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

      val widget = cb.getWidgets.get(0)
      widget.setRectangle(new PDRectangle(0, 0, 10, 10))

      def appearanceStream(): PDAppearanceStream = {
        val s = new PDAppearanceStream(doc)
        s.setBBox(new PDRectangle(0, 0, 10, 10))
        s
      }

      val normalStates = new COSDictionary()
      normalStates.setItem(COSName.YES, appearanceStream())
      normalStates.setItem(COSName.Off, appearanceStream())

      val apDict = new PDAppearanceDictionary()
      apDict.getCOSObject.setItem(COSName.N, normalStates)

      widget.setAppearance(apDict)
      widget.getCOSObject.setItem(COSName.AS, COSName.Off)

      acroForm.getFields.add(cb)
    }

    val out = new ByteArrayOutputStream()
    doc.save(out)
    doc.close()
    out.toByteArray
  }

  private def fill(l: Land, r: FullReturn, firstTimeThrough: Boolean) = buildFiller().fillPdf(l, r, firstTimeThrough, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf4 = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT4New.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf4(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId           = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private val baseLand: Land = Land(
    landID = Some("LND001"),
    propertyType = Some("02")
  )

  private def withTransaction(t: Transaction): FullReturn =
    baseReturn.copy(transaction = Some(t))

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private def withCompanyDetails(cD: CompanyDetails): FullReturn =
    baseReturn.copy(companyDetails = Some(cD))

  private def withLease(l: Lease): FullReturn =
    baseReturn.copy(lease = Some(l))
  
  "SdltReturnPdf4" - {

    "fillPdf" - {

      "must return a non-empty byte array" in {
        val result = fill(baseLand, baseReturn, true)
        result must not be empty
      }

      "must return bytes starting with the %PDF header" in {
        val result = fill(baseLand, baseReturn, true)
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
        when(loader.load("SDLT4New.pdf")).thenReturn(bare)
        val filler = new SdltReturnPdf4(loader)
        intercept[SdltPdfFillException] {
          filler.fillPdf(baseLand, baseReturn, true)
        }
      }

      "must write UTRN from submission" in {
        val r      = withSubmission("UTR-9999")
        val result = fill(baseLand, r, true)
        readField(result, "UTRN") mustBe Some("UTR-9999")
      }

      // Transaction

      "must fill only the selected business sale checkboxes" in {
        val r      = withTransaction(Transaction(
          includesStock    = Some("YES"),
          includesGoodwill = Some("NO"),
          includesChattel  = Some("yes"),
          includesOther    = None
        ))
        val result = fill(baseLand, r, true)

        readField(result, "transaction_businessSaleStock")    mustBe Some("Yes")
        readField(result, "transaction_businessSaleGoodwill") mustBe Some("Off")
        readField(result, "transaction_businessSaleChattel")  mustBe Some("Yes")
        readField(result, "transaction_businessSaleOther")    mustBe Some("Off")
      }

      "must write total consideration" in {
        val r = withTransaction(Transaction(totalConsideration = Some("5000")))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_totalConsideration") mustBe Some("5000")
      }

      "must fill only the selected commercial use checkboxes" in {
        val r = withTransaction(Transaction(
          usedAsOffice     = Some("Yes"),
          usedAsShop       = Some("YES"),
          usedAsFactory    = None,
          usedAsHotel      = Some("NO"),
          usedAsWarehouse  = Some("yes"),
          usedAsIndustrial = Some("no"),
          usedAsOther      = Some("No")
        ))
        val result = fill(baseLand, r, true)

        readField(result, "transaction_commercialUseOffice")     mustBe Some("Yes")
        readField(result, "transaction_commercialUseShop")       mustBe Some("Yes")
        readField(result, "transaction_commercialUseFactory")    mustBe Some("Off")
        readField(result, "transaction_commercialUseHotel")      mustBe Some("Off")
        readField(result, "transaction_commercialUseWarehouse")  mustBe Some("Yes")
        readField(result, "transaction_commercialUseIndustrial") mustBe Some("Off")
        readField(result, "transaction_commercialUseOther")      mustBe Some("Off")
      }

      "must fill yes checkbox when postTransRulingApplied is yes" in {
        val r = withTransaction(Transaction(postTransRulingApplied = Some("YES")))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_postTransactionRuling_yes") mustBe Some("Yes")
        readField(result, "transaction_postTransactionRuling_no")  mustBe Some("Off")
      }

      "must fill 'yes' checkbox when postTransRulingFollowed is yes" in {
        val r = withTransaction(Transaction(
          postTransRulingApplied = Some("YES"),
          postTransRulingFollowed = Some("YES")
        ))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_rulingFollowed_yes") mustBe Some("Yes")
        readField(result, "transaction_rulingFollowed_no") mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_ruling") mustBe Some("Off")
      }

      "must fill 'no' checkbox when postTransRulingFollowed is no" in {
        val r = withTransaction(Transaction(
          postTransRulingApplied  = Some("YES"),
          postTransRulingFollowed = Some("NO")
        ))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_rulingFollowed_yes")    mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_no")     mustBe Some("Yes")
        readField(result, "transaction_rulingFollowed_ruling") mustBe Some("Off")
      }

      "must fill ruling checkbox when postTransRulingFollowed is RulingNotRecieved" in {
        val r = withTransaction(Transaction(
          postTransRulingApplied  = Some("YES"),
          postTransRulingFollowed = Some("RulingNotReceived")
        ))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_rulingFollowed_yes")    mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_no")     mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_ruling") mustBe Some("Yes")
      }

      "must not fill postTransRulingFollowed checkbox when postTransRulingApplied is no" in {
        val r = withTransaction(Transaction(
          postTransRulingApplied  = Some("NO"),
          postTransRulingFollowed = Some("RulingNotReceived")
        ))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_rulingFollowed_yes")    mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_no")     mustBe Some("Off")
        readField(result, "transaction_rulingFollowed_ruling") mustBe Some("Off")
      }

      "must fill yes checkbox when isDependantOnFutureEvent is yes" in {
        val r = withTransaction(Transaction(isDependantOnFutureEvent = Some("YES")))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_dependsFutureEvent_yes") mustBe Some("Yes")
        readField(result, "transaction_dependsFutureEvent_no")  mustBe Some("Off")
      }

      "must fill no checkbox when agreedToDeferPayment is no" in {
        val r = withTransaction(Transaction(agreedToDeferPayment = Some("NO")))
        val result = fill(baseLand, r, true)
        readField(result, "transaction_payDeferred_yes") mustBe Some("Off")
        readField(result, "transaction_payDeferred_no")  mustBe Some("Yes")
      }

      "must write mineral rights" in {
        val r = baseReturn.copy(
          land = Some(Seq(
            Land(landID = Some("LND001"), mineralRights = Some("Yes")),
            Land(landID = Some("LND002"))
          )),
          returnInfo = Some(ReturnInfo(mainLandID = Some("LND001")))
        )
        val result = fill(baseLand, r, true)
        readField(result, "land_mineralRights") mustBe Some("Yes")
      }

      "must write purchaser descriptions as 2 digit codes" in {
        val r = withCompanyDetails(CompanyDetails(
          companyTypeIndividual = Some("yes"),
          companyTypeLocalauth = Some("YES"),
          companyTypeBank = Some("Yes"),
          companyTypePensionfund = Some("Yes")
        ))
        val result = fill(baseLand, r, true)

        readField(result, "purchaser_description_1") mustBe Some("03")
        readField(result, "purchaser_description_2") mustBe Some("05")
        readField(result, "purchaser_description_3") mustBe Some("09")
        readField(result, "purchaser_description_4") mustBe Some("12")
      }

      "must not fill any transaction section fields when firstTimeThrough is false" in {
        val r = withTransaction(Transaction(
          includesStock    = Some("YES"),
          totalConsideration = Some("5000")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "transaction_businessSaleStock") mustBe Some("Off")
        readField(result, "transaction_totalConsideration") mustBe Some("")
      }

      // Land

      "must write text land fields" in {
        val l = baseLand.copy(
          propertyType = Some("02"),
          localAuthorityNumber = Some("3015"),
          titleNumber = Some("TGL123456"),
          NLPGUPRN = Some("100023336956"),
          houseNumber = Some("1"),
          address1 = Some("Test Lane"),
          address2 = Some("Westminster"),
          address3 = Some("London"),
          address4 = Some("United Kingdom"),
          postcode = Some("SW1A 2AA"),
          interestCreatedTransferred = Some("OT")
        )
        val result = fill(l, baseReturn, false)

        readField(result, "land_typeProperty") mustBe Some("02")
        readField(result, "land_localAuthorityNumber") mustBe Some("3015")
        readField(result, "land_nlpgUprn") mustBe Some("100023336956")
        readField(result, "land_houseNumber") mustBe Some("1")
        readField(result, "land_addressLine1") mustBe Some("Test Lane")
        readField(result, "land_addressLine2") mustBe Some("Westminster")
        readField(result, "land_addressLine3") mustBe Some("London")
        readField(result, "land_addressLine4") mustBe Some("United Kingdom")
        readField(result, "land_postcode_1") mustBe Some("SW1A")
        readField(result, "land_postcode_2") mustBe Some("2AA")
        readField(result, "land_estateOrInterestTransfered") mustBe Some("OT")
      }

      "must write area of land in hectares" in {
        val l = baseLand.copy(
          areaUnit = Some("Hectares"),
          landArea = Some("100.123")
        )
        val result = fill(l, baseReturn, false)

        readField(result, "land_landAreaType_Hectares") mustBe Some("Yes")
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100.123")
      }

      "must write area of land in square metres" in {
        val l = baseLand.copy(
          areaUnit = Some("SquareMetres"),
          landArea = Some("100.000")
        )
        val result = fill(l, baseReturn, false)
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Yes")
        readField(result, "land_landAreaType_Hectares") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100.000")
      }

      "must fill yes checkbox when planAttached is yes" in {
        val l = baseLand.copy(willSendPlanByPost = Some("Yes"))
        val result = fill(l, baseReturn, false)
        readField(result, "land_planAttached_yes") mustBe Some("Yes")
        readField(result, "land_planAttached_no") mustBe Some("Off")
      }

      // Lease

      "must write text lease fields" in {
        val r = withLease(Lease(
          leaseType = Some("R"),
          contractStartDate = Some("01/04/2024"),
          contractEndDate = Some("01/04/2026"),
          rentFreePeriod = Some("5"),
          startingRent = Some("2000"),
          startingRentEndDate = Some("01/04/2025"),
          VATAmount = Some("20"),
          totalPremiumPayable = Some("500000"),
          netPresentValue = Some("1897")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_leaseType") mustBe Some("R")
        readField(result, "lease_contractStartDate_day") mustBe Some("01")
        readField(result, "lease_contractStartDate_month") mustBe Some("04")
        readField(result, "lease_contractStartDate_year") mustBe Some("2024")
        readField(result, "lease_contractEndDate_day") mustBe Some("01")
        readField(result, "lease_contractEndDate_month") mustBe Some("04")
        readField(result, "lease_contractEndDate_year") mustBe Some("2026")
        readField(result, "lease_rentFreePeriod") mustBe Some("5")
        readField(result, "lease_startingRent") mustBe Some("2000")
        readField(result, "lease_startingRentEndDate_day") mustBe Some("01")
        readField(result, "lease_startingRentEndDate_month") mustBe Some("04")
        readField(result, "lease_startingRentEndDate_year") mustBe Some("2025")
        readField(result, "lease_vatAmount") mustBe Some("20")
        readField(result, "lease_premiumPaid") mustBe Some("500000")
        readField(result, "lease_netPresentValue") mustBe Some("1897")
      }

      "must fill no checkbox when startingRentLaterKnown is no" in {
        val r = withLease(Lease(laterRentKnown = Some("No")))
        val result = fill(baseLand, r, false)
        readField(result, "lease_startingRentLaterKnown_yes") mustBe Some("Off")
        readField(result, "lease_startingRentLaterKnown_no") mustBe Some("Yes")
      }

      "must write taxDuePremium and taxDueNPV from tax calculation" in {
        val r = baseReturn.copy(
          taxCalculation = Some(TaxCalculation(
            taxDuePremium = Some("1400.00"),
            taxDueNPV = Some("1500.00")
          ))
        )
        val result = fill(baseLand, r, false)

        readField(result, "lease_totalPremiumTax") mustBe Some("1400")
        readField(result, "lease_totalNpvTax") mustBe Some("1500")
      }

      "must write additional text lease fields" in {
        val r = withLease(Lease(
          termsSurrendered = Some("Surrender & Regrant"),
          breakClauseDate = Some("01/04/2022"),
          rentReviewFrequency = Some("12"),
          firstReviewDate = Some("01/10/2021"),
          rentChargeDate = Some("01/04/2021"),
          serviceCharge = Some("1000")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_termsSurrendered") mustBe Some("Surrender & Regrant")
        readField(result, "lease_breakClauseDate_day") mustBe Some("01")
        readField(result, "lease_breakClauseDate_month") mustBe Some("04")
        readField(result, "lease_breakClauseDate_year") mustBe Some("2022")
        readField(result, "lease_reviewFrequency") mustBe Some("12")
        readField(result, "lease_firstReviewDate_day") mustBe Some("01")
        readField(result, "lease_firstReviewDate_month") mustBe Some("10")
        readField(result, "lease_rentChargeDate_day") mustBe Some("01")
        readField(result, "lease_rentChargeDate_month") mustBe Some("04")
        readField(result, "lease_rentChargeDate_year") mustBe Some("2021")
        readField(result, "lease_serviceCharge") mustBe Some("1000")
      }

      "must fill only one matching break clause type checkbox" in {
        val r = withLease(Lease(
          breakClauseType = Some("landlord")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_breakClause_landlord") mustBe Some("Yes")
        readField(result, "lease_breakClause_tenant") mustBe Some("Off")
        readField(result, "lease_breakClause_either") mustBe Some("Off")
      }

      "must fill the selected relate to the lease checkboxes" in {
        val r = withLease(Lease(
          optionToRenew = Some("YES"),
          marketRent = Some("No"),
          turnoverRent = Some("yes"),
          unasertainableRent = None,
          leaseContReservedRent = Some("NO")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_optionToRenew") mustBe Some("Yes")
        readField(result, "lease_marketRent") mustBe Some("Off")
        readField(result, "lease_turnoverRent") mustBe Some("Yes")
        readField(result, "lease_unascertainableRent") mustBe Some("Off")
        readField(result, "lease_contingentReservedRent") mustBe Some("Off")
      }

      "must fill only one matching reviewClauseType checkbox" in {
        val r = withLease(Lease(
          reviewClauseType = Some("RPI")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_reviewClause_open") mustBe Some("Off")
        readField(result, "lease_reviewClause_rpi") mustBe Some("Yes")
        readField(result, "lease_reviewClause_other") mustBe Some("Off")
      }

      "must fill only one matching serviceChargeFrequency checkbox" in {
        val r = withLease(Lease(
          serviceChargeFrequency = Some("Anually")
        ))
        val result = fill(baseLand, r, false)

        readField(result, "lease_serviceChargeFrequency_monthly") mustBe Some("Off")
        readField(result, "lease_serviceChargeFrequency_quarterly") mustBe Some("Off")
        readField(result, "lease_serviceChargeFrequency_annually") mustBe Some("Yes")
        readField(result, "lease_serviceChargeFrequency_other") mustBe Some("Off")
      }

      "must write tenant to landlord considerations as 2 digit codes" in {
        val r = withLease(Lease(
          considToLndlrdDebt = Some("Yes"),
          considToLndlrdOther = Some("YES"),
          considToLndlrdSharedQTD = Some("Yes"),
          considToLndlrdServices = Some("yes")
        ))
        val result = fill(baseLand, r, true)

        readField(result, "lease_toLandlord_1") mustBe Some("01")
        readField(result, "lease_toLandlord_2") mustBe Some("04")
        readField(result, "lease_toLandlord_3") mustBe Some("05")
        readField(result, "lease_toLandlord_4") mustBe Some("08")
      }

      "must write landlord to tenant considerations as 2 digit codes" in {
        val r = withLease(Lease(
          considToTenantBuild = Some("Yes"),
          considToTenantSharesUNQTD = Some("YES"),
          considToTenantServices = Some("Yes"),
          considToTenantContin = Some("yes")
        ))
        val result = fill(baseLand, r, true)

        readField(result, "lease_toTenant_1") mustBe Some("02")
        readField(result, "lease_toTenant_2") mustBe Some("06")
        readField(result, "lease_toTenant_3") mustBe Some("08")
        readField(result, "lease_toTenant_4") mustBe Some("09")
      }

      "must handle a completely populated return without throwing" in {
        val r = FullReturn(
          stornId = "STORN999",
          returnResourceRef = "RRF-999",
          returnInfo = Some(ReturnInfo(
            returnID = Some("RET999"),
            landCertForEachProp = Some("YES"),
            mainLandID = Some("LND001")
          )),
          submission = Some(Submission(UTRN = Some("UTR-1234"))),
          transaction = Some(Transaction(
            transactionDescription = Some("Leasehold"),
            effectiveDate = Some("25/12/2024"),
            contractDate = Some("01/11/2024"),
            totalConsideration = Some("500000"),
            isLinked = Some("NO"),
            restrictionsAffectInterest = Some("NO"),
            isLandExchanged = Some("NO"),
            isPursuantToPreviousOption = Some("NO"),
            claimingRelief = Some("NO"),
            includesGoodwill = Some("YES"),
            includesOther = Some("YES"),
            usedAsOffice = Some("YES"),
            usedAsWarehouse = Some("YES"),
            usedAsOther = Some("YES"),
            postTransRulingApplied = Some("YES"),
            postTransRulingFollowed = Some("RulingNotReceived"),
            isDependantOnFutureEvent = Some("YES"),
            agreedToDeferPayment = Some("NO")
          )),
          taxCalculation = Some(TaxCalculation(taxDue = Some("15000"), amountPaid = Some("15000"), includesPenalty = Some("NO"))),
          land = Some(Seq(
            Land(
            landID = Some("LND001"),
            propertyType = Some("02"),
            houseNumber = Some("1"),
            address1 = Some("Test Street"),
            postcode = Some("ST1 1AA"),
            localAuthorityNumber = Some("1234"),
            titleNumber = Some("AB123"),
            areaUnit = Some("Hectares"),
            landArea = Some("0.5"),
            willSendPlanByPost = Some("NO"),
            mineralRights = Some("YES"),
            NLPGUPRN = Some("TEST1234"),
            interestCreatedTransferred = Some("OT")
            ),
            Land(
              landID = Some("LND002"),
              propertyType = Some("02"),
              houseNumber = Some("2"),
              address1 = Some("Test Street"),
              postcode = Some("ST1 2AA"),
              localAuthorityNumber = Some("2234"),
              titleNumber = Some("AB223"),
              areaUnit = Some("Hectares"),
              landArea = Some("0.5"),
              willSendPlanByPost = Some("NO"),
              mineralRights = Some("NO"),
              NLPGUPRN = Some("TEST2234"),
              interestCreatedTransferred = Some("OT")
            )
          )),
          vendor = Some(Seq(Vendor(name = Some("Vendor One"), postcode = Some("ST2 2BB")))),
          returnAgent = Some(Seq(ReturnAgent(name = Some("Agent Ltd")))),
          companyDetails = Some(CompanyDetails(
            companyTypeBuilder = Some("YES"),
            companyTypePartnership = Some("YES"),
            companyTypePubliccorp = Some("YES"),
            companyTypeOtherfinancial = Some("YES")
          )),
          lease = Some(Lease(
            leaseType = Some("R"),
            contractStartDate = Some("01/04/2024"),
            contractEndDate = Some("01/04/2026"),
            rentFreePeriod = Some("5"),
            startingRent = Some("2000"),
            startingRentEndDate = Some("01/04/2025"),
            VATAmount = Some("20"),
            totalPremiumPayable = Some("500000"),
            netPresentValue = Some("1897"),
            termsSurrendered = Some("Surrender & Regrant"),
            breakClauseDate = Some("01/04/2022"),
            rentReviewFrequency = Some("12"),
            firstReviewDate = Some("01/10/2021"),
            rentChargeDate = Some("01/04/2021"),
            serviceCharge = Some("1000"),
            breakClauseType = Some("landlord"),
            optionToRenew = Some("YES"),
            reviewClauseType = Some("RPI"),
            serviceChargeFrequency = Some("Anually"),
            considToLndlrdDebt = Some("Yes"),
            considToLndlrdOther = Some("YES"),
            considToLndlrdSharedQTD = Some("Yes"),
            considToLndlrdServices = Some("yes"),
            considToTenantBuild = Some("Yes"),
            considToTenantSharesUNQTD = Some("YES"),
            considToTenantServices = Some("Yes"),
            considToTenantContin = Some("yes")
          ))
        )

        val l = Land(
          landID = Some("LND002"),
          propertyType = Some("02"),
          houseNumber = Some("2"),
          address1 = Some("Test Street"),
          postcode = Some("ST1 2AA"),
          localAuthorityNumber = Some("2234"),
          titleNumber = Some("AB223"),
          areaUnit = Some("Hectares"),
          landArea = Some("0.5"),
          willSendPlanByPost = Some("NO"),
          mineralRights = Some("NO"),
          NLPGUPRN = Some("TEST2234"),
          interestCreatedTransferred = Some("OT")
        )

        noException mustBe thrownBy(buildFiller().fillPdf(l, r, true))
        noException mustBe thrownBy(buildFiller().fillPdf(l, r, false))
      }
    }
  }
}