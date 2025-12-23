/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import models.scalabuild.HoldingTypes
import play.api.libs.json.JsPath

case object HoldingPage extends QuestionPage[HoldingTypes] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "holdingType"
}

