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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.util.Using
import SdltPdfFields.*
import PdfFormSupport.*
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding

@Singleton

class SdltReturnPdf1d @Inject()(
                                 pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT1d.pdf")

  def fillPdf(additionalPurchaser: Option[Purchaser], fullReturn: FullReturn, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf1d][fillPdf] Filling SDLT1 for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc = use(Loader.loadPDF(templateBytes))
      val form = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf1d")

      fillPurchaserFields(writer, additionalPurchaser, fullReturn)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray

    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf1d][fillPdf] Failed to fill SDLT1 PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT1 PDF", err)
      },
      identity
    )
  }

  private def fillPurchaserFields(w: PdfFieldWriter, additionalPurchaser: Option[Purchaser], r: FullReturn): Unit = {
    val purchasers = r.purchaser.getOrElse(Seq.empty)
    val purchaserAgent = r.returnAgent.flatMap(_.find(_.agentType.contains(AgentType.Purchaser.toString)))
    // val sameAddress = sameAddressCheck(purchaser1, additionalPurchaser2) // TODO : make changes once receive pdf field name of Purchaser sameAddress check box
    val vendors = r.vendor.getOrElse(Seq.empty)
    val lands = r.land.getOrElse(Seq.empty)
    val sdlt2Count = (purchasers.size - 2).max(0) + (vendors.size - 2).max(0)
    val sdlt3Count = (lands.size - 1).max(0)
    val sdlt4Count = captureSDLT4Count(r)

    w.postcode(PURCHASER_AGENT_POSTCODE_1, PURCHASER_AGENT_POSTCODE_2, purchaserAgent.flatMap(_.postcode))
    w.text(PURCHASER_AGENT_HOUSE_NUMBER, purchaserAgent.flatMap(_.houseNumber))
    w.text(PURCHASER_AGENT_ADDRESS_LINE1, purchaserAgent.flatMap(_.address1))
    w.text(PURCHASER_AGENT_ADDRESS_LINE2, purchaserAgent.flatMap(_.address2))
    w.text(PURCHASER_AGENT_ADDRESS_LINE3, purchaserAgent.flatMap(_.address3))
    w.text(PURCHASER_AGENT_ADDRESS_LINE4, purchaserAgent.flatMap(_.address4))
    w.text(PURCHASER_AGENT_DX_NUMBER, purchaserAgent.flatMap(_.DXAddress))
    w.text(PURCHASER_AGENT_REFERENCE, purchaserAgent.flatMap(_.reference))
    w.text(PURCHASER_AGENT_PHONE_NUMBER, purchaserAgent.flatMap(_.phone))

    additionalPurchaser.foreach { purchaser =>
      w.text(PURCHASER_TITLE, purchaser.title)
      if (purchaser.isCompany.exists(_.equalsIgnoreCase("yes"))) {
        w.text(PURCHASER_COMPANY_NAME, purchaser.companyName)
      } else {
        w.text(PURCHASER_SURNAME, purchaser.surname)
        w.text(PURCHASER_FORENAME_1, purchaser.forename1)
        w.text(PURCHASER_FORENAME_2, purchaser.forename2)
      }

      /* if (!sameAddress) { // TODO : make changes once receive pdf field name of Purchaser sameAddress check box
        w.text(PURCHASER2_SAMEADDRESS, Some("X"))
      } else {
        w.postcode(PURCHASER_POSTCODE_1, PURCHASER_POSTCODE_2, purchaser.postcode)
        w.text(PURCHASER_HOUSE_NUMBER, purchaser.houseNumber)
        w.text(PURCHASER_ADDRESS_LINE1, purchaser.address1)
        w.text(PURCHASER_ADDRESS_LINE2, purchaser.address2)
        w.text(PURCHASER_ADDRESS_LINE3, purchaser.address3)
        w.text(PURCHASER_ADDRESS_LINE4, purchaser.address4)
      } */

      w.postcode(PURCHASER_POSTCODE_1, PURCHASER_POSTCODE_2, purchaser.postcode)
      w.text(PURCHASER_HOUSE_NUMBER, purchaser.houseNumber)
      w.text(PURCHASER_ADDRESS_LINE1, purchaser.address1)
      w.text(PURCHASER_ADDRESS_LINE2, purchaser.address2)
      w.text(PURCHASER_ADDRESS_LINE3, purchaser.address3)
      w.text(PURCHASER_ADDRESS_LINE4, purchaser.address4)

      w.yesNo(
        PURCHASER_ACTING_TRUSTEE_YES,
        PURCHASER_ACTING_TRUSTEE_NO,
        purchaser.isTrustee.map(_.equalsIgnoreCase("YES"))
      )
    }
    w.text(PURCHASER_ADDITIONALDETAILS_SDLT2, Some(sdlt2Count.toString))
    w.text(LAND_ADDITIONALDETAILS_SDLT3, Some(sdlt3Count.toString))
    w.text(LEASE_ADDITIONALDETAILS_SDLT4, Some(sdlt4Count.toString))
  }

  /* private def sameAddressCheck(p1: Option[Purchaser], p2: Option[Purchaser]): Boolean = { // TODO : make changes once receive pdf field name of Purchaser sameAddress check box
     p1.flatMap(_.houseNumber) == p2.flatMap(_.houseNumber) &&
       p1.flatMap(_.address1) == p2.flatMap(_.address1) &&
       p1.flatMap(_.address2) == p2.flatMap(_.address2) &&
       p1.flatMap(_.address3) == p2.flatMap(_.address3) &&
       p1.flatMap(_.address4) == p2.flatMap(_.address4) &&
       p1.flatMap(_.postcode) == p2.flatMap(_.postcode)
   } */

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.text(UTRN, r.submission.flatMap(_.UTRN))

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))

  private def captureSDLT4Count(r: FullReturn) = {
    val lands = r.land.getOrElse(Seq.empty)
    val purchasers = r.purchaser.getOrElse(Seq.empty)
    val mainPurchaser = purchasers.headOption

    val purchaserQuestionAnswered = mainPurchaser.exists(t => t.registrationNumber.isDefined && t.placeOfRegistration.isDefined)

    val mainLand = lands.headOption
    val landPropertyType = mainLand.flatMap(_.propertyType)
    val landQuestionAnswered = landPropertyType.exists(Set("02", "03"))

    val transactionIncludeQuestionAnswered = r.transaction.flatMap(_.includesStock).isDefined || r.transaction.flatMap(_.includesGoodwill).isDefined ||
      r.transaction.flatMap(_.includesOther).isDefined || r.transaction.flatMap(_.includesChattel).isDefined

    val transactionDeferementQuestionAnswered = r.transaction.flatMap(_.agreedToDeferPayment).isDefined || r.transaction.flatMap(_.isDependantOnFutureEvent).isDefined

    val mainPurchaserId: Option[String] = r.returnInfo.flatMap(_.mainPurchaserID)
    val companyDetails = r.companyDetails.filter(_.purchaserID == mainPurchaserId)

    val companyDetailsQuestionAnswered =
      companyDetails.exists(cd =>
        Seq(
          cd.companyTypeBank,
          cd.companyTypeBuilder,
          cd.companyTypeBuildsoc,
          cd.companyTypeCentgov,
          cd.companyTypeIndividual,
          cd.companyTypeInsurance,
          cd.companyTypeLocalauth,
          cd.companyTypeOthercharity,
          cd.companyTypeOthercompany,
          cd.companyTypeOtherfinancial,
          cd.companyTypePartnership,
          cd.companyTypeProperty,
          cd.companyTypePubliccorp,
          cd.companyTypeSoletrader,
          cd.companyTypePensionfund
        ).exists(_.contains("yes"))
      )

    (lands.size > 1, purchaserQuestionAnswered, landQuestionAnswered,
      transactionIncludeQuestionAnswered, transactionDeferementQuestionAnswered, companyDetailsQuestionAnswered) match {
      case (true, _, _, _, _, _) => lands.size - 1
      case (false, false, false, false, false, false) => 0
      case (false ,_ , _, _, _, _) => 1
      case _ => 0
    }

  }

}
