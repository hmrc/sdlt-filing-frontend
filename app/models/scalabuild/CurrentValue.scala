/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait CurrentValue {
  def asBoolean: Boolean
}

object CurrentValue extends Enumerable.Implicits {
  case object AtOrBelowThreshold extends CurrentValue {
    override val asBoolean: Boolean = true
  }

  case object AboveThreshold extends CurrentValue {
    override val asBoolean: Boolean = false
  }

  val values: Seq[CurrentValue] = Seq(
    AtOrBelowThreshold,
    AboveThreshold
  )

  def options(ftbLimit: Int)(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        value = Some(CurrentValue.AtOrBelowThreshold.toString),
        content = Text(messages("currentValue.atOrBelowThreshold", ftbLimit)),
        id = Some("currentValue-yes"),
        hint = Some(Hint(content = Text(messages("currentValue.atOrBelowThreshold.hint"))))
      ),
      RadioItem(
        value = Some(CurrentValue.AboveThreshold.toString),
        content = Text(messages("currentValue.aboveThreshold", ftbLimit)),
        id = Some("currentValue-no")
      )
    )

  def fromBoolean(value: Boolean): CurrentValue = {
    if (value) AtOrBelowThreshold else AboveThreshold
  }

  implicit val enumerable: Enumerable[CurrentValue] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
