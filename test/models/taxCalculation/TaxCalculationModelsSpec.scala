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

import base.SpecBase
import models.FullReturn
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}
import play.api.libs.json.{JsSuccess, Json}

class TaxCalculationModelsSpec extends SpecBase {

  private val calculatedResult = TaxCalculationResult(
    totalTax      = 43750,
    resultHeading = None,
    resultHint    = None,
    npv           = None,
    taxCalcs      = Seq(CalculationDetails(TaxTypes.premium, CalcTypes.slab, 43750, None, None, None, Some(5), None, None))
  )

  private val leaseholdResult = TaxCalculationResult(
    totalTax      = 11000,
    resultHeading = None,
    resultHint    = None,
    npv           = Some(100000),
    taxCalcs      = Seq(
      CalculationDetails(TaxTypes.premium, CalcTypes.slab,  8000, None, None, None, Some(5), None, None),
      CalculationDetails(TaxTypes.rent,    CalcTypes.slice, 3000, None, None, None, Some(1), None, None)
    )
  )

  private val fullReturn = FullReturn(stornId = "TESTSTORN", returnResourceRef = "REF")

  private val freeholdCalculatedAnswers = emptyUserAnswers
    .copy(fullReturn = Some(fullReturn))
    .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
    .set(FreeholdTaxCalculatedSelfAssessedAmountPage, "43750").success.value
    .set(FreeholdTaxCalculatedTotalAmountDuePage, "43850").success.value
    .set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

  private val freeholdSelfAssessedAnswers = emptyUserAnswers
    .copy(fullReturn = Some(fullReturn))
    .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value
    .set(FreeholdSelfAssessedAmountPage, "43750").success.value
    .set(FreeholdSelfAssessedTotalAmountDuePage, "43850").success.value
    .set(FreeholdSelfAssessedPenaltiesAndInterestPage, false).success.value

