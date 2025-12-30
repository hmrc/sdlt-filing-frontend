/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.propertyDetails
import play.api.libs.json.JsPath

case object ReplaceMainResidencePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ propertyDetails \ toString

  override def toString: String = "replaceMainResidence"

}