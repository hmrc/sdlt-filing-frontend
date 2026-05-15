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

package controllers.taxCalculation.freeholdSelfAssessed

import base.SpecBase
import forms.taxCalculation.SdltSelfAssessmentFormProvider
import models.taxCalculation.TaxCalculationFlow
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.taxCalculation.shared.SdltSelfAssessmentView

import scala.concurrent.Future

class FreeholdSelfAssessedSdltSelfAssessmentControllerSpec extends SpecBase with MockitoSugar {

  private val form       = new SdltSelfAssessmentFormProvider()()
  private val sectionKey = "site.taxCalculation.freeholdSelfAssessed.section"

  private val freeholdAnswers: UserAnswers =
    emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value

  private val wrongFlowAnswers: UserAnswers =
    emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

  private lazy val onPageLoadRoute = routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(NormalMode).url
  private lazy val onSubmitRoute   = routes.FreeholdSelfAssessedSdltSelfAssessmentController.onSubmit(NormalMode).url

  private def appWith(answers: UserAnswers): Application = {
    val mockSession = mock[SessionRepository]
    when(mockSession.set(any())).thenReturn(Future.successful(true))
    applicationBuilder(userAnswers = Some(answers))
      .overrides(bind[SessionRepository].toInstance(mockSession))
      .build()
  }

  "FreeholdSelfAssessedSdltSelfAssessmentController" - {

    "GET" - {

      "must render an empty form when no answer has been saved" in {
        val app = appWith(freeholdAnswers)

        running(app) {
          val request  = FakeRequest(GET, onPageLoadRoute)
          val result   = route(app, request).value
          val view     = app.injector.instanceOf[SdltSelfAssessmentView]
          val expected = view(form, routes.FreeholdSelfAssessedSdltSelfAssessmentController.onSubmit(NormalMode), sectionKey)(request, messages(app)).toString

          status(result) mustEqual OK
          contentAsString(result) mustEqual expected
        }
      }

      "must pre-populate the form with the saved answer when one exists" in {
        val withAnswer = freeholdAnswers.set(FreeholdSelfAssessedAmountPage, "9999").success.value
        val app        = appWith(withAnswer)

        running(app) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(app, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("value=\"9999\"")
        }
      }

      "must redirect to the return task list when the user is in a different flow" in {
        val app = applicationBuilder(userAnswers = Some(wrongFlowAnswers)).build()

        running(app) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }

    "POST" - {

      "must save the answer and redirect on a valid submission" in {
        val app = appWith(freeholdAnswers)

        running(app) {
          val request = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody("value" -> "5000")
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedTotalAmountDueController.onPageLoad(NormalMode).url
        }
      }

      "must return Bad Request and re-render the view when the form is invalid" in {
        val app = appWith(freeholdAnswers)

        running(app) {
          val request = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody("value" -> "")
          val result  = route(app, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must redirect to the return task list when the user is in a different flow" in {
        val app = applicationBuilder(userAnswers = Some(wrongFlowAnswers)).build()

        running(app) {
          val request = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody("value" -> "5000")
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }
  }
}
