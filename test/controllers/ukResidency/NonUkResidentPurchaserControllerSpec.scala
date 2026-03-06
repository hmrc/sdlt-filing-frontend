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
import constants.FullReturnConstants.minimalFullReturn
import controllers.routes
import forms.ukResidency.NonUkResidentPurchaserFormProvider
import models.{FullReturn, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukResidency.NonUkResidentPurchaserPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ukResidency.NonUkResidentPurchaserView

import scala.concurrent.Future

class NonUkResidentPurchaserControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new NonUkResidentPurchaserFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val nonUkResidentPurchaserRoute: String = controllers.ukResidency.routes.NonUkResidentPurchaserController.onPageLoad(NormalMode).url

  private lazy val fullReturnComplete = FullReturnConstants.completeFullReturn

  private lazy val testStorn = "TESTSTORN"

  val userAnswersResidentialIndividual: UserAnswers =
    UserAnswers(id = userAnswersId, returnId = Some("RRF-2024-001"), storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))

  val userAnswersAdditionalResidentialCompany: UserAnswers =
    UserAnswers(id = userAnswersId, returnId = Some("RRF-2024-001"), storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))

  val userAnswersNonResidential: UserAnswers =
    UserAnswers(id = userAnswersId, returnId = Some("RRF-2024-001"), storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithPurchaserNonResidential))

  private def fullReturnWithIndividualPurchaser: FullReturn =
    fullReturnComplete.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser1)),
      land = Some(Seq(FullReturnConstants.completeLand))
    )

  private def fullReturnWithCompanyPurchaser: FullReturn =
    fullReturnComplete.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser3)),
      land = Some(Seq(FullReturnConstants.completeLandAdditional))
    )

  private def fullReturnWithPurchaserNonResidential: FullReturn =
    minimalFullReturn.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser2)),
      land = Some(Seq(FullReturnConstants.completeLandNonResidential))
    )

  "NonUkResidentPurchaser Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersResidentialIndividual))
          .build()

      running(application) {
        val request = FakeRequest(GET, nonUkResidentPurchaserRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonUkResidentPurchaserView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Residency CYA on a GET when type of property is non-residential" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNonResidential))
          .build()

      running(application) {
        val request = FakeRequest(GET, nonUkResidentPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersResidentialIndividual.set(NonUkResidentPurchaserPage, true).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request = FakeRequest(GET, nonUkResidentPurchaserRoute)

        val view = application.injector.instanceOf[NonUkResidentPurchaserView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAdditionalResidentialCompany))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the Close Companies page when valid data is submitted and yes selected for company purchaser" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAdditionalResidentialCompany))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url //TODO - DTR-2508 - Sprint 10 - Connect to CloseCompanies page
      }
    }

    "must redirect to the Crown Employment Relief page when valid data is submitted and yes selected for company purchaser" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersResidentialIndividual))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url //TODO - DTR-3002 - Sprint 10 - Connect to CrownEmploymentRelief page
      }
    }

    "must redirect to the Residency CYA page when valid data is submitted and no selected for individual purchaser" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersResidentialIndividual))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
      }
    }

    "must redirect to the Crown Employment Relief page when valid data is submitted and no selected for company purchaser" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAdditionalResidentialCompany))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url //TODO - DTR-3002 - Sprint 10 - Connect to CrownEmploymentRelief page
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[NonUkResidentPurchaserView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request = FakeRequest(GET, nonUkResidentPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkResidentPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
