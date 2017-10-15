package testutils

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsResult, JsSuccess}
import uk.gov.hmrc.play.test.UnitSpec

trait JsonValidation {
  this: UnitSpec =>

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedError: ValidationError): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> Seq(expectedError)))
  }

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedErrors: Seq[ValidationError]): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> expectedErrors))
  }

  def shouldHaveErrors[T](result: JsResult[T], expectedErrors: Map[JsPath, Seq[ValidationError]]): Unit = {
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
