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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait PurchaserConfirmIdentity

object PurchaserConfirmIdentity extends Enumerable.Implicits {

  case object VatRegistrationNumber extends WithName("vatRegistrationNumber") with PurchaserConfirmIdentity
  case object CorporationTaxUTR extends WithName("corporationTaxUniqueTaxpayerReference") with PurchaserConfirmIdentity
  case object PartnershipUTR extends WithName("partnershipUniqueTaxpayerReference") with PurchaserConfirmIdentity
  case object Divider extends WithName("confirmIdentityDivider") with PurchaserConfirmIdentity
  case object AnotherFormOfID extends WithName("anotherFormOfId") with PurchaserConfirmIdentity

  val values: Seq[PurchaserConfirmIdentity] = Seq(
    VatRegistrationNumber, CorporationTaxUTR, PartnershipUTR, Divider, AnotherFormOfID
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (Divider, index) =>
      RadioItem(
        content = Text(messages(s"purchaser.confirmIdentity.${Divider.toString}")),
        id = Some(s"value_$index"),
        divider = Some(messages(s"purchaser.confirmIdentity.${Divider.toString}"))
      )
    case (value, index) =>
        RadioItem(
          content = Text(messages(s"purchaser.confirmIdentity.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
  }

  implicit val enumerable: Enumerable[PurchaserConfirmIdentity] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
