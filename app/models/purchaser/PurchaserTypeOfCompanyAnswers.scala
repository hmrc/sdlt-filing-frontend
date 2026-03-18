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
                                          buildingAssociation: String,
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
      bank = if (selected.contains(Bank)) "YES" else "NO",
      buildingAssociation = if (selected.contains(BuildingAssociation)) "YES" else "NO",
      centralGovernment = if (selected.contains(CentralGovernment)) "YES" else "NO",
      individualOther = if (selected.contains(IndividualOther)) "YES" else "NO",
      insuranceAssurance = if (selected.contains(InsuranceAssurance)) "YES" else "NO",
      localAuthority = if (selected.contains(LocalAuthority)) "YES" else "NO",
      partnership = if (selected.contains(Partnership)) "YES" else "NO",
      propertyCompany = if (selected.contains(PropertyCompany)) "YES" else "NO",
      publicCorporation = if (selected.contains(PublicCorporation)) "YES" else "NO",
      otherCompany = if (selected.contains(OtherCompany)) "YES" else "NO",
      otherFinancialInstitute = if (selected.contains(OtherFinancialInstitute)) "YES" else "NO",
      otherIncludingCharity = if (selected.contains(OtherIncludingCharity)) "YES" else "NO",
      superannuationOrPensionFund = if (selected.contains(SuperannuationOrPensionFund)) "YES" else "NO",
      unincorporatedBuilder = if (selected.contains(UnincorporatedBuilder)) "YES" else "NO",
      unincorporatedSoleTrader = if (selected.contains(UnincorporatedSoleTrader)) "YES" else "NO"
    )
  }

  def toSet(answers: PurchaserTypeOfCompanyAnswers): Set[PurchaserTypeOfCompany] = {
    val allValues: Map[PurchaserTypeOfCompany, String] = Map(
      Bank -> answers.bank,
      BuildingAssociation -> answers.buildingAssociation,
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
      case (key, "YES") => key
    }.toSet
  }

}