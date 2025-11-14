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

import forms.behaviours.StringFieldBehaviours
import forms.preliminary.PurchaserSurnameOrCompanyNameFormProvider
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.FormError
import org.scalatest.prop.TableDrivenPropertyChecks._

class PurchaserSurnameOrCompanyNameFormProviderSpec extends AnyWordSpec with Matchers {

  val maxLength = 56
  val fieldName = "purchaserSurnameOrCompanyName"

  val cases = Table(
    ("choice", "requiredKey", "lengthKey", "invalidKey"),
    ("Individual", "purchaser.name.form.no.input.error.individual", "purchaser.name.form.maxLength.error.individual", "purchaser.name.form.regex.error.individual"),
    ("Business", "purchaser.name.form.no.input.error.business", "purchaser.name.form.maxLength.error.business", "purchaser.name.form.regex.error.business")
  )

  "PProvider form" should {
    forAll(cases) { (choice, requiredKey, lengthKey, invalidKey) =>
      val form = new PurchaserSurnameOrCompanyNameFormProvider()(choice)

      s"bind valid data for $choice" in {
        val validNames = Seq("Mr test", "Business test name", "Business@business.com", "(555) 123-4567")
        validNames.foreach { name =>
          val result = form.bind(Map(fieldName -> name))
          result.errors shouldBe empty
        }
      }

      s"fail when empty for $choice" in {
        val result = form.bind(Map(fieldName -> ""))
        result.errors.map(_.message) should contain(requiredKey)
      }

      s"fail when too long for $choice" in {
        val longName = "a" * (maxLength + 1)
        val result = form.bind(Map(fieldName -> longName))
        result.errors.map(_.message) should contain(lengthKey)
      }

      s"fail when invalid characters for $choice" in {
        val invalidName = "Invalid#Name"
        val result = form.bind(Map(fieldName -> invalidName))
        result.errors.map(_.message) should contain(invalidKey)
      }
    }
  }
}
