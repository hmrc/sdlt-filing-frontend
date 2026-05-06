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
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.{NormalMode, UserAnswers}
import models.taxCalculation.TaxCalculationFlow
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedAmountWithPenaltiesView

class FreeholdSdltCalculatedPenaltiesAndInterestControllerSpec extends SpecBase {

  trait Fixture {
    val form = new PenaltiesAndInterestFormProvider()()
    val answersFreeHold: UserAnswers = emptyUserAnswers.set(TaxCalculationFlowPage,
      TaxCalculationFlow.FreeholdTaxCalculated).success.value
    val answersLeasehold: UserAnswers = emptyUserAnswers.set(TaxCalculationFlowPage,
      TaxCalculationFlow.LeaseholdSelfAssessed).success.value
  }

  "PenaltiesAndInterestControllerSpec" - {

    "return OK for GET :: correct flow state" in new Fixture {
      val application: Application = applicationBuilder(userAnswers = Some(answersFreeHold)).build()

      running(application) {
        val request = FakeRequest(GET,
          controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdTaxCalculatedAmountWithPenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "return SEE_OTHER for GET:: incorrect flow state" in new Fixture {
      val application: Application = applicationBuilder(userAnswers = Some(answersLeasehold)).build()

      running(application) {
        val request = FakeRequest(GET,
          controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }

    }

    "return SEE_OTHER for POST : valid formData" in new Fixture {
      val app: Application = applicationBuilder(userAnswers = Some(answersFreeHold)).build()

      running(app) {
        val request = FakeRequest(POST,
          controllers.taxCalculation
            .freeholdTaxCalculated.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "yes"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url
      }
    }

    "return BAD_REQUEST for POST : inValid formData" in new Fixture {
      val application: Application = applicationBuilder(userAnswers = Some(answersFreeHold)).build()

      running(application) {
        val request = FakeRequest(POST,
          controllers.taxCalculation
            .freeholdTaxCalculated.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "wrongFormData"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "return SEE_OTHER for POST : valid formData but inValidFlow" in new Fixture {
      val application: Application = applicationBuilder(userAnswers = Some(answersLeasehold)).build()

      running(application) {
        val request = FakeRequest(POST,
          controllers.taxCalculation
            .freeholdTaxCalculated.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "penaltiesAndInterestNo"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }

}
