package pages.taxCalculation

import pages.QuestionPage
import play.api.libs.json.JsPath

case object CalculatedSdltDuePage extends QuestionPage[Int] {
  
  override def path: JsPath = JsPath \ toString

  override def toString: String = "calculatedSdltDue"
}
