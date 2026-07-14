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

class SdltReturnPdf3Spec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN",
    "land_typeProperty",
    "land_estateOrInterestTransfered",
    "land_numberProperties",
    "land_postcode_1",
    "land_postcode_2",
    "land_houseNumber",
    "land_addressLine1",
    "land_addressLine2",
    "land_addressLine3",
    "land_addressLine4",
    "land_localAuthorityNumber",
    "land_titleNumber",
    "land_nlpgUprn",
    "land_landArea",
    "land_landArea_decimal",
    "land_mineralRights"
  )

  private val allCheckboxFields: Seq[String] = Seq(
    "land_landAreaType_Hectares",
    "land_landAreaType_Square_Metres",
    "land_planAttached_yes",
    "land_planAttached_no"
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

  private def fill(r: FullReturn, land: Land) = buildFiller().fillPdf(r, land, flatten = false)

  def readField(pdfBytes: Array[Byte], fieldName: String): Option[String] = {
    val doc = Loader.loadPDF(pdfBytes)
    try {
      Option(doc.getDocumentCatalog.getAcroForm)
        .flatMap(f => Option(f.getField(fieldName)))
        .map(_.getValueAsString)
    } finally doc.close()
  }

  private def buildFiller(): SdltReturnPdf3 = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT3.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf3(loader)
  }

  private val baseReturn: FullReturn = FullReturn(
    stornId           = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private def withLand(land: Land): FullReturn =
    baseReturn.copy(land = Some(Seq(land)))


  "SdltReturnPdf3" - {

    "fillPdf" - {

      "must return a non-empty byte array" in {
        val result = fill(baseReturn, Land())
        result must not be empty
      }

      "must return bytes starting with the %PDF header" in {
        val result = fill(baseReturn, Land())
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
        when(loader.load("SDLT3.pdf")).thenReturn(bare)
        val filler = new SdltReturnPdf3(loader)
        intercept[SdltPdfFillException] {
          filler.fillPdf(baseReturn, Land())
        }
      }

      "must write UTRN from submission" in {
        val r      = withSubmission("UTR-9999")
        val result = fill(r, Land())
        readField(result, "UTRN") mustBe Some("UTR-9999")
      }

      "must write property type from land" in {
        val land = Land(propertyType = Some("01"))
        val r      = withLand(land)
        val result = fill(r, land)
        readField(result, "land_typeProperty") mustBe Some("01")
      }


      "must write interest created or transferred as 2 character string" in {
        val land = Land(interestCreatedTransferred = Some("FG"))
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_estateOrInterestTransfered") mustBe Some("FG")
      }

      "must split a standard postcode with space into two fields" in {
        val land = Land(postcode = Some("ST16 3AB"))
        val r      = withLand(land)
        val result = fill(r, land)
        readField(result, "land_postcode_1") mustBe Some("ST16")
        readField(result, "land_postcode_2") mustBe Some("3AB")
      }

      "must put a postcode with no space entirely in field 1" in {
        val land = Land(postcode = Some("ST163AB"))
        val r      = withLand(land)
        val result = fill(r, land)
        readField(result, "land_postcode_1") mustBe Some("ST163AB")
        readField(result, "land_postcode_2") mustBe Some("")
      }

      "must write land address fields" in {
        val land = Land(
          houseNumber = Some("42"),
          address1    = Some("Main Street"),
          address2    = Some("Stafford"),
          postcode    = Some("ST16 3AB")
        )
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_houseNumber")  mustBe Some("42")
        readField(result, "land_addressLine1") mustBe Some("Main Street")
        readField(result, "land_addressLine2") mustBe Some("Stafford")
      }

      "must write local authority number and title number" in {
        val land = Land(
          localAuthorityNumber = Some("5678"),
          titleNumber          = Some("MX123456")
        )
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_localAuthorityNumber") mustBe Some("5678")
        readField(result, "land_titleNumber")          mustBe Some("MX123456")
      }

      "must write area of land in hectares" in {
        val land = Land(
          areaUnit = Some("Hectares"),
          landArea = Some("100.123")
        )
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_landAreaType_Hectares") mustBe Some("Yes")
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100")
        readField(result, "land_landArea_decimal") mustBe Some("123")
      }

      "must write area of land in square metres" in {
        val land = Land(
          areaUnit = Some("SquareMetres"),
          landArea = Some("100.000")
        )
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_landAreaType_Square_Metres") mustBe Some("Yes")
        readField(result, "land_landAreaType_Hectares") mustBe Some("Off")
        readField(result, "land_landArea") mustBe Some("100")
        readField(result, "land_landArea_decimal") mustBe Some("000")
      }

      "must write area of land with no decimal places if no decimal point" in {
        val land = Land(
          areaUnit = Some("SquareMetres"),
          landArea = Some("100")
        )
        val r = withLand(land)
        val result = fill(r, land)
        readField(result, "land_landArea") mustBe Some("100")
        readField(result, "land_landArea_decimal") mustBe Some("")
      }

      "must handle a completely populated land in return without throwing" in {
        val land = Land(
          propertyType         = Some("01"),
          houseNumber          = Some("1"),
          address1             = Some("Test Street"),
          postcode             = Some("ST1 1AA"),
          localAuthorityNumber = Some("1234"),
          titleNumber          = Some("AB123"),
          areaUnit             = Some("Hectares"),
          landArea             = Some("0.5"),
          willSendPlanByPost   = Some("NO"),
          mineralRights        = Some("YES")
        )
        val r = FullReturn(
          stornId           = "STORN999",
          returnResourceRef = "RRF-999",
          returnInfo        = Some(ReturnInfo(returnID = Some("RET999"))),
          submission        = Some(Submission(UTRN = Some("UTR-1234"))),
          land           = Some(Seq(land))
        )
        noException mustBe thrownBy(buildFiller().fillPdf(r, land))
      }
    }
  }
}