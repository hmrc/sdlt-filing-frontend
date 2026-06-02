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
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}

class PopulateTaxCalculationServiceSpec extends SpecBase {

  private val service = new PopulateTaxCalculationService

  "populateTaxCalculationInSession" - {

    "FreeholdTaxCalculated sets total amount due, the penalties flag and the self-assessed amount" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("43850"), includesPenalty = Some("yes"), taxDue = Some("43750"))
      val result = service.populateTaxCalculationInSession(taxCalculation, FreeholdTaxCalculated, emptyUserAnswers).success.value

      result.get(FreeholdTaxCalculatedTotalAmountDuePage).value mustEqual "43850"
      result.get(FreeholdTaxCalculatedPenaltiesAndInterestPage).value mustEqual true
      result.get(FreeholdTaxCalculatedSelfAssessedAmountPage).value mustEqual "43750"
    }

    "FreeholdSelfAssessed sets total amount due, the penalties flag and the self-assessed amount" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("500"), includesPenalty = Some("no"), taxDue = Some("300"))
      val result = service.populateTaxCalculationInSession(taxCalculation, FreeholdSelfAssessed, emptyUserAnswers).success.value

      result.get(FreeholdSelfAssessedTotalAmountDuePage).value mustEqual "500"
      result.get(FreeholdSelfAssessedPenaltiesAndInterestPage).value mustEqual false
      result.get(FreeholdSelfAssessedAmountPage).value mustEqual "300"
    }

    "LeaseholdTaxCalculated sets total amount due, the penalties flag and the self-assessed amount" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("900"), includesPenalty = Some("yes"), taxDue = Some("700"))
      val result = service.populateTaxCalculationInSession(taxCalculation, LeaseholdTaxCalculated, emptyUserAnswers).success.value

      result.get(LeaseholdTaxCalculatedTotalAmountDuePage).value mustEqual "900"
      result.get(LeaseholdTaxCalculatedPenaltiesAndInterestPage).value mustEqual true
      result.get(LeaseholdTaxCalculatedSelfAssessedAmountPage).value mustEqual "700"
    }

    "LeaseholdSelfAssessed sets total amount due, the penalties flag and the premium and NPV tax" in {
      val taxCalculation = TaxCalculation(amountPaid = Some("100"), includesPenalty = Some("no"), taxDuePremium = Some("60"), taxDueNPV = Some("40"))
      val result = service.populateTaxCalculationInSession(taxCalculation, LeaseholdSelfAssessed, emptyUserAnswers).success.value

      result.get(LeaseholdSelfAssessedTotalAmountDuePage).value mustEqual "100"
      result.get(LeaseholdSelfAssessedPenaltiesAndInterestPage).value mustEqual false
      result.get(LeaseholdSelfAssessedPremiumPayableTaxPage).value mustEqual "60"
      result.get(LeaseholdSelfAssessedNpvTaxPage).value mustEqual "40"
    }

    "leaves pages unset when the backend fields are absent" in {
      val result = service.populateTaxCalculationInSession(TaxCalculation(), FreeholdTaxCalculated, emptyUserAnswers).success.value

      result.get(FreeholdTaxCalculatedTotalAmountDuePage) mustBe None
      result.get(FreeholdTaxCalculatedPenaltiesAndInterestPage) mustBe None
      result.get(FreeholdTaxCalculatedSelfAssessedAmountPage) mustBe None
    }
  }
}
