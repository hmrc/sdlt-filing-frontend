/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.relevantRentDetails
import play.api.libs.json.JsPath

case object ContractPost201603Page extends QuestionPage[Boolean]{

  override def path: JsPath = JsPath \ relevantRentDetails \ toString

  override def toString: String = "contractChangedSinceMar16"
}