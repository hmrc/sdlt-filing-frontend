/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

sealed trait PurchaserTypeOfCompany

object PurchaserTypeOfCompany extends Enumerable.Implicits {

  case object Bank extends WithName("bank") with PurchaserTypeOfCompany
  case object BuildingAssociation extends WithName("buildingAssociation") with PurchaserTypeOfCompany
  case object CentralGovernment extends WithName("centralGovernment") with PurchaserTypeOfCompany
  case object IndividualOther extends WithName("individualOther") with PurchaserTypeOfCompany
  case object InsuranceAssurance extends WithName("insuranceAssurance") with PurchaserTypeOfCompany
  case object LocalAuthority extends WithName("localAuthority") with PurchaserTypeOfCompany
  case object Partnership extends WithName("partnership") with PurchaserTypeOfCompany
  case object PropertyCompany extends WithName("propertyCompany") with PurchaserTypeOfCompany
  case object PublicCorporation extends WithName("publicCorporation") with PurchaserTypeOfCompany
  case object OtherCompany extends WithName("otherCompany") with PurchaserTypeOfCompany
  case object OtherFinancialInstitute extends WithName("otherFinancialInstitute") with PurchaserTypeOfCompany
  case object OtherIncludingCharity extends WithName("otherIncludingCharity") with PurchaserTypeOfCompany
  case object SuperannuationOrPensionFund extends WithName("superannuationOrPensionFund") with PurchaserTypeOfCompany
  case object UnincorporatedBuilder extends WithName("unincorporatedBuilder") with PurchaserTypeOfCompany
  case object UnincorporatedSoleTrader extends WithName("unincorporatedSoleTrader") with PurchaserTypeOfCompany

  val values: Seq[PurchaserTypeOfCompany] = Seq(
    Bank,
    BuildingAssociation,
    CentralGovernment,
    IndividualOther,
    InsuranceAssurance,
    LocalAuthority,
    Partnership,
    PropertyCompany,
    PublicCorporation,
    OtherCompany,
    OtherFinancialInstitute,
    OtherIncludingCharity,
    SuperannuationOrPensionFund,
    UnincorporatedBuilder,
    UnincorporatedSoleTrader
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"purchaser.purchaserTypeOfCompany.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[PurchaserTypeOfCompany] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
