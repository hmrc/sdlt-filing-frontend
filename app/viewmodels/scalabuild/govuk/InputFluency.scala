/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import viewmodels.scalabuild.ErrorMessageAwareness

object input extends InputFluency

trait InputFluency {

  object InputViewModel extends ErrorMessageAwareness {

    def apply(
      field: Field,
      label: Label
    )(implicit messages: Messages): Input =
      Input(
        id = field.id,
        name = field.name,
        value = field.value,
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentInput(input: Input) {

    def asNumeric(): Input =
        input.copy(inputmode = Some("numeric"))
        .withPattern("[0-9]*")

    def withInputMode(inputMode: String): Input =
      input.copy(inputmode = Some(inputMode))

    def withHint(hint: Hint): Input =
      input.copy(hint = Some(hint))

    def withCssClass(newClass: String): Input =
      input.copy(classes = s"${input.classes} $newClass")

    def withPattern(pattern: String): Input =
      input.copy(pattern = Some(pattern))
  }
}
