package pages.taxCalculation

import pages.QuestionPage
import play.api.libs.json.JsPath

case object ConfirmEffectiveDateOfTransactionPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "confirmEffectiveDateOfTransaction"

}
