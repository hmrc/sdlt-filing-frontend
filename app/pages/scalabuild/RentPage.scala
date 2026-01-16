/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.leaseDetails
import models.scalabuild.RentPeriods
import play.api.libs.json.JsPath

case object RentPage extends QuestionPage[RentPeriods] {

  override def path: JsPath = JsPath \ leaseDetails \ toString

  override def toString: String = "rentDetails"
}