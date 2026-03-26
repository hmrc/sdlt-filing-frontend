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

package models.purchaser

import models.purchaser.PurchaserTypeOfCompany._
import play.api.libs.json.{Format, Json}


case class PurchaserTypeOfCompanyAnswers(
                                          bank: String,
                                          buildingSociety: String,
                                          centralGovernment: String,
                                          individualOther: String,
                                          insuranceAssurance: String,
                                          localAuthority: String,
                                          partnership: String,
                                          propertyCompany: String,
                                          publicCorporation: String,
                                          otherCompany: String,
                                          otherFinancialInstitute: String,
                                          otherIncludingCharity: String,
                                          superannuationOrPensionFund: String,
                                          unincorporatedBuilder: String,
                                          unincorporatedSoleTrader: String
                                        )

object PurchaserTypeOfCompanyAnswers {
  implicit val format: Format[PurchaserTypeOfCompanyAnswers] = Json.format[PurchaserTypeOfCompanyAnswers]

  def fromSet(selected: Set[PurchaserTypeOfCompany]): PurchaserTypeOfCompanyAnswers = {
    PurchaserTypeOfCompanyAnswers(
      bank = if (selected.contains(Bank)) "yes" else "no",
      buildingSociety = if (selected.contains(BuildingSociety)) "yes" else "no",
      centralGovernment = if (selected.contains(CentralGovernment)) "yes" else "no",
      individualOther = if (selected.contains(IndividualOther)) "yes" else "no",
      insuranceAssurance = if (selected.contains(InsuranceAssurance)) "yes" else "no",
      localAuthority = if (selected.contains(LocalAuthority)) "yes" else "no",
      partnership = if (selected.contains(Partnership)) "yes" else "no",
      propertyCompany = if (selected.contains(PropertyCompany)) "yes" else "no",
      publicCorporation = if (selected.contains(PublicCorporation)) "yes" else "no",
      otherCompany = if (selected.contains(OtherCompany)) "yes" else "no",
      otherFinancialInstitute = if (selected.contains(OtherFinancialInstitute)) "yes" else "no",
      otherIncludingCharity = if (selected.contains(OtherIncludingCharity)) "yes" else "no",
      superannuationOrPensionFund = if (selected.contains(SuperannuationOrPensionFund)) "yes" else "no",
      unincorporatedBuilder = if (selected.contains(UnincorporatedBuilder)) "yes" else "no",
      unincorporatedSoleTrader = if (selected.contains(UnincorporatedSoleTrader)) "yes" else "no"
    )
  }

  def toSet(answers: PurchaserTypeOfCompanyAnswers): Set[PurchaserTypeOfCompany] = {
    val allValues: Map[PurchaserTypeOfCompany, String] = Map(
      Bank -> answers.bank,
      BuildingSociety -> answers.buildingSociety,
      CentralGovernment -> answers.centralGovernment,
      IndividualOther -> answers.individualOther,
      InsuranceAssurance -> answers.insuranceAssurance,
      LocalAuthority -> answers.localAuthority,
      Partnership -> answers.partnership,
      PropertyCompany -> answers.propertyCompany,
      PublicCorporation -> answers.publicCorporation,
      OtherCompany -> answers.otherCompany,
      OtherFinancialInstitute -> answers.otherFinancialInstitute,
      OtherIncludingCharity -> answers.otherIncludingCharity,
      SuperannuationOrPensionFund -> answers.superannuationOrPensionFund,
      UnincorporatedBuilder -> answers.unincorporatedBuilder,
      UnincorporatedSoleTrader -> answers.unincorporatedSoleTrader
    )

    allValues.collect {
      case (key, value) if value.trim.equalsIgnoreCase("yes") => key
    }.toSet
  }

}