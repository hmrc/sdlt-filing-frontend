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

import models.*
import models.land.LandSelectMeasurementUnit
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
class SdltReturnPdf3 @Inject()(
                                 pdfTemplateLoader: PdfTemplateLoader
                               ) extends LoggingUtil {

  private lazy val templateBytes: Array[Byte] = pdfTemplateLoader.load("SDLT3.pdf")

  def fillPdf(fullReturn: FullReturn, land: Land, flatten: Boolean = true): Array[Byte] = {
    logger.info(s"[SdltReturnPdf3][fillPdf] Filling SDLT3 for returnID: ${fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")}")

    Using.Manager { use =>
      val doc    = use(Loader.loadPDF(templateBytes))
      val form   = getAcroForm(doc)
      val customFontResourceInputStream = getClass.getResourceAsStream(CUSTOM_FONT_RESOURCE_PATH)
      val res = form.getDefaultResources
      val customFont = PDTrueTypeFont.load(doc, customFontResourceInputStream, WinAnsiEncoding.INSTANCE)
      res.put(COSName.getPDFName(CUSTOM_FONT_NAME), customFont)
      form.setDefaultResources(res)
      val writer = new PdfFieldWriter(form, "SdltReturnPdf3")

      fillLandFields(writer, land)
      fillCommonFields(writer, fullReturn)

      if (flatten) form.flatten()

      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray

    }.fold(
      err => {
        logger.error(s"[SdltReturnPdf3][fillPdf] Failed to fill SDLT3 PDF", err)
        throw new SdltPdfFillException("Failed to fill SDLT3 PDF", err)
      },
      identity
    )
  }

  private def fillLandFields(w: PdfFieldWriter, land: Land): Unit = {
    w.text(LAND_TYPE_PROPERTY, land.propertyType)
    w.text(LAND_LOCAL_AUTHORITY_NUMBER, land.localAuthorityNumber)
    w.text(LAND_TITLE_NUMBER, land.titleNumber)
    w.text(LAND_NLPG_UPRN, land.NLPGUPRN)
    w.postcode(LAND_POSTCODE_1, LAND_POSTCODE_2, land.postcode)
    w.text(LAND_HOUSE_NUMBER,  land.houseNumber)
    w.text(LAND_ADDRESS_LINE1, land.address1)
    w.text(LAND_ADDRESS_LINE2, land.address2)
    w.text(LAND_ADDRESS_LINE3, land.address3)
    w.text(LAND_ADDRESS_LINE4, land.address4)
    land.areaUnit match {
      case Some(LandSelectMeasurementUnit.Hectares.toString) =>
        w.check(LAND_AREA_TYPE_HECTARES)
        w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
      case Some(LandSelectMeasurementUnit.Sqms.toString) =>
        w.check(LAND_AREA_TYPE_SQUARE_METRES)
        w.uncheck(LAND_AREA_TYPE_HECTARES)
      case _ =>
        w.uncheck(LAND_AREA_TYPE_HECTARES)
        w.uncheck(LAND_AREA_TYPE_SQUARE_METRES)
    }
    fillLandArea(w, land.landArea)
    w.text(LAND_MINERAL_RIGHTS, land.mineralRights)
    w.yesNo(
      LAND_PLAN_ATTACHED_YES,
      LAND_PLAN_ATTACHED_NO,
      land.willSendPlanByPost.map(_.equalsIgnoreCase("YES"))
    )
    w.text(LAND_ESTATE_OR_INTEREST_TRANSFERRED, land.interestCreatedTransferred.map(_.substring(0, 2)))
  }

  private def fillLandArea(w: PdfFieldWriter, landArea: Option[String]): Unit =
    val areaSplit = landArea.map(_.trim).filter(_.nonEmpty) match {
      case None =>
        (None, None)
      case Some(s) =>
        val idx = s.indexOf('.')
        if (idx < 0) {
          (Some(s), None)
        } else {
          (Some(s.substring(0, idx)), Some(s.substring(idx + 1)))
        }
    }
    w.text(LAND_AREA, areaSplit._1)
    w.text(LAND_AREA_DECIMAL, areaSplit._2)

  private def fillCommonFields(w: PdfFieldWriter, r: FullReturn): Unit =
    w.fillCommonFields(r)

  private def getAcroForm(doc: PDDocument): PDAcroForm =
    Option(doc.getDocumentCatalog.getAcroForm)
      .getOrElse(throw new SdltPdfFillException("PDF template has no AcroForm", null))
}