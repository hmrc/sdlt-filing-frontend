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

package forms.purchaser

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class ConfirmNameOfThePurchaserFormProviderSpec extends BooleanFieldBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val purchaserName = "John Smith"

  val companyForm = new ConfirmNameOfThePurchaserFormProvider()(purchaserName, isCompany = true)
  val individualForm = new ConfirmNameOfThePurchaserFormProvider()(purchaserName, isCompany = false)

  ".value - Company Branch" - {

    val fieldName = "value"
    val requiredKey = "purchaser.confirmNameOfPurchaser.error.required.company"

    behave like booleanField(
      companyForm,
      fieldName,
      invalidError = FormError(fieldName, "error.boolean")
    )

    behave like mandatoryField(
      companyForm,
      fieldName,
      requiredError = FormError(fieldName, messages(requiredKey, purchaserName))
    )

    "must bind true for company" in {
      val result = companyForm.bind(Map("value" -> "true"))
      result.value mustBe Some(true)
    }

    "must bind false for company" in {
      val result = companyForm.bind(Map("value" -> "false"))
      result.value mustBe Some(false)
    }

    "must fail to bind empty value for company" in {
      val result = companyForm.bind(Map("value" -> ""))
      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
    }

    "must fail to bind invalid value for company" in {
      val result = companyForm.bind(Map("value" -> "invalid"))
      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
    }

    "must unbind true for company" in {
      val filledForm = companyForm.fill(true)
      filledForm("value").value mustBe Some("true")
    }

    "must unbind false for company" in {
      val filledForm = companyForm.fill(false)
      filledForm("value").value mustBe Some("false")
    }
  }

  ".value - Individual Branch" - {

    val fieldName = "value"
    val requiredKey = "purchaser.confirmNameOfPurchaser.error.required.individual"

    behave like booleanField(
      individualForm,
      fieldName,
      invalidError = FormError(fieldName, "error.boolean")
    )

    behave like mandatoryField(
      individualForm,
      fieldName,
      requiredError = FormError(fieldName, messages(requiredKey, purchaserName))
    )

    "must bind true for individual" in {
      val result = individualForm.bind(Map("value" -> "true"))
      result.value mustBe Some(true)
    }

    "must bind false for individual" in {
      val result = individualForm.bind(Map("value" -> "false"))
      result.value mustBe Some(false)
    }

    "must fail to bind empty value for individual" in {
      val result = individualForm.bind(Map("value" -> ""))
      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
    }

    "must fail to bind invalid value for individual" in {
      val result = individualForm.bind(Map("value" -> "invalid"))
      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
    }

    "must unbind true for individual" in {
      val filledForm = individualForm.fill(true)
      filledForm("value").value mustBe Some("true")
    }

    "must unbind false for individual" in {
      val filledForm = individualForm.fill(false)
      filledForm("value").value mustBe Some("false")
    }
  }
}