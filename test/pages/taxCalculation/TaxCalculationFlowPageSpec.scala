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

package pages.taxCalculation

import base.SpecBase
import models.taxCalculation.TaxCalculationFlow
import play.api.libs.json.JsPath

class TaxCalculationFlowPageSpec extends SpecBase {

  "TaxCalculationFlowPage" - {

    "toString must equal 'taxCalculationFlow'" in {
      TaxCalculationFlowPage.toString mustBe "taxCalculationFlow"
    }

    "path must point at the top-level 'taxCalculationFlow' key" in {
      TaxCalculationFlowPage.path mustBe (JsPath \ "taxCalculationFlow")
    }

    "must round-trip every TaxCalculationFlow value via UserAnswers" in {
      TaxCalculationFlow.values.foreach { flow =>
        val updated = emptyUserAnswers.set(TaxCalculationFlowPage, flow).success.value
        updated.get(TaxCalculationFlowPage) mustBe Some(flow)
      }
    }

    "must remove the value when remove is called" in {
      val withFlow    = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      val withoutFlow = withFlow.remove(TaxCalculationFlowPage).success.value

      withoutFlow.get(TaxCalculationFlowPage) mustBe None
    }
  }
}
