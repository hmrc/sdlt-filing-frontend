/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import models.scalabuild.PropertyType
import play.api.libs.json.JsPath


case object ResidentialOrNonResidentialPage extends QuestionPage[PropertyType] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "propertyType"
}
