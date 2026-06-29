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

import models.{FullReturn, Purchaser}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import services.pdf.PdfFormSupport.{CUSTOM_FONT_NAME, CUSTOM_FONT_RESOURCE_PATH}
import utils.LoggingUtil
import services.pdf.SdltPdfFields.*

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using

@Singleton
class SdltReturnPdf2Purchaser @Inject()(
                                         pdfTemplateLoader: PdfTemplateLoader
                                       ) extends LoggingUtil {
  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT2purchaser.pdf")

  def fillPdf(purchaser: Purchaser, fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf2Purchaser][fillPdf] Filling SDLT2 Purchaser for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc = use(Loader.loadPDF(templateBytes))
      val form = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf2Purchaser")
      
      fillPurchaser(writer, purchaser)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray
    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf2Purchaser][fillPdf] Failed to fill SDLT2 Purchaser PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT2purchaser PDF", err)
      },
      identity
    )
  }
  
  private def fillPurchaser(w: PdfFieldWriter, purchaser: Purchaser): Unit = {
    w.check(IS_PURCHASER)
    w.text(PURCHASER_TITLE, purchaser.title)

    if (purchaser.isCompany.exists(_.equalsIgnoreCase("yes"))) {
      w.text(PURCHASER_COMPANY_NAME, purchaser.companyName)
    } else {
      w.text(PURCHASER_SURNAME, purchaser.surname)
      w.text(PURCHASER_FORENAME_1, purchaser.forename1)
      w.text(PURCHASER_FORENAME_2, purchaser.forename2)
    }

    w.postcode(PURCHASER_POSTCODE_1, PURCHASER_POSTCODE_2, purchaser.postcode)
    w.text(PURCHASER_HOUSE_NUMBER, purchaser.houseNumber)
    w.text(PURCHASER_ADDRESS_LINE_1, purchaser.address1)
    w.text(PURCHASER_ADDRESS_LINE_2, purchaser.address2)
    w.text(PURCHASER_ADDRESS_LINE_3, purchaser.address3)
    w.text(PURCHASER_ADDRESS_LINE_4, purchaser.address4)

    w.yesNo(
      PURCHASER_CONNECTED_TO_VENDOR_YES,
      PURCHASER_CONNECTED_TO_VENDOR_NO,
      purchaser.isConnectedToVendor.map(_.equalsIgnoreCase("YES"))
    )

    w.yesNo(
      PURCHASER_ACTING_TRUSTEE_YES,
      PURCHASER_ACTING_TRUSTEE_NO,
      purchaser.isTrustee.map(_.equalsIgnoreCase("YES"))
    )
  }

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}
