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
import constants.FullReturnConstants.completeReturnInfo
import models.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDTextField}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayOutputStream

class SdltReturnPdf1bSpec extends SpecBase with MockitoSugar {

  private val allTextFields: Seq[String] = Seq(
    "UTRN",
    "vendor_numberVendors",
    "vendor_title", "vendor_name",
    "vendor_forename1", "vendor_forename2",
    "vendor_postcode_1", "vendor_postcode_2",
    "vendor_houseNumber",
    "vendor_addressLine1", "vendor_addressLine2", "vendor_addressLine3", "vendor_addressLine4",
    "vendor_agentName",
    "vendor_agentPostcode_1", "vendor_agentPostcode_2",
    "vendor_agentHouseNumber",
    "vendor_agentAddressLine1", "vendor_agentAddressLine2", "vendor_agentAddressLine3", "vendor_agentAddressLine4",
    "vendor_agentEmailAddress", "vendor_agentPhoneNumber",
    "vendor_agentReference",

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

  private def buildFiller(): SdltReturnPdf1b = {
    val loader = mock[PdfTemplateLoader]
    when(loader.load("SDLT1b.pdf")).thenReturn(buildTemplatePdf())
    new SdltReturnPdf1b(loader)
  }


  private val baseReturn: FullReturn = FullReturn(
    stornId           = "STORN001",
    returnResourceRef = "RRF-001",
    returnInfo = Some(completeReturnInfo)
  )

  private def withSubmission(utrn: String): FullReturn =
    baseReturn.copy(submission = Some(Submission(UTRN = Some(utrn))))

  private def withVendor(vendors: Vendor*): FullReturn =
    baseReturn.copy(vendor = Some(vendors.toSeq))

  private def withReturnAgent(returnAgent: ReturnAgent*): FullReturn =
    baseReturn.copy(returnAgent = Some(returnAgent.toSeq))


  
  "SdltReturnPdf1b" - {

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
        when(loader.load("SDLT1b.pdf")).thenReturn(bare)
        val filler = new SdltReturnPdf1b(loader)
        intercept[SdltPdfFillException] {
          filler.fillPdf(baseReturn)
        }
      }

      "must write UTRN from submission" in {
        val r      = withSubmission("UTR-9999")
        val result = fill(r)
        readField(result, "UTRN") mustBe Some("UTR-9999")
      }

      "must write second vendor name and forenames" in {
        val r = withVendor(
          Vendor(
            name = Some("Smith"),
            forename1 = Some("John"),
            forename2 = Some("Paul"),
            vendorID = Some("VEN001"),
            nextVendorID = Some("VEN002")
          ),
          Vendor(
            name = Some("Jones"),
            forename1 = Some("Jane"),
            forename2 = Some("Mary"),
            vendorID = Some("VEN002")
          )
        )
        val result = fill(r)
        readField(result, "vendor_name")     mustBe Some("Jones")
        readField(result, "vendor_forename1") mustBe Some("Jane")
        readField(result, "vendor_forename2") mustBe Some("Mary")
      }

      "must write second vendor address and postcode" in {
        val r = withVendor(Vendor(
          name = Some("Smith"),
          forename1 = Some("John"),
          forename2 = Some("Paul"),
          vendorID = Some("VEN001"),
          nextVendorID = Some("VEN002")
        ),
          Vendor(
            name = Some("Jones"),
            forename1 = Some("Jane"),
            forename2 = Some("Mary"),
            vendorID = Some("VEN002"),
            houseNumber = Some("5"),
            address1    = Some("Address 1"),
            address2    = Some("Address 2"),
            address3    = Some("Address 3"),
            address4    = Some("Address 4"),
            postcode    = Some("M1 2AB")
          ))
        val result = fill(r)
        readField(result, "vendor_houseNumber")  mustBe Some("5")
        readField(result, "vendor_addressLine1") mustBe Some("Address 1")
        readField(result, "vendor_addressLine2") mustBe Some("Address 2")
        readField(result, "vendor_addressLine3") mustBe Some("Address 3")
        readField(result, "vendor_addressLine4") mustBe Some("Address 4")
        readField(result, "vendor_postcode_1")   mustBe Some("M1")
        readField(result, "vendor_postcode_2")   mustBe Some("2AB")
      }

      "must leave vendor name blank when no vendor 2 is present" in {
        val r = withVendor(
          Vendor(
            name = Some("Smith"),
            forename1 = Some("John"),
            forename2 = Some("Paul"),
            vendorID = Some("VEN001")
          ))
        val result = fill(r)
        readField(result, "vendor_name")     mustBe Some("")
        readField(result, "vendor_forename1") mustBe Some("")
        readField(result, "vendor_forename2") mustBe Some("")
      }

      "must leave address blank when no vendor 2 is present" in {
        val r = withVendor(Vendor(
          name = Some("Smith"),
          forename1 = Some("John"),
          forename2 = Some("Paul"),
          vendorID = Some("VEN001")
        ))
        val result = fill(r)
        readField(result, "vendor_houseNumber") mustBe Some("")
        readField(result, "vendor_addressLine1") mustBe Some("")
        readField(result, "vendor_addressLine2") mustBe Some("")
        readField(result, "vendor_addressLine3") mustBe Some("")
        readField(result, "vendor_addressLine4") mustBe Some("")
        readField(result, "vendor_postcode_1") mustBe Some("")
        readField(result, "vendor_postcode_2") mustBe Some("")
      }

      "must write vendor agent details when return agent of type vendor present" in {
        val r = withReturnAgent(ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET001"),
          agentType = Some("VENDOR"),
          name = Some("Vendor Agent"),
          houseNumber = Some("1"),
          address1 = Some("Agent address 1"),
          address2 = Some("Agent address 2"),
          address3 = Some("Agent address 3"),
          address4 = Some("Agent address 4"),
          postcode =  Some("EC4A 2DQ"),
          phone = Some("0123456789"),
          email = Some("test@test.com"),
          DXAddress = None,
          reference = Some("123456")
        ))

        val result = fill(r)
        readField(result, "vendor_agentHouseNumber") mustBe Some("1")
        readField(result, "vendor_agentAddressLine1") mustBe Some("Agent address 1")
        readField(result, "vendor_agentAddressLine2") mustBe Some("Agent address 2")
        readField(result, "vendor_agentAddressLine3") mustBe Some("Agent address 3")
        readField(result, "vendor_agentAddressLine4") mustBe Some("Agent address 4")
        readField(result, "vendor_agentPostcode_1") mustBe Some("EC4A")
        readField(result, "vendor_agentPostcode_2") mustBe Some("2DQ")
        readField(result, "vendor_agentEmailAddress") mustBe Some("test@test.com")
        readField(result, "vendor_agentPhoneNumber") mustBe Some("0123456789")
        readField(result, "vendor_agentReference") mustBe Some("123456")
      }

