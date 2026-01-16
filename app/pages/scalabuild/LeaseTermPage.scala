/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.LeaseTerm
import models.scalabuild.PageConstants.leaseDetails
import play.api.libs.json.JsPath

case object LeaseTermPage extends QuestionPage[LeaseTerm] {

  override def path: JsPath = JsPath \ leaseDetails \ toString

  override def toString: String = "term"
}
