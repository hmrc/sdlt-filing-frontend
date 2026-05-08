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

package utils

import play.api.i18n.{Lang, Messages}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.util.Try

case object UnparseableDateError

object DateTimeFormats {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private val localisedDateTimeFormatters = Map(
    "en" -> dateTimeFormatter,
    "cy" -> dateTimeFormatter.withLocale(new Locale("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter = {
    localisedDateTimeFormatters.getOrElse(lang.code, dateTimeFormatter)
  }

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

  private val UK_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def parseDate(s: String): Either[UnparseableDateError.type, LocalDate] = {
    Try(LocalDate.parse(s)).toOption
      .orElse(Try(LocalDate.parse(s, UK_DATE)).toOption)
      .toRight(UnparseableDateError)
  }
  
  implicit class LocalDateFormatting(date: LocalDate) {
    def toLongDate(implicit messages: Messages): String = {
      implicit val lang: Lang = messages.lang
      date.format(dateTimeFormat())
    }
  }
}
