/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import play.api.mvc.Call
import forms.scalabuild.CurrentValueFormProvider
import models.scalabuild.CurrentValue.{AboveThreshold, AtOrBelowThreshold}
import pages.scalabuild.{CurrentValuePage, EffectiveDatePage}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.mvc.request.RequestAttrKey
import views.html.scalabuild.CurrentValueView
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.time.LocalDate

class CurrentValueControllerSpec extends AnyFreeSpec with ScalaSpecBase with TestObjects {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/purchase-price")

  val formProvider = new CurrentValueFormProvider()
  val form = formProvider()
  lazy val currentValueRoute = routes.CurrentValueController.onPageLoad().url
  val lowerFtbLimit = 500000
  val higherFtbLimit = 625000
  val thisYear = LocalDate.now().getYear
  val validEffectiveDate = LocalDate.of(thisYear, 1, 1)

  "Current value Controller" - {
    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers2
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, currentValueRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[CurrentValueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers2.set(CurrentValuePage, true).success.value
                          .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, currentValueRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[CurrentValueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AtOrBelowThreshold), lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = uaFreeRes
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, currentValueRoute)
            .withFormUrlEncodedBody(("value", AboveThreshold.toString)).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers2
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, currentValueRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[CurrentValueView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must show the lower FTB limit before the threshold date" in {
      val lowerFTBDate = LocalDate.of(2022, 9, 22)
      val userAnswers = emptyUserAnswers2
        .set(EffectiveDatePage, lowerFTBDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(GET, currentValueRoute)
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[CurrentValueView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, lowerFtbLimit)(request, messages(application)).toString
      }
    }

    "must show the higher FTB limit in between the threshold dates" in {
      val higherFTBDate = LocalDate.of(2022, 9, 23)
      val userAnswers = emptyUserAnswers2
        .set(EffectiveDatePage, higherFTBDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(GET, currentValueRoute)
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[CurrentValueView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, higherFtbLimit)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, currentValueRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, currentValueRoute)
            .withFormUrlEncodedBody(("value", AtOrBelowThreshold.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}