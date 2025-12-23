/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import play.api.libs.json.JsPath

case object PurchasePricePage extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "premium"
}
