/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package testutils

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._
import play.api.libs.json._


trait JsonValidation {
  this: PlaySpec =>

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedError: JsonValidationError): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> Seq(expectedError)))
  }

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedErrors: Seq[JsonValidationError]): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> expectedErrors))
  }

  def shouldHaveErrors[T](result: JsResult[T], expectedErrors: Map[JsPath, Seq[JsonValidationError]]): Unit = {
    result match {
      case JsSuccess(jsValue, _) => fail(s"read should have failed and didn't - produced $jsValue")
      case JsError(errors) =>
        errors.length shouldBe expectedErrors.keySet.toSeq.length
        for( error <- errors ) {
          error match {
            case (path, valErrs) => {
              expectedErrors.keySet should contain(path)
              expectedErrors(path) shouldBe valErrs
            }
          }
        }
    }
  }
}
