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

class SdltReturnPdf1cSpec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN",
    "purchaser_nino_1", "purchaser_nino_2", "purchaser_nino_3", "purchaser_nino_4", "purchaser_nino_5",
    "purchaser_dob_day", "purchaser_dob_month", "purchaser_dob_year",
    "purchaser_vatReference",
    "purchaser_companyUtr",
    "purchaser_registeredNumber",
    "purchaser_registeredPlace",
    "purchaser_numberPurchasers",
    "purchaser_title",
    "purchaser_companyName",
    "purchaser_forename1", "purchaser_forename2",
    "purchaser_houseNumber",
    "purchaser_addressLine1", "purchaser_addressLine2", "purchaser_addressLine3", "purchaser_addressLine4",
    "purchaser_postcode_1", "purchaser_postcode_2",
    "purchaser_daytimePhoneNumber",
    "purchaser_agentName"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "purchaser_actingTrustee_yes", "purchaser_actingTrustee_no",
    "purchaser_connectedToVendor_yes", "purchaser_connectedToVendor_no",
    "land_certificateAddress_property", "land_certificateAddress_purchaser", "land_certificateAddress_agent",
    "purchaser_agentAuthorised_yes", "purchaser_agentAuthorised_no"
  )

  private def buildTemplatePdf(): Array[Byte] = {
    val doc  = new PDDocument()
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

  private def fill(r: FullReturn) = buildFiller().fillPdf(r, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf1c = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT1c.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf1c(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId           = "STORN001",
    returnResourceRef = "RRF-001",
    returnInfo        = Some(completeReturnInfo)
  )

  private def withPurchaser(purchasers: Purchaser*): FullReturn =
    baseReturn.copy(purchaser = Some(purchasers.toSeq))

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private val mainPurchaser: Purchaser = Purchaser(
    purchaserID          = Some("PUR001"),
    isCompany            = Some("NO"),
    title                = Some("Mr"),
    surname              = Some("Smith"),
    forename1            = Some("John"),
    forename2            = Some("David"),
    houseNumber          = Some("42"),
    address1             = Some("High Street"),
    address2             = Some("Kensington"),
    address3             = Some("London"),
    address4             = None,
    postcode             = Some("SW1A 1AA"),
    phone                = Some("020 7946 0958"),
    nino                 = Some("AB 123456 C"),
    isTrustee            = Some("YES"),
    isConnectedToVendor  = Some("NO"),
    isRepresentedByAgent = Some("YES"),
    hasNino              = Some("yes"),
    dateOfBirth          = Some("1985-05-15"),
    registrationNumber   = None,
    placeOfRegistration  = None
  )

  "SdltReturnPdf1c" - {

    "fillPdf" - {

      "must return a non-empty byte array" in {
        fill(baseReturn) must not be empty
      }

      "must return bytes starting with the %PDF header" in {
        new String(fill(baseReturn).take(4)) mustBe "%PDF"
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
        when(loader.load("SDLT1c.pdf")).thenReturn(bare)
        intercept[SdltPdfFillException] {
          new SdltReturnPdf1c(loader).fillPdf(baseReturn)
        }
      }

      "must write UTRN from submission" in {
        readField(fill(withSubmission("UTR-9999")), "UTRN") mustBe Some("UTR-9999")
      }

      "must write purchaser count derived from purchasers list size" in {
        val r = withPurchaser(mainPurchaser, mainPurchaser.copy(purchaserID = Some("PUR002")))
        readField(fill(r), "purchaser_numberPurchasers") mustBe Some("2")
      }

      "must write 0 for number of purchasers when none are present" in {
        readField(fill(baseReturn), "purchaser_numberPurchasers") mustBe Some("0")
      }

      "must split a NINO across five boxes (2/2/2/2/1)" in {
        val result = fill(withPurchaser(mainPurchaser))
        readField(result, "purchaser_nino_1") mustBe Some("AB")
        readField(result, "purchaser_nino_2") mustBe Some("12")
        readField(result, "purchaser_nino_3") mustBe Some("34")
        readField(result, "purchaser_nino_4") mustBe Some("56")
        readField(result, "purchaser_nino_5") mustBe Some("C")
      }

      "must leave NINO boxes blank when purchaser has no NINO" in {
        val result = fill(withPurchaser(mainPurchaser.copy(nino = None)))
        readField(result, "purchaser_nino_1") mustBe Some("")
        readField(result, "purchaser_nino_5") mustBe Some("")
      }

      "must split date of birth into day / month / year" in {
        val result = fill(withPurchaser(mainPurchaser))
        readField(result, "purchaser_dob_day")   mustBe Some("15")
        readField(result, "purchaser_dob_month") mustBe Some("05")
        readField(result, "purchaser_dob_year")  mustBe Some("1985")
      }

      "must leave date of birth fields blank when dateOfBirth is absent" in {
        val result = fill(withPurchaser(mainPurchaser.copy(dateOfBirth = None)))
        readField(result, "purchaser_dob_day")   mustBe Some("")
        readField(result, "purchaser_dob_month") mustBe Some("")
        readField(result, "purchaser_dob_year")  mustBe Some("")
      }

      "must write foreign tax reference and country when present" in {
        val p = mainPurchaser.copy(
          nino                = None,
          registrationNumber  = Some("FOREIGN-REF-123"),
          placeOfRegistration = Some("France")
        )
        val result = fill(withPurchaser(p))
        readField(result, "purchaser_registeredNumber") mustBe Some("FOREIGN-REF-123")
        readField(result, "purchaser_registeredPlace")  mustBe Some("France")
      }

      "must write VAT reference from companyDetails" in {
        val r = withPurchaser(mainPurchaser).copy(
          companyDetails = Some(CompanyDetails(VATReference = Some("GB123456789")))
        )
        readField(fill(r), "purchaser_vatReference") mustBe Some("GB123456789")
      }

      "must write company UTR from companyDetails" in {
        val r = withPurchaser(mainPurchaser).copy(
          companyDetails = Some(CompanyDetails(UTR = Some("1234567890")))
        )
        readField(fill(r), "purchaser_companyUtr") mustBe Some("1234567890")
      }

      "must write title, surname and forenames for an individual purchaser" in {
        val result = fill(withPurchaser(mainPurchaser))
        readField(result, "purchaser_title")       mustBe Some("Mr")
        readField(result, "purchaser_companyName") mustBe Some("Smith")
        readField(result, "purchaser_forename1")   mustBe Some("John")
        readField(result, "purchaser_forename2")   mustBe Some("David")
      }

      "must write company name and leave title and forenames blank for a company purchaser" in {
        val company = mainPurchaser.copy(
          isCompany   = Some("YES"),
          companyName = Some("Acme Ltd"),
          title       = Some("Mr"),
          forename1   = Some("John")
        )
        val result = fill(withPurchaser(company))
        readField(result, "purchaser_title")       mustBe Some("")
        readField(result, "purchaser_companyName") mustBe Some("Acme Ltd")
        readField(result, "purchaser_forename1")   mustBe Some("")
        readField(result, "purchaser_forename2")   mustBe Some("")
      }

      "must write purchaser address and split postcode" in {
        val result = fill(withPurchaser(mainPurchaser))
        readField(result, "purchaser_houseNumber")  mustBe Some("42")
        readField(result, "purchaser_addressLine1") mustBe Some("High Street")
        readField(result, "purchaser_addressLine2") mustBe Some("Kensington")
        readField(result, "purchaser_addressLine3") mustBe Some("London")
        readField(result, "purchaser_postcode_1")   mustBe Some("SW1A")
        readField(result, "purchaser_postcode_2")   mustBe Some("1AA")
      }

      "must write daytime phone number" in {
        readField(fill(withPurchaser(mainPurchaser)), "purchaser_daytimePhoneNumber") mustBe Some("020 7946 0958")
      }

      "must set trustee yes checkbox when isTrustee is YES" in {
        val result = fill(withPurchaser(mainPurchaser.copy(isTrustee = Some("YES"))))
        readField(result, "purchaser_actingTrustee_yes") mustBe Some("Yes")
        readField(result, "purchaser_actingTrustee_no")  mustBe Some("Off")
      }

      "must set trustee no checkbox when isTrustee is NO" in {
        val result = fill(withPurchaser(mainPurchaser.copy(isTrustee = Some("NO"))))
        readField(result, "purchaser_actingTrustee_yes") mustBe Some("Off")
        readField(result, "purchaser_actingTrustee_no")  mustBe Some("Yes")
      }

      "must set connected to vendor yes when isConnectedToVendor is YES" in {
        val result = fill(withPurchaser(mainPurchaser.copy(isConnectedToVendor = Some("YES"))))
        readField(result, "purchaser_connectedToVendor_yes") mustBe Some("Yes")
        readField(result, "purchaser_connectedToVendor_no")  mustBe Some("Off")
      }

      "must set connected to vendor no when isConnectedToVendor is NO" in {
        val result = fill(withPurchaser(mainPurchaser.copy(isConnectedToVendor = Some("NO"))))
        readField(result, "purchaser_connectedToVendor_yes") mustBe Some("Off")
        readField(result, "purchaser_connectedToVendor_no")  mustBe Some("Yes")
      }

      "must always check property for certificate address (box 60)" in {
        val result = fill(baseReturn)
        readField(result, "land_certificateAddress_property")  mustBe Some("Yes")
        readField(result, "land_certificateAddress_purchaser") mustBe Some("Off")
        readField(result, "land_certificateAddress_agent")     mustBe Some("Off")
      }

      "must set agent authorised yes when ReturnAgent.isAuthorised is YES" in {
        val r = withPurchaser(mainPurchaser).copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("PURCHASER"), isAuthorised = Some("YES"))))
        )
        val result = fill(r)
        readField(result, "purchaser_agentAuthorised_yes") mustBe Some("Yes")
        readField(result, "purchaser_agentAuthorised_no")  mustBe Some("Off")
      }

      "must set agent authorised no when ReturnAgent.isAuthorised is NO" in {
        val r = withPurchaser(mainPurchaser).copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("PURCHASER"), isAuthorised = Some("NO"))))
        )
        val result = fill(r)
        readField(result, "purchaser_agentAuthorised_yes") mustBe Some("Off")
        readField(result, "purchaser_agentAuthorised_no")  mustBe Some("Yes")
      }

      "must leave agent authorised unchecked when no purchaser ReturnAgent is present" in {
        val result = fill(withPurchaser(mainPurchaser))
        readField(result, "purchaser_agentAuthorised_yes") mustBe Some("Off")
        readField(result, "purchaser_agentAuthorised_no")  mustBe Some("Off")
      }

      "must write agent name from return agent of type PURCHASER" in {
        val r = withPurchaser(mainPurchaser).copy(
          returnAgent = Some(Seq(ReturnAgent(name = Some("Jones & Co"), agentType = Some("PURCHASER"))))
        )
        readField(fill(r), "purchaser_agentName") mustBe Some("Jones & Co")
      }

      "must leave agent name blank when no return agent of type PURCHASER is present" in {
        val r = withPurchaser(mainPurchaser).copy(
          returnAgent = Some(Seq(ReturnAgent(name = Some("Vendor Agent"), agentType = Some("VENDOR"))))
        )
        readField(fill(r), "purchaser_agentName") mustBe Some("")
      }

      "must leave agent name blank when no return agent is present" in {
        readField(fill(baseReturn), "purchaser_agentName") mustBe Some("")
      }

      "must handle a completely populated return without throwing" in {
        val r = baseReturn.copy(
          submission     = Some(Submission(UTRN = Some("UTR-1234"))),
          purchaser      = Some(Seq(mainPurchaser)),
          companyDetails = Some(CompanyDetails(UTR = Some("1234567890"), VATReference = Some("GB123456789"))),
          returnAgent    = Some(Seq(ReturnAgent(name = Some("Agent Ltd"), agentType = Some("PURCHASER"))))
        )
        noException mustBe thrownBy(buildFiller().fillPdf(r))
      }

      "must handle a minimal return without throwing" in {
        noException mustBe thrownBy(buildFiller().fillPdf(baseReturn))
      }
    }
  }
}
