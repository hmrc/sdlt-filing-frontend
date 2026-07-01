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

import models.{FullReturn, Vendor}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import services.pdf.PdfFormSupport.{CUSTOM_FONT_NAME, CUSTOM_FONT_RESOURCE_PATH}
import services.pdf.SdltPdfFields.*
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using

@Singleton
class SdltReturnPdf2Vendor @Inject()(
                                         pdfTemplateLoader: PdfTemplateLoader
                                       ) extends LoggingUtil {
  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT2vendor.pdf")

  def fillPdf(vendor: Vendor, fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf2Vendor][fillPdf] Filling SDLT2 Vendor for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc = use(Loader.loadPDF(templateBytes))
      val form = getAcroForm(doc)
      form.getFields.forEach { field =>
        println(field.getFullyQualifiedName)
      }
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf2Vendor")
      
      fillVendor(writer, vendor)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray
    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf2Vendor][fillPdf] Failed to fill SDLT2 Vendor PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT2vendor PDF", err)
      },
      identity
    )
  }
  
  private def fillVendor(w: PdfFieldWriter, vendor: Vendor): Unit = {
    w.check(IS_VENDOR)
    w.text(VENDOR_TITLE, vendor.title)

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

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}
