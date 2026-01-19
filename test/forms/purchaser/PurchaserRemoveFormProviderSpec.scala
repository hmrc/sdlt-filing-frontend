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

package forms.purchaser

import forms.behaviours.StringFieldBehaviours
import models.purchaser.PurchaserRemove
import play.api.data.FormError

class PurchaserRemoveFormProviderSpec extends StringFieldBehaviours {

  val form = new PurchaserRemoveFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "purchaser.purchaserRemove.error.required"

    "must bind valid values" - {

      "when 'no' is selected" in {
        val result = form.bind(Map(fieldName -> "no"))
        result.value.value mustBe PurchaserRemove.No
      }

      "when 'keep' is selected" in {
        val result = form.bind(Map(fieldName -> "keep"))
        result.value.value mustBe PurchaserRemove.Keep
      }

      "when 'REMOVE-' prefix with purchaser ID is provided" in {
        val purchaserId = "PUR-001"
        val result = form.bind(Map(fieldName -> s"REMOVE-$purchaserId"))
        result.value.value mustBe PurchaserRemove.Remove(purchaserId)
      }

      "when 'PROMOTE-' prefix with purchaser ID is provided" in {
        val purchaserId = "PUR-002"
        val result = form.bind(Map(fieldName -> s"PROMOTE-$purchaserId"))
        result.value.value mustBe PurchaserRemove.SelectNewMain(purchaserId)
      }
    }

    "must unbind valid values" - {

      "when PurchaserRemove.No is provided" in {
        val filledForm = form.fill(PurchaserRemove.No)
        filledForm.data.get(fieldName) mustBe Some("no-action")
      }

      "when PurchaserRemove.Keep is provided" in {
        val filledForm = form.fill(PurchaserRemove.Keep)
        filledForm.data.get(fieldName) mustBe Some("no-action")
      }

      "when PurchaserRemove.Remove is provided" in {
        val purchaserId = "PUR-001"
        val filledForm = form.fill(PurchaserRemove.Remove(purchaserId))
        filledForm.data.get(fieldName) mustBe Some(s"REMOVE-$purchaserId")
      }

      "when PurchaserRemove.SelectNewMain is provided" in {
        val purchaserId = "PUR-002"
        val filledForm = form.fill(PurchaserRemove.SelectNewMain(purchaserId))
        filledForm.data.get(fieldName) mustBe Some(s"PROMOTE-$purchaserId")
      }
    }

    "must fail to bind" - {

      "when value is empty" in {
        val result = form.bind(Map(fieldName -> ""))
        result.errors must contain(FormError(fieldName, requiredKey))
      }

      "when key is not present" in {
        val result = form.bind(Map.empty[String, String])
        result.errors must contain(FormError(fieldName, requiredKey))
      }

      "when invalid value is provided" in {
        val result = form.bind(Map(fieldName -> "invalid"))
        result.errors.nonEmpty mustBe true
      }

      "when value has no valid prefix" in {
        val result = form.bind(Map(fieldName -> "INVALID-PUR-001"))
        result.errors.nonEmpty mustBe true
      }
    }

    "must successfully bind and unbind in round trip" - {

      "for No action" in {
        val data = Map(fieldName -> "no")
        val boundForm = form.bind(data)
        val filledForm = form.fill(boundForm.value.get)

        boundForm.value.value mustBe PurchaserRemove.No
        filledForm.data.get(fieldName) mustBe Some("no-action")
      }

      "for Keep action" in {
        val data = Map(fieldName -> "keep")
        val boundForm = form.bind(data)
        val filledForm = form.fill(boundForm.value.get)

        boundForm.value.value mustBe PurchaserRemove.Keep
        filledForm.data.get(fieldName) mustBe Some("no-action")
      }

      "for Remove action" in {
        val purchaserId = "PUR-123"
        val data = Map(fieldName -> s"REMOVE-$purchaserId")
        val boundForm = form.bind(data)
        val filledForm = form.fill(boundForm.value.get)

        boundForm.value.value mustBe PurchaserRemove.Remove(purchaserId)
        filledForm.data.get(fieldName) mustBe Some(s"REMOVE-$purchaserId")
      }

      "for SelectNewMain action" in {
        val purchaserId = "PUR-456"
        val data = Map(fieldName -> s"PROMOTE-$purchaserId")
        val boundForm = form.bind(data)
        val filledForm = form.fill(boundForm.value.get)

        boundForm.value.value mustBe PurchaserRemove.SelectNewMain(purchaserId)
        filledForm.data.get(fieldName) mustBe Some(s"PROMOTE-$purchaserId")
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}