/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.ReplaceMainResidenceFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.ReplaceMainResidenceView


class ReplaceMainResidenceControllerSpec extends ScalaSpecBase {
  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/purchase-price")
  val formProvider = new ReplaceMainResidenceFormProvider()
  val form: Form[Boolean] = formProvider()
  lazy val replaceMainResidenceRoute: String = controllers.scalabuild.routes.ReplaceMainResidenceController.onPageLoad().url

  "ReplaceMainResidence Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, replaceMainResidenceRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ReplaceMainResidenceView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, replaceMainResidenceRoute)
            .withFormUrlEncodedBody(("replaceMainResidence", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, replaceMainResidenceRoute)
            .withFormUrlEncodedBody(("replaceMainResidence", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("replaceMainResidence" -> ""))

        val view = application.injector.instanceOf[ReplaceMainResidenceView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
