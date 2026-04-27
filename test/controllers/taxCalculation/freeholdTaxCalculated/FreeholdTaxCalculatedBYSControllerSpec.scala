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

package controllers.taxCalculation.freeholdTaxCalculated

import base.SpecBase
import models.taxCalculation.TaxCalculationFlow
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedBYSView

class FreeholdTaxCalculatedBYSControllerSpec extends SpecBase {

  "FreeholdTaxCalculatedBYS Controller" - {

    "must return OK and the correct view for a GET" in {

      val answers     = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedBYSController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdTaxCalculatedBYSView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to the return task list when the user is not in the freehold tax calculated flow" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedBYSController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
