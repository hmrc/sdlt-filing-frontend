/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild.taxCalculation

import pages.scalabuild.QuestionPage
import play.api.libs.json.JsPath

case object PremiumPayablePage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "premium"
}