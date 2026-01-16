/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.LeaseDates
import models.scalabuild.PageConstants.leaseDetails
import play.api.libs.json.JsPath

case object LeaseDatesPage extends QuestionPage[LeaseDates] {

  override def path: JsPath = JsPath \ leaseDetails \ toString

  override def toString: String = "leaseDates"
}