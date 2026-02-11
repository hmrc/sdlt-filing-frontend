/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.ExchangeContractsFormProvider
import pages.scalabuild.ExchangeContractsPage
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.ExchangeContractsView
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class ExchangeContractsControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/summary")

  val formProvider = new ExchangeContractsFormProvider()
  val form = formProvider()
  lazy val exchangeContractsRoute = routes.ExchangeContractsController.onPageLoad().url

  "Exchange Contracts Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, exchangeContractsRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ExchangeContractsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(ExchangeContractsPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, exchangeContractsRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ExchangeContractsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, exchangeContractsRoute)
            .withFormUrlEncodedBody(("contract-pre-201603", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, exchangeContractsRoute)
            .withFormUrlEncodedBody(("contract-pre-201603", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("contract-pre-201603" -> ""))

        val view = application.injector.instanceOf[ExchangeContractsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}