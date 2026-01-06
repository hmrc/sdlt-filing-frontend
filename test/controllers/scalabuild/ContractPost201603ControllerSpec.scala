/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.ContractPost201603FormProvider
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.ContractPost201603View

class ContractPost201603ControllerSpec extends ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/contract-post-201603")
  val formProvider = new ContractPost201603FormProvider()
  val form          = formProvider()
  lazy val contractPost201603Route = routes.ContractPost201603Controller.onPageLoad().url

  "Contract Post 201603 Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, contractPost201603Route).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ContractPost201603View]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, contractPost201603Route)
            .withFormUrlEncodedBody(("contract-varied-post-201603", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, contractPost201603Route)
            .withFormUrlEncodedBody(("contract-varied-post-201603", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("contract-varied-post-201603" -> ""))

        val view = application.injector.instanceOf[ContractPost201603View]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}