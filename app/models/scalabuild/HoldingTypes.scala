/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait HoldingTypes
object HoldingTypes extends Enumerable.Implicits {
  case object Freehold extends HoldingTypes
  case object Leasehold extends HoldingTypes

  val values: Seq[HoldingTypes] = Seq(
    Freehold,
    Leasehold
  )

  def options(implicit messages: Messages): Seq[RadioItem] =
    values.map {  value =>
      RadioItem(
        content = Text(messages(s"${value.toString}")),
        value = Some(value.toString),
        id = Some(s"holding-${value.toString.dropRight(4).toLowerCase()}")
      )
    }

  implicit val enumerable: Enumerable[HoldingTypes] =
    Enumerable(values.map(v => v.toString -> v): _*)

}

