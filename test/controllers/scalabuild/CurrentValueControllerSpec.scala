/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import play.api.mvc.Call
import forms.scalabuild.CurrentValueFormProvider
import models.scalabuild.CurrentValue.AtOrBelowThreshold
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.mvc.request.RequestAttrKey
import views.html.scalabuild.CurrentValueView

import java.time.LocalDate

class CurrentValueControllerSpec extends ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/current-value")
  val formProvider = new CurrentValueFormProvider()
  val form          = formProvider()
  lazy val currentValueRoute = routes.CurrentValueController.onPageLoad().url
  val lowerFtbLimit = 500000
  val higherFtbLimit = 625000

  "Current value Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, currentValueRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[CurrentValueView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, currentValueRoute)
            .withFormUrlEncodedBody(("value", AtOrBelowThreshold.toString)).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, currentValueRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[CurrentValueView]
        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must show the lower FTB limit before the threshold date" in {
      val application =
        applicationBuilderWithDate(LocalDate.of(2022, 9, 22)).build()
      running(application) {
        val request =
          FakeRequest(GET, currentValueRoute)
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view   = application.injector.instanceOf[CurrentValueView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must show the higher FTB limit in between the threshold dates" in {
      val application =
        applicationBuilderWithDate(LocalDate.of(2022, 9, 23)).build()
      running(application) {
        val request =
          FakeRequest(GET, currentValueRoute)
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view   = application.injector.instanceOf[CurrentValueView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, higherFtbLimit)(request, messages(application)).toString
      }
    }
  }
}