/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait CurrentValue
object CurrentValue extends  Enumerable.Implicits {
  case object AtOrBelowThreshold extends CurrentValue

  case object AboveThreshold extends CurrentValue

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

  implicit val enumerable: Enumerable[CurrentValue] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
