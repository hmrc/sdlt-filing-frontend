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

package forms.land

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class LandTitleNumberFormProvider @Inject() extends Mappings {

  private val characterRegex =
    """^[A-Za-z0-9 ~!@%&'()*+,\-./:=?\[\]^_{};\\]*$"""

  def apply(): Form[String] =
    Form(
      "value" -> text("land.titleNumber.error.required")
        .verifying(regexp(characterRegex, "land.titleNumber.error.invalid"))
        .verifying(maxLength(14, "land.titleNumber.error.length"))
    )
}
