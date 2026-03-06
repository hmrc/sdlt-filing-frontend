/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.RelevantRentDetails
import models.scalabuild.MongoLeaseDetails
import models.scalabuild.PageConstants.relevantRentDetails
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case object RelevantRentDetailPage extends Gettable[RelevantRentDetails] with Settable[MongoLeaseDetails] {
  override def path: JsPath = JsPath \ relevantRentDetails
}


