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
import forms.lease.LeaseThousandPoundsThresholdFormProvider
import models.prelimQuestions.TransactionType
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.LeaseThousandPoundsThresholdPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.lease.LeaseService
import views.html.lease.LeaseThousandPoundsThresholdView

import scala.concurrent.Future

class LeaseThousandPoundsThresholdControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new LeaseThousandPoundsThresholdFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val leaseThousandPoundsThresholdRoute: String = controllers.lease.routes.LeaseThousandPoundsThresholdController.onPageLoad(NormalMode).url

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


  "LeaseThousandPoundsThreshold Controller" - {

    "must return OK and the correct view for a GET when transaction type is L - Grant of Lease and isOnOrAfterAnnualRentCutOff returns false " in {
      val mockLeaseService = mock[LeaseService]
      when(mockLeaseService.transactionType(any()))
        .thenReturn(Some(TransactionType.GrantOfLease))

      when(mockLeaseService.leaseFlowValidationCheck(any()))
        .thenReturn(None)

      when(mockLeaseService.isOnOrAfterAnnualRentCutOff(any()))
        .thenReturn(false)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[LeaseService].toInstance(mockLeaseService))
        .build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseThousandPoundsThresholdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and when transaction type is L - Grant of Lease" in {
      val mockLeaseService = mock[LeaseService]
      when(mockLeaseService.transactionType(any()))
        .thenReturn(Some(TransactionType.GrantOfLease))

      when(mockLeaseService.leaseFlowValidationCheck(any()))
        .thenReturn(None)

      when(mockLeaseService.isOnOrAfterAnnualRentCutOff(any()))
        .thenReturn(false)

      val userAnswers = userAnswersGrantOfLease.set(LeaseThousandPoundsThresholdPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[LeaseService].toInstance(mockLeaseService))
        .build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val view = application.injector.instanceOf[LeaseThousandPoundsThresholdView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to next page and set LeaseThousandPoundsThresholdPage to false when isOnOrAfterAnnualRentCutOff returns true " in {
      val mockLeaseService = mock[LeaseService]
      val mockSessionRepository = mock[SessionRepository]
      when(mockLeaseService.transactionType(any()))
        .thenReturn(Some(TransactionType.GrantOfLease))

      when(mockLeaseService.leaseFlowValidationCheck(any()))
        .thenReturn(None)

      when(mockLeaseService.isOnOrAfterAnnualRentCutOff(any()))
        .thenReturn(true)

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[LeaseService].toInstance(mockLeaseService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val answersCaptor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(mockSessionRepository).set(answersCaptor.capture())

        answersCaptor.getValue
          .get(LeaseThousandPoundsThresholdPage) mustBe Some(false)
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
          FakeRequest(POST, leaseThousandPoundsThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseThousandPoundsThresholdRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[LeaseThousandPoundsThresholdView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, leaseThousandPoundsThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to LeaseIsVatPayableController when transaction type is not 'L'" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer)).build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseIsVatPayableController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to LeaseIsVatPayableController when transaction type is missing" in {
      val userAnswersNoTransaction = emptyUserAnswers.copy(
        fullReturn = Some(completeFullReturn.copy(
          submission = None,
          transaction = Some(completeTransaction.copy(
            transactionDescription = None)))))

      val application = applicationBuilder(userAnswers = Some(userAnswersNoTransaction)).build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseIsVatPayableController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to LeaseIsVatPayableController when transaction type is not L" in {
      val userAnswersNoTransaction = emptyUserAnswers.copy(
        fullReturn = Some(completeFullReturn.copy(
          submission = None,
          transaction = Some(completeTransaction.copy(
            transactionDescription = Some("A"))))))

      val application = applicationBuilder(userAnswers = Some(userAnswersNoTransaction)).build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.lease.routes.LeaseIsVatPayableController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to lease flow validation redirect when leaseFlowValidationCheck returns Some" in {
      val mockLeaseService = mock[LeaseService]
      val validationRedirect = Call("GET", "/validation-redirect")

      when(mockLeaseService.transactionType(any())).thenReturn(Some(TransactionType.GrantOfLease))
      when(mockLeaseService.leaseFlowValidationCheck(any())).thenReturn(Some(validationRedirect))

      val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
        .overrides(bind[LeaseService].toInstance(mockLeaseService))
        .build()

      running(application) {
        val request = FakeRequest(GET, leaseThousandPoundsThresholdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual validationRedirect.url
      }
    }
  }
}