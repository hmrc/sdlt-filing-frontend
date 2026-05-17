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

package utils

import models.UserAnswers
import models.taxCalculation.{BuildRequestError, InvalidDateError, MissingAboutTheTransactionError, MissingFullReturnError, MissingTransactionAnswerError}
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateTimeFormat, parseDate}

import java.time.LocalDate

object EffectiveDateHelper {

   def getEffectiveDate(userAnswers: UserAnswers): Either[BuildRequestError, String] = {
    for {
      fullReturn <- userAnswers.fullReturn.toRight(MissingFullReturnError)
      transaction <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      effectiveDateRaw <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      parsedEffectiveDate <- parseDate(effectiveDateRaw).left.map(_ => InvalidDateError(effectiveDateRaw))
      formattedEffectiveDate = effectiveDateFormatter(parsedEffectiveDate)
    } yield
      formattedEffectiveDate
  }
   
   private def effectiveDateFormatter(effectiveDate:LocalDate):String = {
      val formatter = dateTimeFormat()(Lang("en"))
      val formattedDate = effectiveDate.format(formatter)
      formattedDate
   }


}
