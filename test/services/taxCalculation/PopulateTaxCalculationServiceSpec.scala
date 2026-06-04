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

package services.taxCalculation

import base.SpecBase
import models.TaxCalculation
import models.taxCalculation.TaxCalculationFlow.{FreeholdTaxCalculated, LeaseholdSelfAssessed}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}

class PopulateTaxCalculationServiceSpec extends SpecBase {

  private val service = new PopulateTaxCalculationService

  "PopulateTaxCalculationService" - {

    "must populate the total amount due, penalties flag and self-assessed amount from the tax calculation" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("43850"), includesPenalty = Some("yes"), taxDue = Some("43750"))

      val result = service.populateTaxCalculationInSession(taxCalculation, FreeholdTaxCalculated, emptyUserAnswers).success.value

      result.get(FreeholdTaxCalculatedTotalAmountDuePage).value mustEqual "43850"
      result.get(FreeholdTaxCalculatedPenaltiesAndInterestPage).value mustEqual true
      result.get(FreeholdTaxCalculatedSelfAssessedAmountPage).value mustEqual "43750"
    }

    "must populate the premium and NPV tax for a leasehold self-assessed return" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("100"), includesPenalty = Some("no"), taxDuePremium = Some("60"), taxDueNPV = Some("40"))

      val result = service.populateTaxCalculationInSession(taxCalculation, LeaseholdSelfAssessed, emptyUserAnswers).success.value

      result.get(LeaseholdSelfAssessedTotalAmountDuePage).value mustEqual "100"
      result.get(LeaseholdSelfAssessedPenaltiesAndInterestPage).value mustEqual false
      result.get(LeaseholdSelfAssessedPremiumPayableTaxPage).value mustEqual "60"
      result.get(LeaseholdSelfAssessedNpvTaxPage).value mustEqual "40"
    }

    "must leave the answers unset when the tax calculation has no values" in {
      val result = service.populateTaxCalculationInSession(TaxCalculation(), FreeholdTaxCalculated, emptyUserAnswers).success.value

      result.get(FreeholdTaxCalculatedTotalAmountDuePage) mustBe None
      result.get(FreeholdTaxCalculatedPenaltiesAndInterestPage) mustBe None
    }
  }
}
