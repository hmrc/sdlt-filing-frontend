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

package controllers.ukResidency

import base.SpecBase
import constants.FullReturnConstants
import controllers.routes
import forms.ukResidency.CloseCompanyFormProvider
import models.{FullReturn, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ukResidency.CloseCompanyPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ukResidency.CloseCompanyView

import scala.concurrent.Future

class CloseCompanyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CloseCompanyFormProvider()
  val form = formProvider()

  lazy val closeCompanyRoute = controllers.ukResidency.routes.CloseCompanyController.onPageLoad(NormalMode).url

  private lazy val testStorn = "TESTSTORN"

  val userAnswersWithCompanyPurchaser: UserAnswers =
    UserAnswers(id = userAnswersId, returnId = Some("RRF-2024-001"), storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))

  private def fullReturnWithCompanyPurchaser: FullReturn =
    FullReturnConstants.completeFullReturn.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser3)),
      land = Some(Seq(FullReturnConstants.completeLandAdditional))
    )

  val userAnswersWithIndividualPurchaser: UserAnswers =
    UserAnswers(id = userAnswersId, returnId = Some("RRF-2024-001"), storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))

  private def fullReturnWithIndividualPurchaser: FullReturn =
    FullReturnConstants.completeFullReturn.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser3.copy(isCompany = Some("NO")))),
      land = Some(Seq(FullReturnConstants.completeLandAdditional))
    )
  
  "CloseCompany Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser)).build()

      running(application) {
        val request = FakeRequest(GET, closeCompanyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CloseCompanyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithCompanyPurchaser.set(CloseCompanyPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, closeCompanyRoute)

        val view = application.injector.instanceOf[CloseCompanyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }
    "must redirect to JourneyRecovery when purchaser is not a company on GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser)).build()

      running(application) {

        val request = FakeRequest(GET, closeCompanyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
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
          FakeRequest(POST, closeCompanyRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(CloseCompanyPage) mustBe Some(true)
      }
    }

    "must redirect to ReturnTaskList when no is submitted" in {

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
          FakeRequest(POST, closeCompanyRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(CloseCompanyPage) mustBe Some(false)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, closeCompanyRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CloseCompanyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, closeCompanyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, closeCompanyRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
