/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import play.api.mvc.Call
import forms.scalabuild.ResidentialOrNonResidentialFormProvider
import models.scalabuild.PropertyType.Residential
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.ResidentialOrNonResidentialView

class ResidentialOrNonResidentialControllerSpec extends ScalaSpecBase {
  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/property")
  val formProvider = new ResidentialOrNonResidentialFormProvider()
  val form          = formProvider()
  lazy val residentialOrNonResidentialRoute = routes.ResidentialOrNonResidentialController.onPageLoad().url

  "Residential Or Non Residential Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, residentialOrNonResidentialRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ResidentialOrNonResidentialView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, residentialOrNonResidentialRoute)
            .withFormUrlEncodedBody(("value", Residential.toString)).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, residentialOrNonResidentialRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ResidentialOrNonResidentialView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
