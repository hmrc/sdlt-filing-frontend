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

class DetailControllerSpec extends AnyFreeSpec with ScalaSpecBase with TestObjects() {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/result")
  lazy val detailRoute = routes.DetailController.onPageLoad(Some(0),Some(0)).url

  "Detail Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers = freeResNonIndAddMainJourney.toOption
      val application = applicationBuilder(userAnswers = userAnswers).build()
      running(application) {
        val request = FakeRequest(GET, detailRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to Journey Recovery for a GET no if result is received" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, detailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}