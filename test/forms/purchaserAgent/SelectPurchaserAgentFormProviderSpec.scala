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

package forms.purchaserAgent

import forms.behaviours.OptionFieldBehaviours
import models.purchaserAgent.SelectPurchaserAgent
import models.Agent
import play.api.data.{Form, FormError}

class SelectPurchaserAgentFormProviderSpec extends OptionFieldBehaviours {

  val testStorn = "STN005"

  private def createAgent(
                           agentId: String,
                           name: String,
                           houseNumber: Option[String] = None,
                           address1: String = "123 Street",
                           address2: Option[String] = Some("Town"),
                           address3: Option[String] = Some("City"),
                           address4: Option[String] = Some("County"),
                           postcode: Option[String] = Some("AA1 1AA"),
                           phone: Option[String] = Some("0123456789"),
                           email: Option[String] = Some("test@example.com"),
                           dxAddress: Option[String] = Some("yes"),
                           agentResourceReference: String = "REF001"
                         ): Agent =
    Agent(
      storn = Some(testStorn),
      agentId = Some(agentId),
      name = Some(name),
      houseNumber = houseNumber,
      address1 = Some(address1),
      address2 = address2,
      address3 = address3,
      address4 = address4,
      postcode = postcode,
      phone = phone,
      email = email,
      dxAddress = dxAddress,
      agentResourceReference = Some(agentResourceReference)
    )

  val agentList: Seq[Agent] = Seq(
    createAgent(agentId = "AGT001", name = "John Smith"),
    createAgent(agentId = "AGT002", name = "Jane Jones")
  )

  val formProvider = new SelectPurchaserAgentFormProvider()
  val form: Form[String] = formProvider(agentList)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "purchaserAgent.selectPurchaserAgent.error.required"
    val invalidKey = "purchaserAgent.selectPurchaserAgent.error.invalid"

    "must bind valid existing agent value correctly" in {
      val result = form.bind(Map(fieldName -> "AGT002"))
      result.errors mustBe empty
      result.value.value mustBe "AGT002"
    }

    "must bind valid add new agent value correctly" in {
      val result = form.bind(Map("value" -> SelectPurchaserAgent.AddNewAgent.toString))
      result.errors mustBe empty
      result.value.value mustBe SelectPurchaserAgent.AddNewAgent.toString
    }

    "must error when agent not in list" in {
      val result = form.bind(Map(fieldName -> "AGT003"))
      result.errors must contain(FormError(fieldName, invalidKey))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
