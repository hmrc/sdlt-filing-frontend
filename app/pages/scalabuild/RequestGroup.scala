/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import models.scalabuild.RequestFromMongo
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case object RequestGroup extends Gettable[RequestFromMongo] with Settable[RequestFromMongo] {
  override def path: JsPath = JsPath
}


