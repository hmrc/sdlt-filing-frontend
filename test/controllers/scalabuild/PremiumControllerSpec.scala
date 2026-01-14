/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.PremiumFormProvider
import org.scalatest.freespec.AnyFreeSpec
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.PremiumView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class PremiumControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/premium")
  val formProvider = new PremiumFormProvider()
  val form = formProvider()
  lazy val premiumRoute = routes.PremiumController.onPageLoad().url

  "Premium Controller" - {
    "must return OK and the correct view for a GET" in {
      val testApp = application()
      running(testApp) {
        val request = FakeRequest(GET, premiumRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(testApp, request).value
        val view = testApp.injector.instanceOf[PremiumView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(testApp)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val testApp = application()
      running(testApp) {
        val request =
          FakeRequest(POST, premiumRoute)
            .withFormUrlEncodedBody(("premium", "1234"))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(testApp, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val testApp = application()
      running(testApp) {
        val request =
          FakeRequest(POST, premiumRoute)
            .withFormUrlEncodedBody(("premium", ""))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("premium" -> ""))
        val view = testApp.injector.instanceOf[PremiumView]
        val result = route(testApp, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(testApp)).toString
      }
    }
  }
}
