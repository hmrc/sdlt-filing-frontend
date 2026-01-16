/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.PremiumFormProvider
import pages.scalabuild.PremiumPage
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.PremiumView
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class PremiumControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/premium")
  val formProvider = new PremiumFormProvider()
  val form          = formProvider()
  lazy val premiumRoute = routes.PremiumController.onPageLoad().url
  val premium = BigDecimal(1234)

  "Premium Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, premiumRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[PremiumView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(PremiumPage, premium).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, premiumRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[PremiumView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(premium))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, premiumRoute)
            .withFormUrlEncodedBody(("premium", premium.toString)).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, premiumRoute)
            .withFormUrlEncodedBody(("premium", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("premium" -> ""))
        val view = application.injector.instanceOf[PremiumView]
        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}