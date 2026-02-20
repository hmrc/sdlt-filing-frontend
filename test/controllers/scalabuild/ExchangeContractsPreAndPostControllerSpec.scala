/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.{ContractPost201603FormProvider, ExchangeContractsFormProvider}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild.{ContractPost201603Page, ExchangeContractsPage}
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.ExchangeContractsPreAndPostView

class ExchangeContractsPreAndPostControllerSpec extends AnyFreeSpec with ScalaSpecBase {

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

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
                          .set(ContractPost201603Page, false).success.value
                          .set(ExchangeContractsPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, exchangeContractsPreAndPostRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[ExchangeContractsPreAndPostView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(exchangeForm.fill(true), contractPostForm.fill(false))(request, messages(application)).toString
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
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
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
        redirectLocation(result).value mustEqual routes.RelevantRentController.onPageLoad().url
      }
    }
  }
}
