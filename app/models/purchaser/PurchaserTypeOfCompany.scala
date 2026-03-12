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

sealed trait PurchaserTypeOfCompany {
  def order: Int
}

object PurchaserTypeOfCompany extends Enumerable.Implicits {

  case object UnincorporatedBuilder extends WithName("unincorporatedBuilder") with PurchaserTypeOfCompany {
    val order = 1
  }
  case object UnincorporatedSoleTrader extends WithName("unincorporatedSoleTrader") with PurchaserTypeOfCompany {
    val order = 2
  }
  case object IndividualOther extends WithName("individualOther") with PurchaserTypeOfCompany {
    val order = 3
  }
  case object Partnership extends WithName("partnership") with PurchaserTypeOfCompany {
    val order = 4
  }
  case object LocalAuthority extends WithName("localAuthority") with PurchaserTypeOfCompany {
    val order = 5
  }
  case object CentralGovernment extends WithName("centralGovernment") with PurchaserTypeOfCompany {
    val order = 6
  }
  case object PublicCorporation extends WithName("publicCorporation") with PurchaserTypeOfCompany {
    val order = 7
  }
  case object PropertyCompany extends WithName("propertyCompany") with PurchaserTypeOfCompany {
    val order = 8
  }
  case object Bank extends WithName("bank") with PurchaserTypeOfCompany {
    val order = 9
  }
  case object BuildingSociety extends WithName("buildingSociety") with PurchaserTypeOfCompany {
    val order = 10
  }
  case object InsuranceAssurance extends WithName("insuranceAssurance") with PurchaserTypeOfCompany {
    val order = 11
  }
  case object SuperannuationOrPensionFund extends WithName("superannuationOrPensionFund") with PurchaserTypeOfCompany {
    val order = 12
  }
  case object OtherFinancialInstitute extends WithName("otherFinancialInstitute") with PurchaserTypeOfCompany {
    val order = 13
  }
  case object OtherCompany extends WithName("otherCompany") with PurchaserTypeOfCompany {
    val order = 14
  }
  case object OtherIncludingCharity extends WithName("otherIncludingCharity") with PurchaserTypeOfCompany {
    val order = 15
  }

  val values: Seq[PurchaserTypeOfCompany] = Seq(
    Bank,
    BuildingSociety,
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
  ).sortBy(_.order)

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
