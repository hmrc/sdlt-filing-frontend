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

package models.taxCalculation

import models.taxCalculation.TaxCalculationFlow.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class TaxCalculationFlowSpec extends AnyFreeSpec with Matchers {

  "TaxCalculationFlow" - {

    "must round-trip FreeholdTaxCalculated" in {
      Json.toJson(FreeholdTaxCalculated: TaxCalculationFlow) mustBe JsString("FreeholdTaxCalculated")
      JsString("FreeholdTaxCalculated").as[TaxCalculationFlow] mustBe FreeholdTaxCalculated
    }

    "must round-trip FreeholdSelfAssessed" in {
      Json.toJson(FreeholdSelfAssessed: TaxCalculationFlow) mustBe JsString("FreeholdSelfAssessed")
      JsString("FreeholdSelfAssessed").as[TaxCalculationFlow] mustBe FreeholdSelfAssessed
    }

    "must round-trip LeaseholdTaxCalculated" in {
      Json.toJson(LeaseholdTaxCalculated: TaxCalculationFlow) mustBe JsString("LeaseholdTaxCalculated")
      JsString("LeaseholdTaxCalculated").as[TaxCalculationFlow] mustBe LeaseholdTaxCalculated
    }

    "must round-trip LeaseholdSelfAssessed" in {
      Json.toJson(LeaseholdSelfAssessed: TaxCalculationFlow) mustBe JsString("LeaseholdSelfAssessed")
      JsString("LeaseholdSelfAssessed").as[TaxCalculationFlow] mustBe LeaseholdSelfAssessed
    }

    "must fail to read an unknown flow string" in {
      JsString("SomethingElse").validate[TaxCalculationFlow] mustBe a[JsError]
    }

    "must fail to read a non-string JSON value" in {
      JsNumber(1).validate[TaxCalculationFlow] mustBe a[JsError]
    }
  }
}
