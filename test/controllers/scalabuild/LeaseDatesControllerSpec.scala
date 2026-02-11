/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.LeaseDatesFormProvider
import models.scalabuild.LeaseDates
import pages.scalabuild.{EffectiveDatePage, LeaseDatesPage}
import org.scalatest.freespec.AnyFreeSpec
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.LeaseDatesView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.time.LocalDate

class LeaseDatesControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/premium")

  val formProvider = new LeaseDatesFormProvider()
  val thisYear = LocalDate.now().getYear
  val validEffectiveDate = LocalDate.of(thisYear, 1, 1)
  val form = formProvider(validEffectiveDate)
  val leaseDates = LeaseDates(LocalDate.of(thisYear, 1, 1), LocalDate.of(thisYear + 1, 1, 1))

  lazy val leaseDatesControllerRoute: String = controllers.scalabuild.routes.LeaseDatesController.onPageLoad().url

  "Lease Dates Controller" - {

    "must return OK and the correct view for a GET when saved user Details are fetched" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, leaseDatesControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[LeaseDatesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(LeaseDatesPage, leaseDates).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, leaseDatesControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[LeaseDatesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(leaseDates))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, leaseDatesControllerRoute)
            .withFormUrlEncodedBody(
              ("leaseStartDate.day", "1"), ("leaseStartDate.month", "1"), ("leaseStartDate.year", s"$thisYear"),
              ("leaseEndDate.day", "1"), ("leaseEndDate.month", "1"), ("leaseEndDate.year", s"${thisYear + 1}")
            ).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, leaseDatesControllerRoute)
            .withFormUrlEncodedBody(("leaseStartDate.day", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("leaseStartDate.day" -> ""))
        val view = application.injector.instanceOf[LeaseDatesView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, leaseDatesControllerRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseDatesControllerRoute)
            .withFormUrlEncodedBody(
              ("leaseStartDate.day", "1"), ("leaseStartDate.month", "1"), ("leaseStartDate.year", s"$thisYear"),
              ("leaseEndDate.day", "1"), ("leaseEndDate.month", "1"), ("leaseEndDate.year", s"${thisYear + 1}")
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}