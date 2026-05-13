package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ConfirmEffectiveDateOfTransaction {

  object ConfirmEffectiveDateOfTransaction extends Enumerable.Implicits {

    case object EffectiveDateOfTransactionYes extends WithName("yes") with ConfirmEffectiveDateOfTransaction

    case object EffectiveDateOfTransactionNo extends WithName("no") with ConfirmEffectiveDateOfTransaction

    val values: Seq[ConfirmEffectiveDateOfTransaction] = Seq(
      EffectiveDateOfTransactionYes, EffectiveDateOfTransactionNo
    )

    def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"site.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }

    implicit val enumerable: Enumerable[ConfirmEffectiveDateOfTransaction] =
      Enumerable(values.map(v => v.toString -> v): _*)
  }

}
