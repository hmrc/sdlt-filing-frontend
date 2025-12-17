/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

case class MarketValue(value: MarketValueChoice,
                       paySDLTUpfront: Option[BigDecimal],
                       marketPropValue: Option[BigDecimal]
                      )

sealed trait MarketValueChoice {
  def id: String
}
  object MarketValueChoice extends Enumerable.Implicits {
    case object PayUpfront extends MarketValueChoice{ val id = "paySDLTUpfront"}

    case object PayInStages extends MarketValueChoice{ val id = "paySDLTInStages"}

    val values: Seq[MarketValueChoice] = Seq(
      PayUpfront,
      PayInStages
    )

    def options(implicit messages: Messages): Seq[RadioItem] =

      values.map { value =>
        val label = messages(s"marketValue.${value.toString}")
        RadioItem(
          content = Text(label),
          value = Some(value.toString),
          id = Some(value.id)
        )
      }

    implicit val enumerable: Enumerable[MarketValueChoice] =
      Enumerable(values.map(v => v.toString -> v): _*)
  }


