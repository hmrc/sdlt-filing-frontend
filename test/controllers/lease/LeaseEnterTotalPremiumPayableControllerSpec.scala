/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.lease

import base.SpecBase
import constants.FullReturnConstants.{completeFullReturn, completeTransaction}
import controllers.routes
import forms.lease.LeaseEnterTotalPremiumPayableFormProvider
import models.{FullReturn, NormalMode, Transaction, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.LeaseEnterTotalPremiumPayablePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.lease.LeaseEnterTotalPremiumPayableView

import scala.concurrent.Future

class LeaseEnterTotalPremiumPayableControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new LeaseEnterTotalPremiumPayableFormProvider()
  val form: Form[String] = formProvider()

  lazy val leaseEnterTotalPremiumPayableRoute: String = controllers.lease.routes.LeaseEnterTotalPremiumPayableController.onPageLoad(NormalMode).url

  val userAnswersGrantOfLease: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      submission = None,
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L"))))))

  val userAnswersConveyanceTransfer: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      submission = None,
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("F"))))))

  "LeaseEnterTotalPremiumPayable Controller" - {

    "must return OK and the correct view for a GET when transaction type is L - Grant of Lease" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease)).build()

      running(application) {
        val request = FakeRequest(GET, leaseEnterTotalPremiumPayableRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseEnterTotalPremiumPayableView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when transaction type is L - Grant of Lease and the question has previously been answered" in {

      val userAnswers = userAnswersGrantOfLease.set(LeaseEnterTotalPremiumPayablePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, leaseEnterTotalPremiumPayableRoute)

        val view = application.injector.instanceOf[LeaseEnterTotalPremiumPayableView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, leaseEnterTotalPremiumPayableRoute)
            .withFormUrlEncodedBody(("value", "1000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseEnterTotalPremiumPayableRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[LeaseEnterTotalPremiumPayableView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, leaseEnterTotalPremiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseEnterTotalPremiumPayableRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to return task list when transaction type is not 'A' or 'L' and return Id is present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer.copy(returnId = Some("123456")))).build()

      running(application) {
        val request = FakeRequest(GET, leaseEnterTotalPremiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to Journey recovery when transaction type is not 'A' or 'L' and return Id is not present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer)).build()

      running(application) {
        val request = FakeRequest(GET, leaseEnterTotalPremiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
