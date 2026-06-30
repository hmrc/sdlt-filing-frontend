/*
 * Copyright 2026 HM Revenue & Customs
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
import constants.FullReturnConstants.completeReturnInfo
import models.{CompanyDetails, FullReturn, Land, Lease, Purchaser, ReturnAgent, ReturnInfo, Submission, TaxCalculation, Transaction, Vendor}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.{COSDictionary, COSName}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.annotation.{PDAppearanceDictionary, PDAppearanceStream}
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf1dSpec extends SpecBase with MockitoSugar {
  private val allTextFields: Seq[String] = Seq(
    "UTRN", "purchaser_title", "purchaser_companyName", "purchaser_forename1",
    "purchaser_forename2", "purchaser_houseNumber", "purchaser_addressLine1", "purchaser_addressLine2",
    "purchaser_addressLine3", "purchaser_addressLine4", "purchaser_postcode_1", "purchaser_postcode_2",
    "purchaser_sameAddress", "purchaser_agentName", "purchaser_agentPostcode_1", "purchaser_agentPostcode_2",
    "purchaser_agentHouseNumber", "purchaser_agentAddressLine1", "purchaser_agentAddressLine2",
    "purchaser_agentAddressLine3", "purchaser_agentAddressLine4", "purchaser_agentDxAddress",
    "purchaser_agentReference", "purchaser_agentPhoneNumber", "return_additionalDetailsSdlt2",
    "return_additionalDetailsSdlt3", "return_additionalDetailsSdlt4"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "purchaser_actingTrustee_yes", "purchaser_actingTrustee_no"
  )

  private def buildTemplatePdf(): Array[Byte] = {
    val doc = new PDDocument()
    val page = new org.apache.pdfbox.pdmodel.PDPage()
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

  private def fill(r: FullReturn, additionalPurchaser: Option[Purchaser]) = buildFiller().fillPdf(additionalPurchaser, r, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf1d = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT1d.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf1d(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId = "STORN001",
    returnResourceRef = "RRF-001",
    returnInfo = Some(completeReturnInfo)
  )

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private def withPurchaser(purchasers: Purchaser*): FullReturn =
    baseReturn.copy(purchaser = Some(purchasers.toSeq))

  private def withReturnAgent(returnAgent: ReturnAgent*): FullReturn =
    baseReturn.copy(returnAgent = Some(returnAgent.toSeq))

  private val additionalPurchaser = Some(Purchaser(
      title = Some("MR"),
      isCompany = Some("NO"),
      surname = Some("Jones"),
      forename1 = Some("Jane"),
      forename2 = Some("Mary"),
      purchaserID = Some("PUR002"),
      houseNumber = Some("5"),
      address1 = Some("Address 1"),
      address2 = Some("Address 2"),
      address3 = Some("Address 3"),
      address4 = Some("Address 4"),
      postcode = Some("M1 2AB"),
      isTrustee = Some("YES")
    ))

  private val additionalPurchaserWithCompany = Some(Purchaser(
    title = Some("MR"),
    isCompany = Some("YES"),
    companyName = Some("Apple"),
    surname = Some("Jones"),
    forename1 = Some("Jane"),
    forename2 = Some("Mary"),
    purchaserID = Some("PUR002"),
    houseNumber = Some("5"),
    address1 = Some("Address 1"),
    address2 = Some("Address 2"),
    address3 = Some("Address 3"),
    address4 = Some("Address 4"),
    postcode = Some("M1 2AB"),
    isTrustee = Some("YES")
  ))

  val r = withPurchaser(
    Purchaser(
      isCompany = Some("NO"),
      title = Some("MR"),
      surname = Some("Smith"),
      forename1 = Some("John"),
      forename2 = Some("Paul"),
      purchaserID = Some("PUR001"),
      nextPurchaserID = Some("PUR002")
    ),
    Purchaser(
      isCompany = Some("NO"),
      title = Some("MR"),
      surname = Some("Jones"),
      forename1 = Some("Jane"),
      forename2 = Some("Mary"),
      purchaserID = Some("PUR002")
    )
  )

  "fillPdf" - {

    "must return a non-empty byte array" in {
      val result = fill(baseReturn, None)
      result must not be empty
    }

    "must return bytes starting with the %PDF header" in {
      val result = fill(baseReturn,None)
      new String(result.take(4)) mustBe "%PDF"
    }

    "must throw SdltPdfFillException when template has no AcroForm" in {
      val loader = mock[PdfTemplateLoader]
      val bare = {
        val d = new PDDocument()
        d.addPage(new org.apache.pdfbox.pdmodel.PDPage())
        val out = new ByteArrayOutputStream()
        d.save(out);
        d.close()
        out.toByteArray
      }
      when(loader.load("SDLT1d.pdf")).thenReturn(bare)
      val filler = new SdltReturnPdf1d(loader)
      intercept[SdltPdfFillException] {
        filler.fillPdf(None, baseReturn)
      }
    }

    "must write UTRN from submission" in {
      val r = withSubmission("UTR-9999")
      val result = fill(r, None)
      readField(result, "UTRN") mustBe Some("UTR-9999")
    }

    "must write second purchaser name and forenames" in {

      val result = fill(r, additionalPurchaser)
      readField(result, "purchaser_companyName") mustBe Some("Jones")
      readField(result, "purchaser_forename1") mustBe Some("Jane")
      readField(result, "purchaser_forename2") mustBe Some("Mary")
    }

    "must write second purchaser companyName when isCompany is 'Yes' " in {

      val result = fill(r, additionalPurchaserWithCompany)
      readField(result, "purchaser_companyName") mustBe Some("Apple")
      readField(result, "purchaser_forename1") mustBe Some("")
      readField(result, "purchaser_forename2") mustBe Some("")
    }

    "must write second purchaser address and postcode" in {

      val result = fill(r, additionalPurchaser)
      readField(result, "purchaser_houseNumber") mustBe Some("5")
      readField(result, "purchaser_addressLine1") mustBe Some("Address 1")
      readField(result, "purchaser_addressLine2") mustBe Some("Address 2")
      readField(result, "purchaser_addressLine3") mustBe Some("Address 3")
      readField(result, "purchaser_addressLine4") mustBe Some("Address 4")
      readField(result, "purchaser_postcode_1") mustBe Some("M1")
      readField(result, "purchaser_postcode_2") mustBe Some("2AB")
      readField(result, "purchaser_actingTrustee_yes") mustBe Some("Yes")
      readField(result, "purchaser_actingTrustee_no") mustBe Some("Off")
    }

    "must leave purchaser name blank when no additional purchaser is present" in {
      val result = fill(r, None)
      readField(result, "purchaser_companyName") mustBe Some("")
      readField(result, "purchaser_forename1") mustBe Some("")
      readField(result, "purchaser_forename2") mustBe Some("")
    }

    "must leave address blank when no additional purchaser is present" in {
      val result = fill(r, None)
      readField(result, "purchaser_houseNumber") mustBe Some("")
      readField(result, "purchaser_addressLine1") mustBe Some("")
      readField(result, "purchaser_addressLine2") mustBe Some("")
      readField(result, "purchaser_addressLine3") mustBe Some("")
      readField(result, "purchaser_addressLine4") mustBe Some("")
      readField(result, "purchaser_postcode_1") mustBe Some("")
      readField(result, "purchaser_postcode_2") mustBe Some("")
    }

    "must write purchaser agent details when return agent of type purchaser present" in {
      val r = withReturnAgent(ReturnAgent(
        returnAgentID = Some("RA001"),
        returnID = Some("RET001"),
        agentType = Some("PURCHASER"),
        name = Some("Purchaser Agent"),
        houseNumber = Some("1"),
        address1 = Some("Agent address 1"),
        address2 = Some("Agent address 2"),
        address3 = Some("Agent address 3"),
        address4 = Some("Agent address 4"),
        postcode = Some("EC4A 2DQ"),
        phone = Some("0123456789"),
        DXAddress = Some("London"),
        reference = Some("123456")
      ))
      val result = fill(r, additionalPurchaser)
      readField(result, "purchaser_agentHouseNumber") mustBe Some("1")
      readField(result, "purchaser_agentAddressLine1") mustBe Some("Agent address 1")
      readField(result, "purchaser_agentAddressLine2") mustBe Some("Agent address 2")
      readField(result, "purchaser_agentAddressLine3") mustBe Some("Agent address 3")
      readField(result, "purchaser_agentAddressLine4") mustBe Some("Agent address 4")
      readField(result, "purchaser_agentPostcode_1") mustBe Some("EC4A")
      readField(result, "purchaser_agentPostcode_2") mustBe Some("2DQ")
      readField(result, "purchaser_agentDxAddress") mustBe Some("London")
      readField(result, "purchaser_agentPhoneNumber") mustBe Some("0123456789")
      readField(result, "purchaser_agentReference") mustBe Some("123456")
    }

    "must not write purchaser agent details when return agent not present" in {

      val result = fill(baseReturn, additionalPurchaser)
      readField(result, "purchaser_agentHouseNumber") mustBe Some("")
      readField(result, "purchaser_agentAddressLine1") mustBe Some("")
      readField(result, "purchaser_agentAddressLine2") mustBe Some("")
      readField(result, "purchaser_agentAddressLine3") mustBe Some("")
      readField(result, "purchaser_agentAddressLine4") mustBe Some("")
      readField(result, "purchaser_agentPostcode_1") mustBe Some("")
      readField(result, "purchaser_agentPostcode_2") mustBe Some("")
      readField(result, "purchaser_agentDxAddress") mustBe Some("")
      readField(result, "purchaser_agentPhoneNumber") mustBe Some("")
      readField(result, "purchaser_agentReference") mustBe Some("")
    }

    "must write purchaser agent details when return agent of type VENDOR present" in {
      val r = withReturnAgent(ReturnAgent(
        returnAgentID = Some("RA001"),
        returnID = Some("RET001"),
        agentType = Some("VENDOR"),
        name = Some("Purchaser Agent"),
        houseNumber = Some("1"),
        address1 = Some("Agent address 1"),
        address2 = Some("Agent address 2"),
        address3 = Some("Agent address 3"),
        address4 = Some("Agent address 4"),
        postcode = Some("EC4A 2DQ"),
        phone = Some("0123456789"),
        DXAddress = Some("London"),
        reference = Some("123456")
      ))

      val result = fill(r, additionalPurchaser)
      readField(result, "purchaser_agentHouseNumber") mustBe Some("")
      readField(result, "purchaser_agentAddressLine1") mustBe Some("")
      readField(result, "purchaser_agentAddressLine2") mustBe Some("")
      readField(result, "purchaser_agentAddressLine3") mustBe Some("")
      readField(result, "purchaser_agentAddressLine4") mustBe Some("")
      readField(result, "purchaser_agentPostcode_1") mustBe Some("")
      readField(result, "purchaser_agentPostcode_2") mustBe Some("")
      readField(result, "purchaser_agentDxAddress") mustBe Some("")
      readField(result, "purchaser_agentPhoneNumber") mustBe Some("")
      readField(result, "purchaser_agentReference") mustBe Some("")
    }

    "must handle a completely populated return without throwing" in {
      noException mustBe thrownBy(buildFiller().fillPdf(None,r))
    }


    "must write '0' for sdlt2, sdlt3, sdlt4 values when no additional data existed for vendor, purchaser, land and lease" in {
      val result = fill(r, additionalPurchaser)
      readField(result, "return_additionalDetailsSdlt2") mustBe Some("0")
      readField(result, "return_additionalDetailsSdlt3") mustBe Some("0")
      readField(result, "return_additionalDetailsSdlt4") mustBe Some("0")
    }

    "must write second purchaser, return agent details along with sdlt2, sdlt3, sdlt4 counts " in {
      val r = FullReturn(
        stornId = "STORN999",
        returnResourceRef = "RRF-999",
        returnInfo = Some(ReturnInfo(returnID = Some("RET999"), landCertForEachProp = Some("YES"), mainPurchaserID = Some("PUR0001"))),
        submission = Some(Submission(UTRN = Some("UTR-1234"))),
        transaction = Some(Transaction(transactionDescription = Some("Freehold"), effectiveDate = Some("25/12/2024"), contractDate = Some("01/11/2024"),
          totalConsideration = Some("500000"), isLinked = Some("NO")
        )),
        land = Some(Seq(Land(
          propertyType = Some("01"), houseNumber = Some("1"), address1 = Some("Test Street"),
          postcode = Some("ST1 1AA"), localAuthorityNumber = Some("1234")
        ), Land(
          propertyType = Some("02"), houseNumber = Some("2"), address1 = Some("Test Street"),
          postcode = Some("ST2 1AA"), localAuthorityNumber = Some("12344")))),
        purchaser = Some(Seq(Purchaser(surname = Some("Purchaser One"), postcode = Some("ST2 2BB"), registrationNumber = Some("123456"), placeOfRegistration = Some("London"), purchaserID = Some("PUR0001")),
          Purchaser(title = Some("MR"), surname = Some("Jones"), forename1 = Some("Jane"), forename2 = Some("Mary"),
            purchaserID = Some("PUR002"), houseNumber = Some("5"), address1 = Some("Address 1"), address2 = Some("Address 2"),
            address3 = Some("Address 3"), address4 = Some("Address 4"), postcode = Some("M1 2AB"), isTrustee = Some("NO")))),
        vendor = Some(Seq(Vendor(name = Some("Vendor One"), postcode = Some("ST2 2BB")),
          Vendor(name = Some("Vendor Two"), postcode = Some("ST2 2BB")), Vendor(name = Some("Vendor Three"), postcode = Some("ST2 2BB")))),
        lease = Some(Lease(leaseID = Some("123456"), returnID = Some("L123456"), isAnnualRentOver1000 = Some("250000"))),
        returnAgent = Some(Seq(ReturnAgent(name = Some("Agent Ltd"), returnAgentID = Some("RA001"), returnID = Some("RET001"),
          agentType = Some("PURCHASER"), houseNumber = Some("1"), address1 = Some("Agent address 1"), address2 = Some("Agent address 2"),
          address3 = Some("Agent address 3"), address4 = Some("Agent address 4"), postcode = Some("EC4A 2DQ"), phone = Some("0123456789"),
          DXAddress = Some("London"), reference = Some("123456")))),
        companyDetails = Some(CompanyDetails(companyDetailsID = Some("382966900"), returnID = Some("382966898"), purchaserID = Some("PUR0001"),
          UTR = null, VATReference = null, companyTypeBank = Some("no"), companyTypeBuilder = Some("yes"), companyTypeBuildsoc = Some("no"),
          companyTypeCentgov = Some("no"), companyTypeIndividual = Some("no"), companyTypeInsurance = Some("no"), companyTypeLocalauth = Some("no"),
          companyTypeOthercharity = Some("no"), companyTypeOtherfinancial = Some("no"), companyTypePartnership = Some("yes"), companyTypeProperty = Some("no"),
          companyTypePubliccorp = Some("no"), companyTypeSoletrader = Some("yes"), companyTypePensionfund = Some("no")))
      )
      val result = fill(r, additionalPurchaser)
      readField(result, "purchaser_agentHouseNumber") mustBe Some("1")
      readField(result, "purchaser_agentAddressLine1") mustBe Some("Agent address 1")
      readField(result, "purchaser_agentAddressLine2") mustBe Some("Agent address 2")
      readField(result, "purchaser_agentAddressLine3") mustBe Some("Agent address 3")
      readField(result, "purchaser_agentAddressLine4") mustBe Some("Agent address 4")
      readField(result, "purchaser_agentPostcode_1") mustBe Some("EC4A")
      readField(result, "purchaser_agentPostcode_2") mustBe Some("2DQ")
      readField(result, "purchaser_agentDxAddress") mustBe Some("London")
      readField(result, "purchaser_agentPhoneNumber") mustBe Some("0123456789")
      readField(result, "purchaser_agentReference") mustBe Some("123456")
      readField(result, "purchaser_companyName") mustBe Some("Jones")
      readField(result, "purchaser_forename1") mustBe Some("Jane")
      readField(result, "purchaser_forename2") mustBe Some("Mary")
      readField(result, "purchaser_houseNumber") mustBe Some("5")
      readField(result, "purchaser_addressLine1") mustBe Some("Address 1")
      readField(result, "purchaser_addressLine2") mustBe Some("Address 2")
      readField(result, "purchaser_addressLine3") mustBe Some("Address 3")
      readField(result, "purchaser_addressLine4") mustBe Some("Address 4")
      readField(result, "purchaser_postcode_1") mustBe Some("M1")
      readField(result, "purchaser_postcode_2") mustBe Some("2AB")
      readField(result, "purchaser_actingTrustee_yes") mustBe Some("Yes")
      readField(result, "purchaser_actingTrustee_no") mustBe Some("Off")
      readField(result, "return_additionalDetailsSdlt2") mustBe Some("1")
      readField(result, "return_additionalDetailsSdlt3") mustBe Some("1")
      readField(result, "return_additionalDetailsSdlt4") mustBe Some("1")

    }

    "must write sdlt4Count value as 1 when no land in the fullReturn  " in {
      val r = FullReturn(
        stornId = "STORN999",
        returnResourceRef = "RRF-999",
        returnInfo = Some(ReturnInfo(returnID = Some("RET999"), landCertForEachProp = Some("YES"), mainPurchaserID = Some("PUR0001"))),
        submission = Some(Submission(UTRN = Some("UTR-1234"))),
        transaction = Some(Transaction(transactionDescription = Some("Freehold"), effectiveDate = Some("25/12/2024"), contractDate = Some("01/11/2024"),
          totalConsideration = Some("500000"), isLinked = Some("NO")
        )),
        purchaser = Some(Seq(Purchaser(surname = Some("Purchaser One"), postcode = Some("ST2 2BB"), registrationNumber = Some("123456"), placeOfRegistration = Some("London"), purchaserID = Some("PUR0001")),
          Purchaser(title = Some("MR"), surname = Some("Jones"), forename1 = Some("Jane"), forename2 = Some("Mary"),
            purchaserID = Some("PUR002"), houseNumber = Some("5"), address1 = Some("Address 1"), address2 = Some("Address 2"),
            address3 = Some("Address 3"), address4 = Some("Address 4"), postcode = Some("M1 2AB"), isTrustee = Some("NO")))),
        vendor = Some(Seq(Vendor(name = Some("Vendor One"), postcode = Some("ST2 2BB")),
          Vendor(name = Some("Vendor Two"), postcode = Some("ST2 2BB")), Vendor(name = Some("Vendor Three"), postcode = Some("ST2 2BB")))),
        lease = Some(Lease(leaseID = Some("123456"), returnID = Some("L123456"), isAnnualRentOver1000 = Some("250000"))),
        returnAgent = Some(Seq(ReturnAgent(name = Some("Agent Ltd"), returnAgentID = Some("RA001"), returnID = Some("RET001"),
          agentType = Some("PURCHASER"), houseNumber = Some("1"), address1 = Some("Agent address 1"), address2 = Some("Agent address 2"),
          address3 = Some("Agent address 3"), address4 = Some("Agent address 4"), postcode = Some("EC4A 2DQ"), phone = Some("0123456789"),
          DXAddress = Some("London"), reference = Some("123456")))),
        companyDetails = Some(CompanyDetails(companyDetailsID = Some("382966900"), returnID = Some("382966898"), purchaserID = Some("PUR0001"),
          UTR = null, VATReference = null, companyTypeBank = Some("no"), companyTypeBuilder = Some("yes"), companyTypeBuildsoc = Some("no"),
          companyTypeCentgov = Some("no"), companyTypeIndividual = Some("no"), companyTypeInsurance = Some("no"), companyTypeLocalauth = Some("no"),
          companyTypeOthercharity = Some("no"), companyTypeOtherfinancial = Some("no"), companyTypePartnership = Some("yes"), companyTypeProperty = Some("no"),
          companyTypePubliccorp = Some("no"), companyTypeSoletrader = Some("yes"), companyTypePensionfund = Some("no")))
      )
      val result = fill(r, additionalPurchaser)
      readField(result, "return_additionalDetailsSdlt4") mustBe Some("1")

    }

    "must write sdlt4Count value as 0 when fullReturn has empty for land, companyDetails, transaction, purchaser " in {
      val r = FullReturn(
        stornId = "STORN999",
        returnResourceRef = "RRF-999",
        returnInfo = Some(ReturnInfo(returnID = Some("RET999"), landCertForEachProp = Some("YES"), mainPurchaserID = Some("PUR0001"))),
        submission = Some(Submission(UTRN = Some("UTR-1234"))),

        vendor = Some(Seq(Vendor(name = Some("Vendor One"), postcode = Some("ST2 2BB")),
          Vendor(name = Some("Vendor Two"), postcode = Some("ST2 2BB")), Vendor(name = Some("Vendor Three"), postcode = Some("ST2 2BB")))),

        returnAgent = Some(Seq(ReturnAgent(name = Some("Agent Ltd"), returnAgentID = Some("RA001"), returnID = Some("RET001"),
          agentType = Some("PURCHASER"), houseNumber = Some("1"), address1 = Some("Agent address 1"), address2 = Some("Agent address 2"),
          address3 = Some("Agent address 3"), address4 = Some("Agent address 4"), postcode = Some("EC4A 2DQ"), phone = Some("0123456789"),
          DXAddress = Some("London"), reference = Some("123456")))),

      )
      val result = fill(r, additionalPurchaser)
      readField(result, "return_additionalDetailsSdlt4") mustBe Some("0")

    }
  }
}


