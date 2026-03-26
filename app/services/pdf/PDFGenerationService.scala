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
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.{MemoryUsageSetting, RandomAccessReadBuffer, RandomAccessStreamCache}
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.{PDDocument, PDPageContentStream}
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import play.api.Environment
import utils.LoggingUtil

import java.io.ByteArrayOutputStream
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Using}

@Singleton
class ClasspathPdfTemplateLoader @Inject()(env: Environment) extends PdfTemplateLoader {
  def load(filename: String): Array[Byte] =
    env.resourceAsStream(s"pdf/$filename")
      .map { stream =>
        try stream.readAllBytes()
        finally stream.close()
      }
      .getOrElse(throw new IllegalStateException(s"PDF template not found on classpath: pdf/$filename"))
}

@Singleton
class PDFGenerationService @Inject()(
                                      pdf1aFiller:     SdltReturnPdf1a,
//                                      pdf2PurchFiller: SdltReturnPdf2Purchaser,
//                                      pdf2VendFiller:  SdltReturnPdf2Vendor,
//                                      pdf3Filler:      SdltReturnPdf3,
//                                      pdf4Filler:      SdltReturnPdf4,
//                                      pdf4aFiller:     SdltReturnPdf4a
                                    ) extends LoggingUtil {

  private val MaxMemBytes        = 10 * 1024 * 1024L
  private val PageNumberFontSize = 9f
  private val PageNumberX        = 520f
  private val PageNumberY        = 5f

  private val streamCacheFunction: RandomAccessStreamCache.StreamCacheCreateFunction =
    MemoryUsageSetting.setupMixed(MaxMemBytes).streamCache

  def generatePdf(fullReturn: FullReturn)(implicit ec: ExecutionContext): Future[Array[Byte]] =
    Future {
      val returnId = fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")
      logger.info(s"[PDFGenerationService][generatePdf] Generating PDF for returnID: $returnId")
      val pdf = mergePdfs(collectPdfParts(fullReturn))
      logger.info(s"[PDFGenerationService][generatePdf] PDF generated successfully, size: ${pdf.length} bytes")
      pdf
    }.recover {
      case ex =>
        val returnId = fullReturn.returnInfo.flatMap(_.returnID).getOrElse("unknown")
        logger.error(s"[PDFGenerationService][generatePdf] Failed to generate PDF for returnID: $returnId", ex)
        throw ex
    }
  

  private def collectPdfParts(r: FullReturn): Vector[Array[Byte]] = {
    var parts = Vector.empty[Array[Byte]]

//    val purchasers = r.purchaser.getOrElse(Seq.empty)
//    val vendors    = r.vendor.getOrElse(Seq.empty)
//    val lands      = r.land.getOrElse(Seq.empty)
//    val isLease    = r.lease.isDefined

    // ---- SDLT1: always present ----
    parts :+= pdf1aFiller.fillPdf(r)

//    // ---- SDLT2: one per purchaser beyond the first two ----
//    if (purchasers.size > 2) {
//      purchasers.drop(2).zipWithIndex.foreach { case (purchaser, idx) =>
//        tryFill(s"SDLT2 purchaser index ${idx + 2}") {
//          parts :+= pdf2PurchFiller.fillPdf(purchaser, r)
//        }
//      }
//    }
//
//    // ---- SDLT2: one per vendor beyond the first two ----
//    if (vendors.size > 2) {
//      vendors.drop(2).zipWithIndex.foreach { case (vendor, idx) =>
//        tryFill(s"SDLT2 vendor index ${idx + 2}") {
//          parts :+= pdf2VendFiller.fillPdf(vendor, r)
//        }
//      }
//    }

//    // ---- SDLT3: one per additional land parcel (index 0 is on SDLT1 itself) ----
//    if (lands.size > 1) {
//      lands.tail.zipWithIndex.foreach { case (land, idx) =>
//        tryFill(s"SDLT3 land index ${idx + 1}") {
//          parts :+= pdf3Filler.fillPdf(land, r)
//        }
//      }
//    }
//
//    // ---- SDLT4 / SDLT4a: conditioned on lease type and land count ----
//    val propertyType = r.land.flatMap(_.headOption).flatMap(_.propertyType).getOrElse("")
//    val needsSdlt4   = Seq("02", "03").contains(propertyType) || isLease
//
//    if (needsSdlt4) {
//      if (!isLease) {
//        tryFill("SDLT4a (non-lease)") {
//          parts :+= pdf4aFiller.fillPdf(r)
//        }
//      } else if (lands.size > 1) {
//        lands.tail.zipWithIndex.foreach { case (land, idx) =>
//          tryFill(s"SDLT4 land index ${idx + 1} (lease, multi-land)") {
//            parts :+= pdf4Filler.fillPdf(land, r, firstTimeThrough = idx == 0)
//          }
//        }
//      } else {
//        tryFill("SDLT4a (lease, single land)") {
//          parts :+= pdf4aFiller.fillPdf(r)
//        }
//      }
//    }

    logger.info(s"[PDFGenerationService][collectPdfParts] Collected ${parts.length} PDF part(s)")
    parts
  }


//  private def tryFill(description: String)(block: => Unit): Unit =
//    try {
//      logger.debug(s"[PDFGenerationService][collectPdfParts] Adding $description")
//      block
//    } catch {
//      case _: NotImplementedError =>
//        logger.warn(s"[PDFGenerationService][collectPdfParts] Skipping $description — not yet implemented")
//    }

  private def mergePdfs(parts: Vector[Array[Byte]]): Array[Byte] = {
    val merger    = new PDFMergerUtility()
    val mergedOut = new ByteArrayOutputStream(4096)

    parts.foreach(p => merger.addSource(new RandomAccessReadBuffer(p)))
    merger.setDestinationStream(mergedOut)

    Try(merger.mergeDocuments(streamCacheFunction)).fold(
      err => {
        logger.error("[PDFGenerationService][mergePdfs] Failed to merge PDF parts", err)
        throw new SdltPdfGenerationException("Failed to merge SDLT PDF parts", err)
      },
      _ => ()
    )

    Using(Loader.loadPDF(mergedOut.toByteArray)) { doc =>
      stampPageNumbers(doc)
      stripMetadata(doc)
      val out = new ByteArrayOutputStream()
      doc.save(out)
      out.toByteArray
    }.fold(
      err => {
        logger.error("[PDFGenerationService][mergePdfs] Failed to post-process merged PDF", err)
        throw new SdltPdfGenerationException("Failed to post-process merged PDF", err)
      },
      identity
    )
  }

  private def stampPageNumbers(doc: PDDocument): Unit = {
    val totalPages = doc.getNumberOfPages
    for (i <- 0 until totalPages) {
      Using(new PDPageContentStream(doc, doc.getPage(i), AppendMode.APPEND, true, true)) { cs =>
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), PageNumberFontSize)
        cs.beginText()
        cs.newLineAtOffset(PageNumberX, PageNumberY)
        cs.showText(s"${i + 1} of $totalPages")
        cs.endText()
      }.failed.foreach { err =>
        logger.warn(s"[PDFGenerationService][stampPageNumbers] Could not stamp page ${i + 1}", err)
      }
    }
  }

  private def stripMetadata(doc: PDDocument): Unit = {
    val info = doc.getDocumentInformation
    info.setAuthor(null)
    info.setCreationDate(null)
    info.setModificationDate(null)
  }
}

class SdltPdfGenerationException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)