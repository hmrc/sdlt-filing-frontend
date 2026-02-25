/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait PropertyType {
  val displayCya: String
}

  object PropertyType extends Enumerable.Implicits {
    case object Residential extends PropertyType {
    override val displayCya: String = "Residential"
    }
    case object NonResidential extends PropertyType {
    override val displayCya: String = "Non-residential"
  }

    val values: Seq[PropertyType] = Seq(
      Residential,
      NonResidential
    )

    def options(implicit messages: Messages): Seq[RadioItem] =
      values.map { value =>
        val label = messages(s"propertyType.${value.toString}")
        RadioItem(
          content = Text(label),
          value = Some(value.toString),
          id = Some(label.toLowerCase())
        )
      }

    implicit val enumerable: Enumerable[PropertyType] =
      Enumerable(values.map(v => v.toString -> v): _*)
  }