      "must not write vendor agent details when return agent not present" in {

        val result = fill(baseReturn)
        readField(result, "vendor_agentHouseNumber") mustBe Some("")
        readField(result, "vendor_agentAddressLine1") mustBe Some("")
        readField(result, "vendor_agentAddressLine2") mustBe Some("")
        readField(result, "vendor_agentAddressLine3") mustBe Some("")
        readField(result, "vendor_agentAddressLine4") mustBe Some("")
        readField(result, "vendor_agentPostcode_1") mustBe Some("")
        readField(result, "vendor_agentPostcode_2") mustBe Some("")
        readField(result, "vendor_agentEmailAddress") mustBe Some("")
        readField(result, "vendor_agentPhoneNumber") mustBe Some("")
        readField(result, "vendor_agentReference") mustBe Some("")
      }

      "must not write vendor agent details when return agent of type PURCHASER present" in {
        val r = withReturnAgent(ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET001"),
          agentType = Some("PURCHASER"),
          name = Some("Vendor Agent"),
          houseNumber = Some("1"),
          address1 = Some("Agent address 1"),
          address2 = Some("Agent address 2"),
          address3 = Some("Agent address 3"),
          address4 = Some("Agent address 4"),
          postcode = Some("EC4A 2DQ"),
          phone = Some("0123456789"),
          email = Some("test@test.com"),
          DXAddress = None,
          reference = Some("123456")
        ))

        val result = fill(r)
        readField(result, "vendor_agentHouseNumber") mustBe Some("")
        readField(result, "vendor_agentAddressLine1") mustBe Some("")
        readField(result, "vendor_agentAddressLine2") mustBe Some("")
        readField(result, "vendor_agentAddressLine3") mustBe Some("")
        readField(result, "vendor_agentAddressLine4") mustBe Some("")
        readField(result, "vendor_agentPostcode_1") mustBe Some("")
        readField(result, "vendor_agentPostcode_2") mustBe Some("")
        readField(result, "vendor_agentEmailAddress") mustBe Some("")
        readField(result, "vendor_agentPhoneNumber") mustBe Some("")
        readField(result, "vendor_agentReference") mustBe Some("")
      }

      "must handle a completely populated return without throwing" in {
        val r = FullReturn(
          stornId           = "STORN999",
          returnResourceRef = "RRF-999",
          returnInfo        = Some(ReturnInfo(returnID = Some("RET999"))),
          submission        = Some(Submission(UTRN = Some("UTR-1234"))),
          vendor         = Some(Seq(
            Vendor(
              name = Some("Smith"),
              forename1 = Some("John"),
              forename2 = Some("Paul"),
              vendorID = Some("VEN001"),
              nextVendorID = Some("VEN002")
            ),
            Vendor(
              name = Some("Jones"),
              forename1 = Some("Jane"),
              forename2 = Some("Mary"),
              vendorID = Some("VEN002"),
              houseNumber = Some("5"),
              address1    = Some("Address 1"),
              address2    = Some("Address 2"),
              address3    = Some("Address 3"),
              address4    = Some("Address 4"),
              postcode    = Some("M1 2AB")
            ))),
          returnAgent    = Some(Seq(ReturnAgent(
            name = Some("Agent Ltd"),
            returnAgentID = Some("RA001"),
            returnID = Some("RET001"),
            agentType = Some("PURCHASER"),
            houseNumber = Some("1"),
            address1 = Some("Agent address 1"),
            address2 = Some("Agent address 2"),
            address3 = Some("Agent address 3"),
            address4 = Some("Agent address 4"),
            postcode = Some("EC4A 2DQ"),
            phone = Some("0123456789"),
            email = Some("test@test.com"),
            DXAddress = None,
            reference = Some("123456")
          )))
        )
        noException mustBe thrownBy(buildFiller().fillPdf(r))
      }
    }
  }
}