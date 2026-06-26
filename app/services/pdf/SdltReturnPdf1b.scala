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

import models.*

import javax.inject.{Inject, Singleton}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import utils.LoggingUtil
import PdfFormSupport.*
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding
import org.apache.pdfbox.cos.COSName

import scala.util.Using
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import services.pdf.SdltPdfFields.*

import java.io.ByteArrayOutputStream

@Singleton
class SdltReturnPdf1b @Inject()(
                                  pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT1b.pdf")
  
  def fillPdf(fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf1b[fillPdf] Filling SDLT1 for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")
    
    Using.Manager { use =>
      val doc = use(Loader.loadPDF(templateBytes))
      val form = getAcroForm(doc)

      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf1b")

      fillVendorFields(writer, fullReturn)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray
    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf1b][fillPdf] Failed to fill SDLT1 PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT1 PDF", err)
      },
      identity
    )
  }

  private def fillVendorFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainVendorId: Option[String] = r.returnInfo.flatMap(_.mainVendorID)
    val mainVendor = r.vendor.flatMap(_.find(_.vendorID == mainVendorId))
    val secondVendorId = mainVendor.flatMap(_.nextVendorID)
    val secondVendor = secondVendorId.flatMap(id =>
      r.vendor.flatMap(_.find(_.vendorID.contains(id)))
    )
    val vendorAgent = r.returnAgent.flatMap(_.find(_.agentType.contains(AgentType.Vendor.toString)))

    w.postcode(VENDOR_AGENT_POSTCODE_1, VENDOR_AGENT_POSTCODE_2, vendorAgent.flatMap(_.postcode))
    w.text(VENDOR_AGENT_HOUSE_NUMBER, vendorAgent.flatMap(_.houseNumber))
    w.text(VENDOR_AGENT_ADDRESS_LINE1, vendorAgent.flatMap(_.address1))
    w.text(VENDOR_AGENT_ADDRESS_LINE2, vendorAgent.flatMap(_.address2))
    w.text(VENDOR_AGENT_ADDRESS_LINE3, vendorAgent.flatMap(_.address3))
    w.text(VENDOR_AGENT_ADDRESS_LINE4, vendorAgent.flatMap(_.address4))
    
    w.text(VENDOR_AGENT_EMAIL_ADDRESS, vendorAgent.flatMap(_.email))
    w.text(VENDOR_AGENT_REFERENCE, vendorAgent.flatMap(_.reference))
    w.text(VENDOR_AGENT_PHONE_NUMBER, vendorAgent.flatMap(_.phone))
        
    secondVendor.foreach { vendor =>
      w.text(VENDOR_NAME, vendor.name)
      w.text(VENDOR_FORENAME_1, vendor.forename1)
      w.text(VENDOR_FORENAME_2, vendor.forename2)

      w.postcode(VENDOR_POSTCODE_1, VENDOR_POSTCODE_2, vendor.postcode)
      w.text(VENDOR_HOUSE_NUMBER, vendor.houseNumber)
      w.text(VENDOR_ADDRESS_LINE1, vendor.address1)
      w.text(VENDOR_ADDRESS_LINE2, vendor.address2)
      w.text(VENDOR_ADDRESS_LINE3, vendor.address3)
      w.text(VENDOR_ADDRESS_LINE4, vendor.address4)
    }
  }

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}

