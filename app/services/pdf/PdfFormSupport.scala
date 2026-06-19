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

import models.FullReturn
import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import services.pdf.SdltPdfFields.{IR_MARK, PRINT_STATUS, UTRN}
import utils.LoggingUtil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object PdfFormSupport {

  def splitPostcode(postcode: Option[String]): (Option[String], Option[String]) =
    postcode.map(_.trim).filter(_.nonEmpty) match {
      case None => (None, None)
      case Some(pc) =>
        val parts = pc.split(" +", 2)
        if (parts.length == 2) (Some(parts(0)), Some(parts(1))) else (Some(pc), None)
    }

  /** Split a long string at a word boundary near maxLen across two fields. */
  def splitLines(s: Option[String], maxLen: Int): (Option[String], Option[String]) =
    s.map(_.trim).filter(_.nonEmpty) match {
      case None => (None, None)
      case Some(str) =>
        if str.length <= maxLen then (Some(str), None)
        else {
          val cut = str.lastIndexOf(' ', maxLen)
          if (cut > 0) (Some(str.substring(0, cut)), Some(str.substring(cut + 1)))
          else (Some(str.substring(0, maxLen)), Some(str.substring(maxLen)))
        }
    }
  }

class PdfFieldWriter(form: PDAcroForm, ctx: String) extends LoggingUtil {

  /** Set a text field. Null/blank values silently clear the field. */
  def text(fieldName: String, value: Option[String]): Unit = {
    val safe = value.map(_.trim).getOrElse("")
    Try(form.getField(fieldName)).toOption match {
      case Some(f: PDTextField) =>
        Try(f.setValue(safe)).failed.foreach { e =>
          logger.warn(s"[$ctx][FieldWriter] Could not set '$fieldName': ${e.getMessage}")
        }
      case Some(_) =>
        logger.warn(s"[$ctx][FieldWriter] '$fieldName' is not a text field")
      case None =>
        logger.debug(s"[$ctx][FieldWriter] '$fieldName' not found in template — skipping")
    }
  }

  /** Set a Yes/No checkbox pair from a boolean. */
  def yesNo(yesField: String, noField: String, isYes: Option[Boolean]): Unit =
    isYes match {
      case Some(true) =>
        check(yesField)
        uncheck(noField)
      case Some(false) =>
        uncheck(yesField)
        check(noField)
      case None =>
        uncheck(yesField)
        uncheck(noField)
    }

  def check(fieldName: String): Unit   = setCheckbox(fieldName, checked = true)
  def uncheck(fieldName: String): Unit = setCheckbox(fieldName, checked = false)

  private def setCheckbox(fieldName: String, checked: Boolean): Unit =
    Try(form.getField(fieldName)).toOption match {
      case Some(cb: PDCheckBox) =>
        Try(if (checked) cb.check() else cb.unCheck()).failed.foreach { e =>
          logger.warn(s"[$ctx][FieldWriter] Could not set checkbox '$fieldName': ${e.getMessage}")
        }
      case Some(_) =>
        logger.warn(s"[$ctx][FieldWriter] '$fieldName' is not a checkbox")
      case None =>
        logger.debug(s"[$ctx][FieldWriter] '$fieldName' not found in template — skipping")
    }

  /**
   * Set a whole decimal field as a plain string, removing any 0s after the decimal point.
   */
  def wholeDecimal(fieldName: String, value: Option[String]): Unit =
    value.map(_.trim).filter(_.nonEmpty) match {
      case None => text(fieldName, None)
      case Some(s) =>
        val idx = s.indexOf('.')
        if (idx < 0) text(fieldName, Some(s))
        else text(fieldName, Some(s.substring(0, idx)))
    }

  /**
   * Parse a stored date string ("dd/MM/yyyy") or ("yyyy-MM-dd") and split into three fields.
   * Tolerant of null — leaves fields blank.
   */
  def dateStr(dayField: String, monthField: String, yearField: String, stored: Option[String]): Unit =
    stored.map(_.trim).filter(_.nonEmpty) match {
      case None =>
        text(dayField, None); text(monthField, None); text(yearField, None)
      case Some(d) =>
        val fmts = Seq("dd/MM/yyyy", "yyyy-MM-dd").map(DateTimeFormatter.ofPattern)
        val optDate = fmts.iterator.flatMap(f => Try(LocalDate.parse(d.trim, f)).toOption).nextOption()
        optDate match {
          case Some(date) =>
            text(dayField, Some(f"${date.getDayOfMonth}%02d"))
            text(monthField, Some(f"${date.getMonthValue}%02d"))
            text(yearField, Some(date.getYear.toString))
          case None =>
            logger.warn(s"[$ctx][FieldWriter] Could not parse date '$d'")
        }
    }

  def postcode(postcodeField1: String, postcodeField2: String, postcode: Option[String]): Unit =
    PdfFormSupport.splitPostcode(postcode) match {
      case (part1, part2) =>
        text(postcodeField1, part1)
        text(postcodeField2, part2)
    }

  def fillCommonFields(fullReturn: FullReturn): Unit =
    text(UTRN, fullReturn.submission.flatMap(_.UTRN))
    text(IR_MARK, fullReturn.submission.flatMap(_.irmarkSent))
    text(PRINT_STATUS, None)
}
