/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild


import base.ScalaSpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild.QuestionPage
import play.api.libs.json._

import java.time.Instant
import scala.util.Success

class UserAnswersSpec extends AnyFreeSpec with ScalaSpecBase{

  private val id       = "id"
  private val testData = Json.obj("key" -> "value")

  case object TestPage extends QuestionPage[JsValue] {
    override def path: JsPath = JsPath \ "test"
  }

  case object EmptyPathPage extends QuestionPage[JsValue] {
    override def path: JsPath = JsPath
  }
  case object TestPageOne extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "testPageOne"
  }
  case object TestPageTwo extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "testPageTwo"
  }

  "set" - {
    "successfully set a value" in {
      val userAnswers    = UserAnswers(id)
      val updatedAnswers = userAnswers.set(TestPage, testData)

      val result = updatedAnswers.get
      result.get(TestPage) mustBe Some(testData)
    }

    "fail to set a value when page path is empty" in {
      val invalidData = Json.toJson(Instant.now())
      val userAnswers = UserAnswers(id)

      val result = userAnswers.set(EmptyPathPage, invalidData)
      result.isFailure mustBe true
    }
  }
  "setTwo" - {
    "set both pages correctly and return updated UserAnswers" in {

      val emptyAnswers = UserAnswers("id")

      val result = emptyAnswers.setTwo(
        TestPageOne,
        "value1",
        TestPageTwo,
        "value2"
      )

      result mustBe a[Success[_]]
      val updatedAnswers = result.get

      updatedAnswers.get(TestPageOne) mustBe Some("value1")
      updatedAnswers.get(TestPageTwo) mustBe Some("value2")
    }
  }

  "get" - {
    "return the value when it has been set using set method" in {
      val userAnswers = emptyUserAnswers.set(TestPage, testData).success.value

      val result = userAnswers.get(TestPage)
      result mustBe Some(testData)
    }
  }

  "remove" - {
    "successfully remove a page" in {
      val userAnswers = emptyUserAnswers.set(TestPage, testData).success.value

      val result = userAnswers.remove(TestPage).success.value
      result.get(TestPage) mustBe None
    }
  }
}
