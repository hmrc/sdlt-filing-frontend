/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.IsPurchaserIndividualFormProvider
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.Form
import views.html.scalabuild.IsPurchaserIndividualView
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class IsPurchaserIndividualControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/additional-property-double")
  val formProvider = new IsPurchaserIndividualFormProvider()
  val form: Form[Boolean] = formProvider()
  lazy val isPurchaserIndividualRoute: String = controllers.scalabuild.routes.IsPurchaserIndividualController.onPageLoad().url

  "IsPurchaserIndividual Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, isPurchaserIndividualRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[IsPurchaserIndividualView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isPurchaserIndividualRoute)
            .withFormUrlEncodedBody(("individual", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isPurchaserIndividualRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsPurchaserIndividualView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}