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
import models.{FullReturn, Purchaser, ReturnInfo, Submission}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.{COSDictionary, COSName}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.annotation.{PDAppearanceDictionary, PDAppearanceStream}
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf2PurchaserSpec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN", "purchaser_title", "purchaser_surname", "purchaser_companyName",
    "purchaser_forename1", "purchaser_forename2", "purchaser_houseNumber",
    "purchaser_addressLine1", "purchaser_addressLine2", "purchaser_addressLine3",
    "purchaser_addressLine4", "purchaser_postcode_1", "purchaser_postcode_2"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "is_purchaser", "purchaser_connectedToVendor_yes",
    "purchaser_connectedToVendor_no", "purchaser_actingTrustee_yes",
    "purchaser_actingTrustee_no"
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

  private def fill(p: Purchaser, fullReturn: FullReturn) = buildFiller().fillPdf(p, fullReturn, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf2Purchaser = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT2purchaser.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf2Purchaser(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private def withPurchaser(purchaser: Purchaser): FullReturn =
    baseReturn.copy(purchaser = Some(Seq(purchaser)))

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  "fillPdf" - {

    "must return a non-empty byte array" in {
      val result = fill(Purchaser(), baseReturn)
      result must not be empty
    }

    "must return bytes starting with the %PDF header" in {
      val result = fill(Purchaser(), baseReturn)
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
      when(loader.load("SDLT2.pdf")).thenReturn(bare)
      val filler = new SdltReturnPdf2Purchaser(loader)
      intercept[SdltPdfFillException] {
        filler.fillPdf(Purchaser(), baseReturn)
      }
    }

    "must write UTRN from submission" in {
      val fullReturn = withSubmission("UTR-9999")
      val result = fill(Purchaser(), fullReturn)
      readField(result, "UTRN") mustBe Some("UTR-9999")
    }

    "must fill Vendor or Purchaser checkbox with Purchaser when there is additional purchaser" in {
      val fullReturn = withPurchaser(Purchaser())
      val result = fill(Purchaser(), fullReturn)
      readField(result, "is_purchaser") mustBe Some("Yes")
    }

    "must write purchaser title when purchaser is individual" in {
      val purchaser = Purchaser(isCompany = Some("NO"), title = Some("MR"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_title") mustBe Some("MR")
    }

    "must write purchaser surname when purchaser is individual" in {
      val purchaser = Purchaser(isCompany = Some("NO"), surname = Some("Jon"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_surname") mustBe Some("Jon")
    }

    "must write purchaser forename 1 and forename 2 when purchaser is individual" in {
      val purchaser = Purchaser(isCompany = Some("NO"), forename1 = Some("Bone"), forename2 = Some("Jones"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_forename1") mustBe Some("Bone")
      readField(result, "purchaser_forename2") mustBe Some("Jones")
    }

    "must write purchaser company name when purchaser is company" in {
      val purchaser = Purchaser(isCompany = Some("YES"), companyName = Some("Apple"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_companyName") mustBe Some("Apple")
    }

    "must not write purchaser surname, forename 1 and forename 2 when purchaser is company" in {
      val purchaser = Purchaser(isCompany = Some("YES"), companyName = Some("Apple"), surname = Some("Jon"), forename1 = Some("Bone"), forename2 = Some("Jones"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_surname") mustBe Some("")
      readField(result, "purchaser_forename1") mustBe Some("")
      readField(result, "purchaser_forename2") mustBe Some("")
    }

    "must not write purchaser company name when purchaser is individual" in {
      val purchaser = Purchaser(isCompany = Some("NO"), companyName = Some("Apple"), surname = Some("Jon"), forename1 = Some("Bone"), forename2 = Some("Jones"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_companyName") mustBe Some("")
    }

    "must split a standard postcode with space into two fields" in {
      val purchaser = Purchaser(postcode = Some("ST16 3AB"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_postcode_1") mustBe Some("ST16")
      readField(result, "purchaser_postcode_2") mustBe Some("3AB")
    }

    "must put a postcode with no space entirely in field 1" in {
      val purchaser = Purchaser(postcode = Some("ST163AB"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_postcode_1") mustBe Some("ST163AB")
      readField(result, "purchaser_postcode_2") mustBe Some("")
    }

    "must write purchaser address fields" in {
      val purchaser = Purchaser(
        houseNumber = Some("42"),
        address1 = Some("Main Street"),
        address2 = Some("Stafford"),
        address3 = Some("Somewhere"),
        address4 = Some("Anywhere"),
        postcode = Some("ST16 3AB")
      )
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_houseNumber") mustBe Some("42")
      readField(result, "purchaser_addressLine1") mustBe Some("Main Street")
      readField(result, "purchaser_addressLine2") mustBe Some("Stafford")
      readField(result, "purchaser_addressLine3") mustBe Some("Somewhere")
      readField(result, "purchaser_addressLine4") mustBe Some("Anywhere")
    }

    "must fill yes checkbox when purchaser and vendor are connected" in {
      val purchaser = Purchaser(isConnectedToVendor = Some("YES"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_connectedToVendor_yes") mustBe Some("Yes")
      readField(result, "purchaser_connectedToVendor_no") mustBe Some("Off")
    }

    "must fill no checkbox when purchaser and vendor are not connected" in {
      val purchaser = Purchaser(isConnectedToVendor = Some("NO"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_connectedToVendor_yes") mustBe Some("Off")
      readField(result, "purchaser_connectedToVendor_no") mustBe Some("Yes")
    }

    "must fill yes checkbox when purchaser is acting as trustee" in {
      val purchaser = Purchaser(isTrustee = Some("YES"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_actingTrustee_yes") mustBe Some("Yes")
      readField(result, "purchaser_actingTrustee_no") mustBe Some("Off")
    }

    "must fill no checkbox when purchaser is not acting as trustee" in {
      val purchaser = Purchaser(isTrustee = Some("NO"))
      val fullReturn = withPurchaser(purchaser)
      val result = fill(purchaser, fullReturn)
      readField(result, "purchaser_actingTrustee_yes") mustBe Some("Off")
      readField(result, "purchaser_actingTrustee_no") mustBe Some("Yes")
    }

    "must handle a completely populated purchaser in return without throwing" in {
      val purchaser = Purchaser(
        isCompany = Some("NO"),
        title = Some("MR"),
        surname = Some("Jon"),
        forename1 = Some("Bone"),
        forename2 = Some("Jones"),
        houseNumber = Some("1"),
        address1 = Some("Test Street"),
        postcode = Some("ST1 1AA"),
        isTrustee = Some("YES"),
        isConnectedToVendor = Some("YES")
      )

      val fullReturn = FullReturn(
        stornId = "STORN999",
        returnResourceRef = "RRF-999",
        returnInfo = Some(ReturnInfo(returnID = Some("RET999"))),
        submission = Some(Submission(UTRN = Some("UTR-1234"))),
        purchaser = Some(Seq(purchaser))
      )
      noException mustBe thrownBy(buildFiller().fillPdf(purchaser, fullReturn))
    }
  }
}


