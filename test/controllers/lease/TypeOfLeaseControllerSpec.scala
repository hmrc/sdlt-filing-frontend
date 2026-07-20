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
import forms.lease.TypeOfLeaseFormProvider
import models.lease.TypeOfLease
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.TypeOfLeasePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import services.lease.LeaseService
import views.html.lease.TypeOfLeaseView

import scala.concurrent.Future

class TypeOfLeaseControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val typeOfLeaseRoute: String =
    controllers.lease.routes.TypeOfLeaseController.onPageLoad(NormalMode).url

  val formProvider = new TypeOfLeaseFormProvider()
  val form: Form[TypeOfLease] = formProvider()

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

  /** Cf-5a — F30, lease type must be Residential when main land type is 01/04.
   *  Inline error key is what surfaces on the form when binding fails. */
  private val cf5aFailure = CrossFlowFailure(
    ruleId         = "Cf-5a",
    affects        = ReturnSection.Lease,
    messageKey     = "crossflow.lease.Cf-5a.body",
    inlineErrorKey = "crossflow.lease.Cf-5a.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5a.body"),
    targets        = Seq(CrossFlowTarget(Pages.LeaseType, "value")),
    headingKey     = "crossflow.lease.heading"
  )

  private val cf5bFailure = cf5aFailure.copy(
    ruleId         = "Cf-5b",
    messageKey     = "crossflow.lease.Cf-5b.body",
    inlineErrorKey = "crossflow.lease.Cf-5b.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5b.body")
  )

  private val cf5cFailure = cf5aFailure.copy(
    ruleId         = "Cf-5c",
    messageKey     = "crossflow.lease.Cf-5c.body",
    inlineErrorKey = "crossflow.lease.Cf-5c.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5c.body")
  )

  private def crossFlowWith(failures: Seq[CrossFlowFailure]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] =
        if (page == Pages.LeaseType) failures else Nil
    }

  private val crossFlowSilent = crossFlowWith(Nil)

  private def stubLeaseService(redirect: Option[Call] = None): LeaseService = {
    val mockService = mock[LeaseService]
    when(mockService.leaseFlowValidationCheck(any())).thenReturn(redirect)
    mockService
  }

  "TypeOfLease Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when leaseFlowValidationCheck allows continuation" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(bind[LeaseService].toInstance(stubLeaseService()))
          .build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TypeOfLeaseView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = userAnswersGrantOfLease
          .set(TypeOfLeasePage, TypeOfLease.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[LeaseService].toInstance(stubLeaseService()))
          .build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val view = application.injector.instanceOf[TypeOfLeaseView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(TypeOfLease.values.head), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to whatever leaseFlowValidationCheck returns when it returns Some(redirect)" in {

        val redirect = Call("GET", "/lease/diverted")

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(bind[LeaseService].toInstance(stubLeaseService(Some(redirect))))
          .build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual redirect.url
        }
      }

      "must redirect to return task list when transaction type is not 'A' or 'L' and return Id is present" in {

        val redirect = controllers.routes.ReturnTaskListController.onPageLoad()

        val userAnswers = userAnswersConveyanceTransfer.copy(returnId = Some("123456"))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[LeaseService].toInstance(stubLeaseService(Some(redirect))))
          .build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to Journey recovery when transaction type is not 'A' or 'L' and return Id is not present" in {

        val redirect = controllers.routes.JourneyRecoveryController.onPageLoad()

        val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer))
          .overrides(bind[LeaseService].toInstance(stubLeaseService(Some(redirect))))
          .build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, typeOfLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted and there are no cross-flow failures" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[CrossFlowValidationService].toInstance(crossFlowSilent),
              bind[LeaseService].toInstance(stubLeaseService())
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody(("value", TypeOfLease.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must persist the chosen lease type in session before redirecting" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[CrossFlowValidationService].toInstance(crossFlowSilent),
              bind[LeaseService].toInstance(stubLeaseService())
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody(("value", TypeOfLease.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          org.mockito.Mockito.verify(mockSessionRepository).set(any[UserAnswers])
        }
      }

      "must return Bad Request and errors when invalid (unparseable) form data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowSilent),
            bind[LeaseService].toInstance(stubLeaseService())
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[TypeOfLeaseView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return Bad Request when cross-flow rule Cf-5a fires (R lease vs property type 02/03)" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowWith(Seq(cf5aFailure))),
            bind[LeaseService].toInstance(stubLeaseService())
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody("value" -> "R")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include(messages(application)("crossflow.lease.Cf-5a.inline"))
        }
      }

      "must return Bad Request when cross-flow rule Cf-5b fires (M lease vs property type that isn't 02)" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowWith(Seq(cf5bFailure))),
            bind[LeaseService].toInstance(stubLeaseService())
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody("value" -> "M")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include(messages(application)("crossflow.lease.Cf-5b.inline"))
        }
      }

      "must return Bad Request when cross-flow rule Cf-5c fires (N lease vs property type that isn't 03)" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowWith(Seq(cf5cFailure))),
            bind[LeaseService].toInstance(stubLeaseService())
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody("value" -> "N")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include(messages(application)("crossflow.lease.Cf-5c.inline"))
        }
      }

      "must not persist to session when a cross-flow failure is returned" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowWith(Seq(cf5aFailure))),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[LeaseService].toInstance(stubLeaseService())
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody("value" -> "R")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          org.mockito.Mockito.verify(mockSessionRepository, org.mockito.Mockito.never).set(any[UserAnswers])
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, typeOfLeaseRoute)
              .withFormUrlEncodedBody(("value", TypeOfLease.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}