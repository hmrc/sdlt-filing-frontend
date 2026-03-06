/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.MongoLeaseDetails
import models.scalabuild.PageConstants.leaseDetails
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case object MongoLeaseDetailPage extends Gettable[MongoLeaseDetails] with Settable[MongoLeaseDetails] {
  override def path: JsPath = JsPath \ leaseDetails
}


