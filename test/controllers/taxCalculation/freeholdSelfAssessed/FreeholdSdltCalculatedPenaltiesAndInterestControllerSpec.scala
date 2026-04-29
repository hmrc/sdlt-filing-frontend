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
import models.taxCalculation.TaxCalculationFlow
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountWithPenaltiesView

class FreeholdSdltCalculatedPenaltiesAndInterestControllerSpec extends SpecBase {

  private val form = new PenaltiesAndInterestFormProvider()()

  "PenaltiesAndInterestControllerSpec" - {

    "return OK and the correct view for a GET" in {

      // TODO: set expected answers min
      val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET,
          controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdSelfAssessedAmountWithPenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }

    }

    // TODO: implement redirect test
    "ridirect" in {

    }
  }

}
