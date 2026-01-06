/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.{ContractPost201603FormProvider, ExchangeContractsFormProvider}
import play.api.mvc.Call
import play.api.test.Helpers._
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import views.html.scalabuild.ExchangeContractsPreAndPostView

class ExchangeContractsPreAndPostControllerSpec extends ScalaSpecBase {
  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/exchange-contracts-double")
  val exchangeFormProvider = new ExchangeContractsFormProvider()
  val contractPostFormProvider = new ContractPost201603FormProvider()
  val exchangeForm = exchangeFormProvider()
  val contractPostForm = contractPostFormProvider()
  lazy val exchangeContractsPreAndPostRoute = routes.ExchangeContractsPreAndPostController.onPageLoad().url

  "ExchangeContractsPreAndPostController" - {
    "must return OK and the correct view for GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, exchangeContractsPreAndPostRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ExchangeContractsPreAndPostView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(exchangeForm, contractPostForm)(request, messages(application)).toString
      }
    }

    "must return BAD_REQUEST when first question is not answered" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, exchangeContractsPreAndPostRoute)
          .withFormUrlEncodedBody("contract-pre-201603" -> "")
          .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundExchangeForm =
          exchangeForm.bind(Map("contract-pre-201603" -> ""))
        val view = application.injector.instanceOf[ExchangeContractsPreAndPostView]
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundExchangeForm, boundExchangeForm)(request, messages(application)).toString
      }
    }

    "must redirect when first question is false" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, exchangeContractsPreAndPostRoute)
          .withFormUrlEncodedBody("contract-pre-201603" -> "false")
          .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual exchangeContractsPreAndPostRoute
      }
    }

    "must return BAD_REQUEST when first question is true but second is missing" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, exchangeContractsPreAndPostRoute)
          .withFormUrlEncodedBody(
            "contract-pre-201603" -> "true",
            "contract-varied-post-201603" -> ""
          )
          .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundPostForm =
          contractPostForm.bind(Map(
            "contract-pre-201603" -> "true",
            "contract-varied-post-201603" -> ""))
        val view = application.injector.instanceOf[ExchangeContractsPreAndPostView]
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundPostForm, boundPostForm)(request, messages(application)).toString
      }
    }

    "must redirect when both questions are answered correctly" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, exchangeContractsPreAndPostRoute)
          .withFormUrlEncodedBody(
            "contract-pre-201603" -> "true",
            "contract-varied-post-201603" -> "false"
          )
          .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual exchangeContractsPreAndPostRoute
      }
    }
  }
}
