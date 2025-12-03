/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import base.ScalaSpecBase
import forms.scalabuild.MainResidenceFormProvider
import views.html.scalabuild.MainResidenceView

import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call

  class MainResidenceControllerSpec extends ScalaSpecBase {
    def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/main-residence")
    val formProvider = new MainResidenceFormProvider()
    val form          = formProvider()
    lazy val mainResidenceRoute = controllers.scalabuild.routes.MainResidenceController.onPageLoad().url

    "Main Residence Controller" - {
      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, mainResidenceRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
          val result = route(application, request).value
          val view = application.injector.instanceOf[MainResidenceView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(form)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val application = applicationBuilder().build()

        running(application) {
          val request =
            FakeRequest(POST, mainResidenceRoute)
              .withFormUrlEncodedBody(("mainResidence", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder().build()

        running(application) {
          val request =
            FakeRequest(POST, mainResidenceRoute)
              .withFormUrlEncodedBody(("mainResidence", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val boundForm = form.bind(Map("mainResidence" -> ""))

          val view = application.injector.instanceOf[MainResidenceView]

          val result = route(application, request).value

          status(result)          mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
        }
      }
    }
}
