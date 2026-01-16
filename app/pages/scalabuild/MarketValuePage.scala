/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.MarketValueChoice
import play.api.libs.json.JsPath

object MarketValuePage extends QuestionPage[MarketValueChoice] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "marketValue"
}
