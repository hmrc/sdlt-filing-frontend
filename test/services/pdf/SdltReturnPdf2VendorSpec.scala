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
import models.{FullReturn, Vendor, ReturnInfo, Submission}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.{COSDictionary, COSName}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.annotation.{PDAppearanceDictionary, PDAppearanceStream}
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf2VendorSpec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN", "vendor_title", "vendor_name", "vendor_companyName",
    "vendor_forename1", "vendor_forename2", "vendor_houseNumber",
    "vendor_addressLine1", "vendor_addressLine2", "vendor_addressLine3",
    "vendor_addressLine4", "vendor_postcode_1", "vendor_postcode_2"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "is_vendor"
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

  private def fill(v: Vendor, fullReturn: FullReturn) = buildFiller().fillPdf(v, fullReturn, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf2Vendor = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT2vendor.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf2Vendor(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private def withVendor(vendor: Vendor): FullReturn =
    baseReturn.copy(vendor = Some(Seq(vendor)))

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  "fillPdf" - {

    "must return a non-empty byte array" in {
      val result = fill(Vendor(), baseReturn)
      result must not be empty
    }

    "must return bytes starting with the %PDF header" in {
      val result = fill(Vendor(), baseReturn)
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
      val filler = new SdltReturnPdf2Vendor(loader)
      intercept[SdltPdfFillException] {
        filler.fillPdf(Vendor(), baseReturn)
      }
    }

    "must write UTRN from submission" in {
      val fullReturn = withSubmission("UTR-9999")
      val result = fill(Vendor(), fullReturn)
      readField(result, "UTRN") mustBe Some("UTR-9999")
    }

    "must fill Vendor or Purchaser checkbox with Vendor when there is additional vendor" in {
      val fullReturn = withVendor(Vendor())
      val result = fill(Vendor(), fullReturn)
      readField(result, "is_vendor") mustBe Some("Yes")
    }

    "must write vendor title when vendor is individual" in {
      val vendor = Vendor(title = Some("MR"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_title") mustBe Some("MR")
    }

    "must write vendor surname" in {
      val vendor = Vendor(name = Some("Jon"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_name") mustBe Some("Jon")
    }

    "must write vendor forename 1 and forename 2" in {
      val vendor = Vendor(forename1 = Some("Bone"), forename2 = Some("Jones"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_forename1") mustBe Some("Bone")
      readField(result, "vendor_forename2") mustBe Some("Jones")
    }

    "must not write vendor company name when purchaser is individual" in {
      val vendor = Vendor(name = Some("Jon"), forename1 = Some("Bone"), forename2 = Some("Jones"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_companyName") mustBe Some("")
    }

    "must split a standard postcode with space into two fields" in {
      val vendor = Vendor(postcode = Some("ST16 3AB"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_postcode_1") mustBe Some("ST16")
      readField(result, "vendor_postcode_2") mustBe Some("3AB")
    }

    "must put a postcode with no space entirely in field 1" in {
      val vendor = Vendor(postcode = Some("ST163AB"))
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_postcode_1") mustBe Some("ST163AB")
      readField(result, "vendor_postcode_2") mustBe Some("")
    }

    "must write vendor address fields" in {
      val vendor = Vendor(
        houseNumber = Some("42"),
        address1 = Some("Main Street"),
        address2 = Some("Stafford"),
        address3 = Some("Somewhere"),
        address4 = Some("Anywhere"),
        postcode = Some("ST16 3AB")
      )
      val fullReturn = withVendor(vendor)
      val result = fill(vendor, fullReturn)
      readField(result, "vendor_houseNumber") mustBe Some("42")
      readField(result, "vendor_addressLine1") mustBe Some("Main Street")
      readField(result, "vendor_addressLine2") mustBe Some("Stafford")
      readField(result, "vendor_addressLine3") mustBe Some("Somewhere")
      readField(result, "vendor_addressLine4") mustBe Some("Anywhere")
    }

    "must handle a completely populated vendor in return without throwing" in {
      val vendor = Vendor(
        title = Some("MR"),
        name = Some("Jon"),
        forename1 = Some("Bone"),
        forename2 = Some("Jones"),
        houseNumber = Some("1"),
        address1 = Some("Test Street"),
        address2 = Some("address2"),
        address3 = Some("address3"),
        address4 = Some("address4"),
        postcode = Some("ST1 1AA")
      )

      val fullReturn = FullReturn(
        stornId = "STORN999",
        returnResourceRef = "RRF-999",
        returnInfo = Some(ReturnInfo(returnID = Some("RET999"))),
        submission = Some(Submission(UTRN = Some("UTR-1234"))),
        vendor = Some(Seq(vendor))
      )
      noException mustBe thrownBy(buildFiller().fillPdf(vendor, fullReturn))
    }
  }
}


