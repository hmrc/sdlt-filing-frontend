/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import models.scalabuild.MarketValueChoice.{PayInStages, PayUpfront}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

case class MarketValue(value: MarketValueChoice,
                       paySDLTUpfront: Option[BigDecimal],
                       marketPropValue: Option[BigDecimal]
                      ) {
  def premium: BigDecimal =
    paySDLTUpfront.orElse(marketPropValue).getOrElse(throw new IllegalStateException("MarketValue must contain a premium"))
}

object MarketValue {
  def fromUserAnswers(marketValueChoice: MarketValueChoice, premium: BigDecimal): MarketValue =
    marketValueChoice match {
      case PayUpfront => MarketValue(marketValueChoice, Some(premium), None)
      case PayInStages => MarketValue(marketValueChoice, None, Some(premium))
    }
}

sealed trait MarketValueChoice {
  def id: String
  val displayCya: String
}

object MarketValueChoice extends Enumerable.Implicits {
  case object PayUpfront extends MarketValueChoice {
    val id = "paySDLTUpfront"
    val displayCya = "Using market value election"
  }

  case object PayInStages extends MarketValueChoice {
    val id = "paySDLTInStages"
    val displayCya = "Stages"
  }

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


