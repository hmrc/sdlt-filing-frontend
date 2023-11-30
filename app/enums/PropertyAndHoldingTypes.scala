/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package enums

import play.api.libs.json._

import scala.collection.Seq

object HoldingTypes extends Enumeration {

  val leasehold = Value
  val freehold = Value

  private val lowerCaseReads = new Reads[HoldingTypes.Value] {
    def reads(json: JsValue) = json match {
      case JsString(str) =>
        HoldingTypes.values
          .find(_.toString == str.toLowerCase)
          .map(JsSuccess(_))
          .getOrElse(JsError(Seq(JsPath() -> Seq(JsonValidationError("invalid holding type")))))
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("no holding type string provided"))))
    }
  }


  implicit val reads: Reads[HoldingTypes.Value] = lowerCaseReads
}

object PropertyTypes extends Enumeration {

  val residential = Value
  val nonResidential = Value

  private val propertyReads: Reads[PropertyTypes.Value] = new Reads[PropertyTypes.Value] {
    override def reads(json: JsValue): JsResult[PropertyTypes.Value] = json match {
      case JsString("Residential")     => JsSuccess(PropertyTypes.residential)
      case JsString("Non-residential") => JsSuccess(PropertyTypes.nonResidential)
      case JsString(err) => JsError(Seq(JsPath() -> Seq(JsonValidationError("invalid property type"))))
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("no property type string provided"))))
    }
  }

  implicit val reads: Reads[PropertyTypes.Value] = propertyReads
}

