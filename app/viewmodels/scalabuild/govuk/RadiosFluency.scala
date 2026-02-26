/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.govuk

import play.api.data.Field
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.{RadioItem, Radios}
import viewmodels.scalabuild.ErrorMessageAwareness

object radios extends RadiosFluency

trait RadiosFluency {

  object RadiosViewModel extends ErrorMessageAwareness with FieldsetFluency {

    def apply(
      field: Field,
      items: Seq[RadioItem],
      legend: Legend
    )(implicit messages: Messages): Radios =
      apply(
        field = field,
        items = items,
        fieldset = FieldsetViewModel(legend)
      )

    def apply(
      field: Field,
      items: Seq[RadioItem],
      fieldset: Fieldset
    )(implicit messages: Messages): Radios =
      Radios(
        fieldset = Some(fieldset),
        name = field.name,
        items = items map (item => item.copy(checked = field.value.isDefined && field.value == item.value)),
        errorMessage = errorMessage(field)
      )

    def yesNo(
      field: Field,
      legend: Legend
    )(implicit messages: Messages): Radios =
      yesNo(
        field = field,
        fieldset = FieldsetViewModel(legend)
      )

    def yesNo(
      field: Field,
      fieldset: Fieldset
    )(implicit messages: Messages): Radios = {

      val items = Seq(
        RadioItem(
          id = Some(field.id),
          value = Some("true"),
          content = Text(messages("site.yes"))
        ),
        RadioItem(
          id = Some(s"${field.id}-no"),
          value = Some("false"),
          content = Text(messages("site.no"))
        )
      )

      apply(
        field = field,
        fieldset = fieldset,
        items = items
      ).inline()
    }


    def yesNoCustomIdSuffix(
      field: Field,
      legend: Legend,
      yesId: String,
      noId: String
    )(implicit messages: Messages): Radios =
      yesNoCustomIdSuffix(
        field = field,
        fieldset = FieldsetViewModel(legend),
        yesId: String,
        noId: String
      )

    def yesNoCustomIdSuffix(
      field: Field,
      fieldset: Fieldset,
      yesId: String,
      noId: String
    )(implicit messages: Messages): Radios = {

      val items = Seq(
        RadioItem(
          id = Some(s"${field.id}${yesId}"),
          value = Some("true"),
          content = Text(messages("site.yes"))
        ),
        RadioItem(
          id = Some(s"${field.id}${noId}"),
          value = Some("false"),
          content = Text(messages("site.no"))
        )
      )

      apply(
        field = field,
        fieldset = fieldset,
        items = items
      ).inline()
    }
    def yesNoCustomId(
      field: Field,
      fieldset: Fieldset,
      yesId: String,
      noId: String
    )(implicit messages: Messages): Radios = {

      val items = Seq(
        RadioItem(
          id = Some(s"${yesId}"),
          value = Some("true"),
          content = Text(messages("site.yes"))
        ),
        RadioItem(
          id = Some(s"${noId}"),
          value = Some("false"),
          content = Text(messages("site.no"))
        )
      )

      apply(
        field = field,
        fieldset = fieldset,
        items = items
      ).inline()
    }

    def yesNoItemsWithConditionalHtml(
                                       field: Field,
                                       conditionalYesHtml: Option[Html] = None,
                                       conditionalNoHtml: Option[Html] = None,
                                       yesText: String = "site.yes",
                                       noText: String = "site.no",
                                       yesId: String = "",
                                       noId: String = "-no"
                                     )(implicit messages: Messages): Seq[RadioItem] = Seq(
      RadioItem(
        id = Some(field.id+yesId),
        value = Some("true"),
        content = Text(messages(yesText)),
        conditionalHtml = conditionalYesHtml
      ),
      RadioItem(
        id = Some(field.id+noId),
        value = Some("false"),
        content = Text(messages(noText)),
        conditionalHtml = conditionalNoHtml
      )
    )

    def yesNoWithConditionalHtml(
                                  field: Field,
                                  legend: Legend,
                                  items: Seq[RadioItem],
                                  // will be appended to the end of input Id's to allow for custom Id's needed in AT's
                                  yesId: String = "",
                                  noId: String = "-no"
                                )(implicit messages: Messages): Radios = {

      apply(
        field = field,
        fieldset = FieldsetViewModel(legend),
        items = items
      )
    }
  }

  implicit class FluentRadios(radios: Radios) {

    def withHint(hint: Hint): Radios =
      radios.copy(hint = Some(hint))

    def inline(): Radios ={
      val newClass = "govuk-radios--inline"
      radios.copy(classes = s"${radios.classes} $newClass")
    }
  }
}
