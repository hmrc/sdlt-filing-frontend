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
import models.taxCalculation.*
import play.api.i18n.Messages
import utils.DateTimeFormats.{LocalDateFormatting, parseDate}

object EffectiveDateHelper {

  def getEffectiveDate(userAnswers: UserAnswers)(implicit messages: Messages): Either[BuildRequestError, String] = {
    for {
      fullReturn <- userAnswers.fullReturn.toRight(MissingFullReturnError)
      transaction <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      effectiveDateRaw <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      parsedEffectiveDate <- parseDate(effectiveDateRaw).map(_.toLongDate).left.map(_ => InvalidDateError(effectiveDateRaw))
    } yield
      parsedEffectiveDate
  }

}
