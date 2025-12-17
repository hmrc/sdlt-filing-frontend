/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.{Input, PrefixOrSuffix}
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

    def withHint(hint: Hint): Input =
      input.copy(hint = Some(hint))

    def withCssClass(newClass: String): Input =
      input.copy(classes = s"${input.classes} $newClass")

    def withPattern(pattern: String): Input =
      input.copy(pattern = Some(pattern))

    def withPrefix(prefix: PrefixOrSuffix): Input =
      input.copy(prefix = Some(prefix))

    def asVisuallyHidden(): Input =
      withCssClass("govuk-visually-hidden")

    def withPoundPrefix: Input =
      input.copy(prefix = Some(PrefixOrSuffix(content = HtmlContent("&pound;"))))
  }
}
