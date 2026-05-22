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
import models.taxCalculation.TaxCalculationFlow
import models.{FullReturn, Land, Residency, ReturnInfo, Transaction, UserAnswers}
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdSelfAssessed.FreeholdCannotCalculateSdltDueView

class FreeholdCannotCalculateSdltDueControllerSpec extends SpecBase {

  private val value: Option[String] = Some("taxCalculation.cannotCalculateSdltDue.reason1")

  private val freeholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId           = "STORN",
        returnResourceRef = "REF",
        returnInfo        = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction       = Some(Transaction(
          effectiveDate          = Some("2026-01-01"),
          totalConsideration     = Some(BigDecimal(300000)),
          claimingRelief         = Some("no"),
          transactionDescription = Some("F"),
          isLinked               = Some("yes")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land      = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value

  "FreeholdCannotCalculateSdltDue Controller" - {

    "must return OK and the correct view for a GET when valid reason is matched" in {
      val application = applicationBuilder(userAnswers = Some(freeholdAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdCannotCalculateSdltDueController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdCannotCalculateSdltDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(value)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when no valid reason is matched" in {
      val wrongAnswers = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(isLinked = Some("no"))))
      ))
      val application = applicationBuilder(userAnswers = Some(wrongAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdCannotCalculateSdltDueController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdCannotCalculateSdltDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, messages(application)).toString
      }
    }

    "must redirect to the return task list when the user is not in the freehold self-assessed flow" in {
      val answers     = freeholdAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdCannotCalculateSdltDueController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

  }
}
