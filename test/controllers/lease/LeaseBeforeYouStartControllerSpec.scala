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
import models.{FullReturn, Transaction, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.lease.LeaseBeforeYouStartView

class LeaseBeforeYouStartControllerSpec extends SpecBase {

  val userAnswersGrantOfLease: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L"))))))

  val userAnswersConveyanceTransfer: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("F"))))))
  
  "LeaseBeforeYouStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersGrantOfLease)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to return task list when transaction type is not 'A' or 'L' and return Id is present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer.copy(returnId = Some("123456")))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to Journey recovery when transaction type is not 'A' or 'L' and return Id is not present" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersConveyanceTransfer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
