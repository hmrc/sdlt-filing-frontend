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

package viewmodels.taxCalculation

import models.taxCalculation.{CalculationDetails, CalculationResponse, TaxCalcTestFormData, TaxTypes}

case class TaxCalcResultViewModel(
  requestJson:       String,
  responseJson:      String,
  effectiveDate:     String,
  interestCodeLabel: String,
  propertyTypeLabel: String,
  isLinked:          String,
  totalTax:          Int,
  premiumTaxDue:     Int,
  npvTaxDue:         Int,
  premiumCalcs:      Seq[CalculationDetails],
  rentCalcs:         Seq[CalculationDetails]
)

object TaxCalcResultViewModel {

  def from(
    response:     CalculationResponse,
    input:        TaxCalcTestFormData,
    requestJson:  String,
    responseJson: String
  ): TaxCalcResultViewModel = {
    val result       = response.result.headOption
    val premiumCalcs = result.toSeq.flatMap(_.taxCalcs.filter(_.taxType == TaxTypes.premium))
    val rentCalcs    = result.toSeq.flatMap(_.taxCalcs.filter(_.taxType == TaxTypes.rent))
    val premiumTaxDue = premiumCalcs.map(_.taxDue).sum
    val npvTaxDue     = result.flatMap(_.npv).getOrElse(rentCalcs.map(_.taxDue).sum)
    val totalTax      = result.map(_.totalTax).getOrElse(0)

    TaxCalcResultViewModel(
      requestJson       = requestJson,
      responseJson      = responseJson,
      effectiveDate     = input.effectiveDate.toString,
      interestCodeLabel = interestCodeLabel(input.interestCode),
      propertyTypeLabel = propertyTypeLabel(input.propertyType),
      isLinked          = input.isLinked.getOrElse("No"),
      totalTax          = totalTax,
      premiumTaxDue     = premiumTaxDue,
      npvTaxDue         = npvTaxDue,
      premiumCalcs      = premiumCalcs,
      rentCalcs         = rentCalcs
    )
  }

  private def propertyTypeLabel(code: String): String = code match {
    case "01"   => "01 - Residential"
    case "02"   => "02 - Mixed"
    case "03"   => "03 - Non-residential"
    case "04"   => "04 - Additional"
    case other  => other
  }

  private def interestCodeLabel(code: String): String = code match {
    case "FG"  => "FG - Freehold granted"
    case "FP"  => "FP - Freehold purchased"
    case "FT"  => "FT - Freehold transferred"
    case "LG"  => "LG - Leasehold granted"
    case "LP"  => "LP - Leasehold purchased"
    case "LT"  => "LT - Leasehold transferred"
    case other => other
  }
}