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

import org.apache.pdfbox.pdmodel.interactive.form.{PDAcroForm, PDCheckBox, PDTextField}
import utils.LoggingUtil

import java.time.LocalDate
import scala.util.Try

object PdfFormSupport {

  /** Split "SW1A 2AA" -> ("SW1A", "2AA"). Single-part postcodes go entirely into field 1. */
  def splitPostcode(postcode: String): (String, String) =
    Option(postcode).map(_.trim).filter(_.nonEmpty) match {
      case None => ("", "")
      case Some(pc) =>
        val parts = pc.split(" ", 2)
        if (parts.length == 2) (parts(0), parts(1)) else (pc, "")
    }

  /** Split a long string at a word boundary near maxLen across two fields. */
  def splitLines(s: String, maxLen: Int): (String, String) =
    if (s.length <= maxLen) (s, "")
    else {
      val cut = s.lastIndexOf(' ', maxLen)
      if (cut > 0) (s.substring(0, cut), s.substring(cut + 1))
      else (s.substring(0, maxLen), s.substring(maxLen))
    }
}

class PdfFieldWriter(form: PDAcroForm, ctx: String) extends LoggingUtil {

  /** Set a text field. Null/blank values silently clear the field. */
  def text(fieldName: String, value: String): Unit = {
    val safe = Option(value).map(_.trim).getOrElse("")
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
  def yesNo(yesField: String, noField: String, isYes: Boolean): Unit =
    if (isYes) { check(yesField); uncheck(noField) }
    else       { uncheck(yesField); check(noField) }

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
   * Set a BigDecimal money field as a plain string.
   * The PDF template renders the £ symbol and decimal separator as static content.
   */
  def bigDecimal(fieldName: String, amount: Option[BigDecimal]): Unit =
    text(fieldName, amount.map(_.setScale(2, BigDecimal.RoundingMode.DOWN).toString).orNull)

  /**
   * Parse a stored date string ("dd/MM/yyyy") and split into three fields.
   * Tolerant of null — leaves fields blank.
   */
  def dateStr(dayField: String, monthField: String, yearField: String, stored: String): Unit =
    Option(stored).map(_.trim).filter(_.nonEmpty) match {
      case None =>
        text(dayField, ""); text(monthField, ""); text(yearField, "")
      case Some(d) =>
        Try {
          val fmt  = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
          val date = LocalDate.parse(d, fmt)
          text(dayField,   f"${date.getDayOfMonth}%02d")
          text(monthField, f"${date.getMonthValue}%02d")
          text(yearField,   date.getYear.toString)
        }.failed.foreach { e =>
          logger.warn(s"[$ctx][FieldWriter] Could not parse date '$d': ${e.getMessage}")
          text(dayField, d); text(monthField, ""); text(yearField, "")
        }
    }
}