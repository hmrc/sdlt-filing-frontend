/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.PropertyDetails
import models.scalabuild.PageConstants.propertyDetails
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case object PropertyDetailsPage extends Gettable[PropertyDetails] with Settable[PropertyDetails] {
  override def path: JsPath = JsPath \ propertyDetails
}


