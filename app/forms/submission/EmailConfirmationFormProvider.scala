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

package forms.submission

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class EmailConfirmationFormProvider @Inject() extends Mappings {

  private val emailInvalidCharsRegex = "^[^|<>\"'`]+$"
  private val emailFormatRegex = "[^@|`'<>\"|`]+@[^@|`'<>\"]+"

  
  def apply(): Form[String] =
    Form(
      "value" -> text("submission.emailConfirmation.error.required")
        .verifying(firstError(
          maxLength(36, "submission.emailConfirmation.error.length"),
          regexp(emailInvalidCharsRegex, "submission.emailConfirmation.error.invalid"),
          regexp(emailFormatRegex, "submission.emailConfirmation.error.invalidFormat"),
        ))
    )
}
