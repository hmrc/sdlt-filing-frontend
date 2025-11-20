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

package forms.preliminary

import forms.preliminary.PurchaserOrCompanyNameFormProvider
import models.prelimQuestions.PurchaserName
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError

class PurchaserOrCompanyNameFormProviderSpec extends AnyWordSpec with Matchers {

  val maxLength = 56
  val nameRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

  val cases = Table(
    ("choice", "requiredKey", "lengthKey", "invalidKey"),
    ("Individual", "purchaser.name.form.no.input.error.individual", "purchaser.name.form.maxLength.error.individual", "purchaser.surname.form.regex.error.individual"),
    ("Company",   "purchaser.name.form.no.input.error.company",    "purchaser.name.form.maxLength.error.company",    "purchaser.name.form.regex.error.company")
  )

  "PurchaserOrCompanyNameFormProvider" should {

    forAll(cases) { (choice, requiredKey, lengthKey, invalidKey) =>

      val form = new PurchaserOrCompanyNameFormProvider()(choice)

      s"bind valid data for $choice" in {
        val result = form.bind(
          Map(
            "forename1" -> "John",
            "forename2" -> "A",
            "name"      -> "Valid Co Ltd"
          )
        )

        result.errors shouldBe empty
        result.value shouldBe Some(PurchaserName(Some("John"), Some("A"), "Valid Co Ltd"))
      }

      s"fail when 'name' is empty for $choice" in {
        val result = form.bind(Map("forename1" -> "", "forename2" -> "", "name" -> ""))

        result.errors should contain(FormError("name", requiredKey))
      }

      s"fail when 'name' exceeds max length for $choice" in {
        val longName = "a" * (maxLength + 1)

        val result = form.bind(Map("forename1" -> "", "forename2" -> "", "name" -> longName))

        result.errors should contain(
          FormError("name", lengthKey, Seq(maxLength))
        )
      }

      s"fail when 'name' contains invalid characters for $choice" in {
        val result = form.bind(Map("forename1" -> "", "forename2" -> "", "name" -> "Invalid#Name"))

        result.errors should contain(
          FormError("name", invalidKey, Seq(nameRegex))
        )
      }

      s"allow optional forename fields for $choice" in {
        val result = form.bind(Map("forename1" -> "", "forename2" -> "", "name" -> "Valid"))

        result.errors shouldBe empty
        result.value shouldBe Some(PurchaserName(None, None, "Valid"))
      }
    }
  }
}
