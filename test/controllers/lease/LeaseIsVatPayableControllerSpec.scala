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
import constants.FullReturnConstants
import constants.FullReturnConstants.{completeFullReturn, completeTransaction}
import controllers.routes
import forms.lease.LeaseIsVatPayableFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.LeaseIsVatPayablePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.lease.LeaseIsVatPayableView

import scala.concurrent.Future

class LeaseIsVatPayableControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new LeaseIsVatPayableFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val leaseIsVatPayableRoute: String = controllers.lease.routes.LeaseIsVatPayableController.onPageLoad(NormalMode).url

  val userAnswersGrantOfLease: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L"))))))

  val userAnswersConveyanceTransferLease: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("A"))))))

  val userAnswersOtherTransaction: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("O"))))))

  "LeaseIsVatPayable Controller" - {

    "must return OK and the correct view for a GET when transaction type is L - Grant of Lease" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease)).build()

      running(application) {
        val request = FakeRequest(GET, leaseIsVatPayableRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseIsVatPayableView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and when transaction type is L - Grant of Lease" in {

      val userAnswers = userAnswersGrantOfLease.set(LeaseIsVatPayablePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, leaseIsVatPayableRoute)

        val view = application.injector.instanceOf[LeaseIsVatPayableView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when 'Yes' is submitted" in {

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
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    
    "must redirect to total premium payable page when 'No' is submitted and transaction type is L (GrantOfLease)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseEnterTotalPremiumPayableController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to CYA page when 'No' is submitted and transaction type is A (ConveyanceTransferLease)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersConveyanceTransferLease))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to check your answers for a POST when in CheckMode" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.lease.routes.LeaseIsVatPayableController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when when 'No' is submitted and transaction type is not L (GrantOfLease) or A (ConveyanceTransferLease)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersOtherTransaction))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[LeaseIsVatPayableView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, leaseIsVatPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseIsVatPayableRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to return task list when transaction type is not 'A' or 'L' and return Id is present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersOtherTransaction.copy(returnId = Some("123456")))).build()

      running(application) {
        val request = FakeRequest(GET, leaseIsVatPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to Journey recovery when transaction type is not 'A' or 'L' and return Id is not present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersOtherTransaction)).build()

      running(application) {
        val request = FakeRequest(GET, leaseIsVatPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
