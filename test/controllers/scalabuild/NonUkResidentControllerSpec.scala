/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import play.api.mvc.Call
import forms.scalabuild.NonUkResidentFormProvider
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.NonUkResidentView

class NonUkResidentControllerSpec extends ScalaSpecBase {
  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/non-uk-resident")
  val formProvider = new NonUkResidentFormProvider()
  val form          = formProvider()
  lazy val nonUkResidentRoute = controllers.scalabuild.routes.NonUkResidentController.onPageLoad().url

  "Non UK Resident Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, nonUkResidentRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[NonUkResidentView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentRoute)
            .withFormUrlEncodedBody(("nonUKResident", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[NonUkResidentView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