  private val leaseholdCalculatedAnswers = emptyUserAnswers
    .copy(fullReturn = Some(fullReturn))
    .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdTaxCalculated).success.value
    .set(LeaseholdTaxCalculatedSelfAssessedAmountPage, "11000").success.value
    .set(LeaseholdTaxCalculatedTotalAmountDuePage, "11100").success.value
    .set(LeaseholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

  private val leaseholdSelfAssessedAnswers = emptyUserAnswers
    .copy(fullReturn = Some(fullReturn))
    .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value
    .set(LeaseholdSelfAssessedPremiumPayableTaxPage, "8000").success.value
    .set(LeaseholdSelfAssessedNpvTaxPage, "3000").success.value
    .set(LeaseholdSelfAssessedTotalAmountDuePage, "11100").success.value
    .set(LeaseholdSelfAssessedPenaltiesAndInterestPage, false).success.value

  "UpdateTaxCalculationRequest" - {

    "must serialize to JSON correctly with all fields populated" in {
      val request = UpdateTaxCalculationRequest(
        stornId = "STORN123",
        returnResourceRef = "100001",
        amountPaid = Some("43850"),
        includesPenalty = Some("no"),
        taxDue = Some("43750"),
        calcPenaltyDue = Some("100"),
        calcTaxDue = Some("43750"),
        calcTaxRate1 = Some("5"),
        calcTaxRate2 = Some("10"),
        calcTotalTaxPenaltyDue = Some("43850"),
        calcTotalNpvTax = Some("3000"),
        calcTotalPremiumTax = Some("8000"),
        taxDuePremium = Some("8000"),
        taxDueNpv = Some("3000"),
        honestyDeclaration = Some("yes")
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN123"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "amountPaid").as[String] mustBe "43850"
      (json \ "includesPenalty").as[String] mustBe "no"
      (json \ "taxDue").as[String] mustBe "43750"
      (json \ "calcPenaltyDue").as[String] mustBe "100"
      (json \ "calcTaxDue").as[String] mustBe "43750"
      (json \ "calcTaxRate1").as[String] mustBe "5"
      (json \ "calcTaxRate2").as[String] mustBe "10"
      (json \ "calcTotalTaxPenaltyDue").as[String] mustBe "43850"
      (json \ "calcTotalNpvTax").as[String] mustBe "3000"
      (json \ "calcTotalPremiumTax").as[String] mustBe "8000"
      (json \ "taxDuePremium").as[String] mustBe "8000"
      (json \ "taxDueNpv").as[String] mustBe "3000"
      (json \ "honestyDeclaration").as[String] mustBe "yes"
    }

    "must serialize to JSON correctly with only required fields" in {
      val request = UpdateTaxCalculationRequest(
        stornId = "STORN123",
        returnResourceRef = "100001"
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN123"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "amountPaid").toOption mustBe None
      (json \ "taxDue").toOption mustBe None
      (json \ "calcTotalNpvTax").toOption mustBe None
      (json \ "taxDueNpv").toOption mustBe None
      (json \ "honestyDeclaration").toOption mustBe None
    }

    "must deserialize from JSON correctly with all fields populated" in {
      val json = Json.obj(
        "stornId"                -> "STORN123",
        "returnResourceRef"      -> "100001",
        "amountPaid"             -> "43850",
        "includesPenalty"        -> "no",
        "taxDue"                 -> "43750",
        "calcPenaltyDue"         -> "100",
        "calcTaxDue"             -> "43750",
        "calcTaxRate1"           -> "5",
        "calcTaxRate2"           -> "10",
        "calcTotalTaxPenaltyDue" -> "43850",
        "calcTotalNpvTax"        -> "3000",
        "calcTotalPremiumTax"    -> "8000",
        "taxDuePremium"          -> "8000",
        "taxDueNpv"              -> "3000",
        "honestyDeclaration"     -> "yes"
      )

      val result = json.validate[UpdateTaxCalculationRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN123"
      request.returnResourceRef mustBe "100001"
      request.amountPaid mustBe Some("43850")
      request.includesPenalty mustBe Some("no")
      request.taxDue mustBe Some("43750")
      request.calcPenaltyDue mustBe Some("100")
      request.calcTaxDue mustBe Some("43750")
      request.calcTaxRate1 mustBe Some("5")
      request.calcTaxRate2 mustBe Some("10")
      request.calcTotalTaxPenaltyDue mustBe Some("43850")
      request.calcTotalNpvTax mustBe Some("3000")
      request.calcTotalPremiumTax mustBe Some("8000")
      request.taxDuePremium mustBe Some("8000")
      request.taxDueNpv mustBe Some("3000")
      request.honestyDeclaration mustBe Some("yes")
    }

    "must deserialize from JSON correctly with only required fields" in {
      val json = Json.obj(
        "stornId"           -> "STORN123",
        "returnResourceRef" -> "100001"
      )

      val result = json.validate[UpdateTaxCalculationRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN123"
      request.returnResourceRef mustBe "100001"
      request.amountPaid mustBe None
      request.taxDue mustBe None
      request.calcTotalNpvTax mustBe None
      request.taxDueNpv mustBe None
      request.honestyDeclaration mustBe None
    }

    "must fail to deserialize when required field stornId is missing" in {
      val json = Json.obj("returnResourceRef" -> "100001")

      json.validate[UpdateTaxCalculationRequest].isError mustBe true
    }

    "must fail to deserialize when required field returnResourceRef is missing" in {
      val json = Json.obj("stornId" -> "STORN123")

      json.validate[UpdateTaxCalculationRequest].isError mustBe true
    }

    ".from" - {

      "must build the request from a FreeholdTaxCalculated flow" in {
        val request = UpdateTaxCalculationRequest.from(freeholdCalculatedAnswers, Some(calculatedResult), BigDecimal(100)).futureValue

        request.stornId mustBe "TESTSTORN"
        request.returnResourceRef mustBe "REF"
        request.amountPaid mustBe Some("43850")
        request.includesPenalty mustBe Some("yes")
        request.taxDue mustBe Some("43750")
        request.calcTaxDue mustBe Some("43750")
        request.calcPenaltyDue mustBe Some("100")
        request.calcTaxRate1 mustBe Some("5%")
        request.calcTotalTaxPenaltyDue mustBe Some("43850")
        request.honestyDeclaration mustBe Some("yes")
        request.calcTaxRate2 mustBe None
        request.calcTotalNpvTax mustBe None
        request.calcTotalPremiumTax mustBe None
      }

      "must fall back to the calculated SDLT due when the self-assessed amount is absent" in {
        val answers = freeholdCalculatedAnswers.remove(FreeholdTaxCalculatedSelfAssessedAmountPage).success.value

        UpdateTaxCalculationRequest.from(answers, Some(calculatedResult), BigDecimal(0)).futureValue.taxDue mustBe Some("43750")
      }

      "must build the request from a FreeholdSelfAssessed flow without any calculated fields" in {
        val request = UpdateTaxCalculationRequest.from(freeholdSelfAssessedAnswers, None, BigDecimal(50)).futureValue

        request.amountPaid mustBe Some("43850")
        request.includesPenalty mustBe Some("no")
        request.taxDue mustBe Some("43750")
        request.calcPenaltyDue mustBe Some("50")
        request.calcTotalTaxPenaltyDue mustBe Some("50")
        request.calcTaxDue mustBe None
        request.calcTaxRate1 mustBe None
        request.calcTotalPremiumTax mustBe None
      }

      "must build the request from a LeaseholdTaxCalculated flow with premium and NPV rates and tax" in {
        val request = UpdateTaxCalculationRequest.from(leaseholdCalculatedAnswers, Some(leaseholdResult), BigDecimal(100)).futureValue

        request.amountPaid mustBe Some("11100")
        request.taxDue mustBe Some("11000")
        request.calcTaxDue mustBe Some("11000")
        request.calcTaxRate1 mustBe Some("5%")
        request.calcTaxRate2 mustBe Some("1%")
        request.calcTotalPremiumTax mustBe Some("8000")
        request.calcTotalNpvTax mustBe Some("3000")
        request.taxDuePremium mustBe Some("8000")
        request.taxDueNpv mustBe Some("3000")
        request.calcTotalTaxPenaltyDue mustBe Some("11100")
      }

      "must format a fractional rate as a decimal percentage" in {
        val fractionalResult = leaseholdResult.copy(taxCalcs = Seq(
          CalculationDetails(TaxTypes.premium, CalcTypes.slab,  8000, None, None, None, Some(4), Some(5), None),
          CalculationDetails(TaxTypes.rent,    CalcTypes.slice, 3000, None, None, None, Some(1), None,    None)
        ))
        val request = UpdateTaxCalculationRequest.from(leaseholdCalculatedAnswers, Some(fractionalResult), BigDecimal(100)).futureValue

        request.calcTaxRate1 mustBe Some("4.5%")
        request.calcTaxRate2 mustBe Some("1%")
      }

      "must build the request from a LeaseholdSelfAssessed flow with the premium and NPV answers" in {
        val request = UpdateTaxCalculationRequest.from(leaseholdSelfAssessedAnswers, None, BigDecimal(0)).futureValue

        request.amountPaid mustBe Some("11100")
        request.taxDuePremium mustBe Some("8000")
        request.taxDueNpv mustBe Some("3000")
        request.taxDue mustBe None
        request.calcTaxDue mustBe None
        request.calcTotalPremiumTax mustBe None
      }

      "must fail when the full return is absent" in {
        val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

        UpdateTaxCalculationRequest.from(answers, Some(calculatedResult), BigDecimal(0)).failed.futureValue mustBe a[NoSuchElementException]
      }
    }
  }

  "UpdateTaxCalculationReturn" - {

    "must serialize to JSON correctly when updated is true" in {
      val json = Json.toJson(UpdateTaxCalculationReturn(updated = true))
      (json \ "updated").as[Boolean] mustBe true
    }

    "must serialize to JSON correctly when updated is false" in {
      val json = Json.toJson(UpdateTaxCalculationReturn(updated = false))
      (json \ "updated").as[Boolean] mustBe false
    }

    "must deserialize from JSON correctly when updated is true" in {
      val result = Json.obj("updated" -> true).validate[UpdateTaxCalculationReturn]
      result mustBe a[JsSuccess[_]]
      result.get.updated mustBe true
    }

    "must fail to deserialize when updated field is missing" in {
      Json.obj().validate[UpdateTaxCalculationReturn].isError mustBe true
    }
  }
}
