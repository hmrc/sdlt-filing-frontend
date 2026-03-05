/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import play.api.libs.json.JsPath

case object MainResidencePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "mainResidence"

}
