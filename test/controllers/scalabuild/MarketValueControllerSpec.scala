/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.MarketValueFormProvider
import models.scalabuild.MarketValueChoice.PayUpfront
import org.scalatest.freespec.AnyFreeSpec
import models.scalabuild.MarketValue
import models.scalabuild.MarketValueChoice.PayInStages
import pages.scalabuild.{EffectiveDatePage, MarketValuePage, PremiumPage}
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.MarketValueView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.time.LocalDate

class MarketValueControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/lease-dates")

  val formProvider = new MarketValueFormProvider()
  val form = formProvider(500000)
  lazy val marketValueRoute = routes.MarketValueController.onPageLoad().url
  val thisYear = LocalDate.now().getYear
  val validEffectiveDate = LocalDate.of(thisYear, 1, 1)

  "Market value Controller" - {
    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, marketValueRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[MarketValueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
        .set(MarketValuePage, PayInStages).success.value
        .set(PremiumPage, BigDecimal(1234)).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, marketValueRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[MarketValueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(MarketValue(PayInStages, None, Some(BigDecimal(1234)))))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = emptyUserAnswers
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, marketValueRoute)
            .withFormUrlEncodedBody(
              ("value", PayUpfront.toString),
              ("paySDLTUpfront", "1234"))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

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
          FakeRequest(POST, marketValueRoute)
            .withFormUrlEncodedBody(("value", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MarketValueView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }


    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, marketValueRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, marketValueRoute)
            .withFormUrlEncodedBody(
              ("value", PayUpfront.toString),
              ("paySDLTUpfront", "1234"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}