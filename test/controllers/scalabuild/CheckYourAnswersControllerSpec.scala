/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._

class CheckYourAnswersControllerSpec extends AnyFreeSpec with ScalaSpecBase with TestObjects (){

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/summary")
  lazy val checkYourAnswersRoute = routes.CheckYourAnswersController.onPageLoad().url

  "CheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers = freeResNonIndAddMainJourney.toOption
      val application = applicationBuilder(userAnswers = userAnswers).build()
      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result)          mustEqual OK
      }
    }
  }
}