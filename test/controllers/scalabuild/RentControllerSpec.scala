/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.RentFormProvider
import models.scalabuild.{LeaseContext, LeaseContextBuilder, LeaseDates, LeaseTerm, RentPeriods}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.RentView
import play.api.data.FormBinding
import org.mockito.ArgumentMatchers._
import pages.scalabuild.{EffectiveDatePage, LeaseDatesPage, RentPage}
import play.api.inject.bind

import java.time.LocalDate

class RentControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  lazy val rentRoute = controllers.scalabuild.routes.RentController.onPageLoad().url

  "RentController" - {
    val mockLeaseContextBuilder = mock[LeaseContextBuilder]
    val expectedPeriodCount = 4
    val expectedTerm = LeaseTerm(4, 0, 0)
    val thisYear = LocalDate.now().getYear
    val validEffectiveDate = LocalDate.of(thisYear, 1, 1)
    val leaseDates = LeaseDates(LocalDate.of(thisYear, 1, 2), LocalDate.of(thisYear + 4, 1, 1))
    val rentPeriods = RentPeriods(List(BigDecimal(12), BigDecimal(34), BigDecimal(56), BigDecimal(78)))
    when(mockLeaseContextBuilder.build(any(), any(), any()))
      .thenReturn(LeaseContext(periodCount = expectedPeriodCount, term = expectedTerm))


    "must return OK and render the view on GET" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(LeaseDatesPage, leaseDates).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, rentRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[RentView]
        val formProvider = application.injector.instanceOf[RentFormProvider]
        val form = formProvider(expectedPeriodCount)

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(form, expectedPeriodCount)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(LeaseDatesPage, leaseDates).success.value
        .set(RentPage, rentPeriods).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, rentRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[RentView]
        val formProvider = application.injector.instanceOf[RentFormProvider]
        val form = formProvider(expectedPeriodCount)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(rentPeriods), expectedPeriodCount)(request, messages(application)).toString
      }
    }

    "must redirect to GET when valid rents are submitted" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(LeaseDatesPage, leaseDates).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, rentRoute)
            .withFormUrlEncodedBody(
              "rents[0]" -> "100",
              "rents[1]" -> "200",
              "rents[2]" -> "300",
              "rents[3]" -> "400"
            )
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe rentRoute
      }
    }

    "must return Bad Request and show errors when invalid rents are submitted" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(LeaseDatesPage, leaseDates).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, rentRoute)
            .withFormUrlEncodedBody(
              "rents[0]" -> "100",
              "rents[1]" -> "",
              "rents[2]" -> "300"
              // missing rents[3] → should fail length = periodCount validation
            )
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentView]
        val formProvider = application.injector.instanceOf[RentFormProvider]
        val form = formProvider(expectedPeriodCount)
        val boundForm = form.bindFromRequest()(request, FormBinding.Implicits.formBinding)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(boundForm, expectedPeriodCount)(request, messages(application)).toString
      }
    }


    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, rentRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if effective date is present but lease dates are not" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, rentRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, rentRoute)
            .withFormUrlEncodedBody(
              "rents[0]" -> "100",
              "rents[1]" -> "200",
              "rents[2]" -> "300",
              "rents[3]" -> "400"
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
