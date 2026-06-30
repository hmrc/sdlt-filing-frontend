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
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import services.pdf.PdfFormSupport.*
import services.pdf.SdltPdfFields.*
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using

@Singleton
class SdltReturnPdf1c @Inject()(
  pdfTemplateLoader: PdfTemplateLoader
) extends LoggingUtil {

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT1c.pdf")

  def fillPdf(fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf1c][fillPdf] Filling SDLT1c for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc  = use(Loader.loadPDF(templateBytes))
      val form = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res  = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf1c")

      fillPurchaserFields(writer, fullReturn)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray
    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf1c][fillPdf] Failed to fill SDLT1c PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT1c PDF", err)
      },
      identity
    )
  }

  private def fillPurchaserFields(w: PdfFieldWriter, r: FullReturn): Unit = {
    val mainPurchaserId = r.returnInfo.flatMap(_.mainPurchaserID)
    val mainPurchaser   = r.purchaser.flatMap(_.find(_.purchaserID == mainPurchaserId))
    val purchaserAgent  = r.returnAgent.flatMap(_.find(_.agentType.contains(AgentType.Purchaser.toString)))

    w.text(PURCHASER_NUMBER_PURCHASERS, Some(r.purchaser.getOrElse(Seq.empty).size.toString))

    mainPurchaser.foreach { p =>
      val isCompany = p.isCompany.exists(_.equalsIgnoreCase("YES"))

      val (n1, n2, n3, n4, n5) = splitNino(p.nino)
      w.text(PURCHASER_NINO_1, n1)
      w.text(PURCHASER_NINO_2, n2)
      w.text(PURCHASER_NINO_3, n3)
      w.text(PURCHASER_NINO_4, n4)
      w.text(PURCHASER_NINO_5, n5)

      w.dateStr(PURCHASER_DOB_DAY, PURCHASER_DOB_MONTH, PURCHASER_DOB_YEAR, p.dateOfBirth)

      w.text(PURCHASER_VAT_REFERENCE, r.companyDetails.flatMap(_.VATReference))

      w.text(PURCHASER_COMPANY_UTR,       r.companyDetails.flatMap(_.UTR))
      w.text(PURCHASER_REGISTERED_NUMBER, p.registrationNumber)
      w.text(PURCHASER_REGISTERED_PLACE,  p.placeOfRegistration)

      w.text(PURCHASER_TITLE, if (isCompany) None else p.title)

      w.text(PURCHASER_COMPANY_NAME, if (isCompany) p.companyName else p.surname)

      w.text(PURCHASER_FORENAME_1, if (isCompany) None else p.forename1)
      w.text(PURCHASER_FORENAME_2, if (isCompany) None else p.forename2)

      w.postcode(PURCHASER_POSTCODE_1, PURCHASER_POSTCODE_2, p.postcode)
      w.text(PURCHASER_HOUSE_NUMBER,   p.houseNumber)
      w.text(PURCHASER_ADDRESS_LINE_1, p.address1)
      w.text(PURCHASER_ADDRESS_LINE_2, p.address2)
      w.text(PURCHASER_ADDRESS_LINE_3, p.address3)
      w.text(PURCHASER_ADDRESS_LINE_4, p.address4)

      w.yesNo(PURCHASER_ACTING_TRUSTEE_YES, PURCHASER_ACTING_TRUSTEE_NO,
        p.isTrustee.map(_.equalsIgnoreCase("YES")))

      w.text(PURCHASER_DAYTIME_PHONE_NUMBER, p.phone)

      w.yesNo(PURCHASER_CONNECTED_TO_VENDOR_YES, PURCHASER_CONNECTED_TO_VENDOR_NO,
        p.isConnectedToVendor.map(_.equalsIgnoreCase("YES")))

    }

    w.check(LAND_CERTIFICATE_ADDRESS_PROPERTY)
    w.uncheck(LAND_CERTIFICATE_ADDRESS_PURCHASER)
    w.uncheck(LAND_CERTIFICATE_ADDRESS_AGENT)

    w.yesNo(PURCHASER_AGENT_AUTHORISED_YES, PURCHASER_AGENT_AUTHORISED_NO,
      purchaserAgent.flatMap(_.isAuthorised).map(_.equalsIgnoreCase("YES")))

    w.text(PURCHASER_AGENT_NAME, purchaserAgent.flatMap(_.name))
  }

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}
