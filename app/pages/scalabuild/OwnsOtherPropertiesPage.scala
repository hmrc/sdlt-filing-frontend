/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import play.api.libs.json.JsPath

case object OwnsOtherPropertiesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ownedOtherProperties"
}