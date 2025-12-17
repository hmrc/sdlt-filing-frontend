/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait Tenancy
object Tenancy extends Enumerable.Implicits {
  case object Freehold extends Tenancy
  case object Leasehold extends Tenancy

  val values: Seq[Tenancy] = Seq(
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

  implicit val enumerable: Enumerable[Tenancy] =
    Enumerable(values.map(v => v.toString -> v): _*)

}

