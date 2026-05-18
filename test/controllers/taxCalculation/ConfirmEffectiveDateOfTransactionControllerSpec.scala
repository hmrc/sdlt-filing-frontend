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

package controllers.taxCalculation

import base.SpecBase
import forms.taxCalculation.ConfirmEffectiveDateOfTransactionFormProvider
import models.{CheckMode, FullReturn, Land, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.ConfirmEffectiveDateOfTransactionPage
import play.api.inject.bind
import play.api.test
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.taxCalculation.ConfirmEffectiveDateOfTransactionYesNoView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmEffectiveDateOfTransactionControllerSpec extends SpecBase with MockitoSugar {

  private val mockSessionRepository = mock[SessionRepository]

  private val formProvider = new ConfirmEffectiveDateOfTransactionFormProvider()

  private def form = formProvider()

  implicit val request: FakeRequest[_] = FakeRequest()

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val onPageLoadRoute: String = controllers.taxCalculation.routes.ConfirmEffectiveDateOfTransactionController.onPageLoad().url

  lazy val onSubmitRoute: String = controllers.taxCalculation.routes.ConfirmEffectiveDateOfTransactionController.onSubmit().url

  lazy val noReturnReferenceControllerRoute: String = controllers.routes.NoReturnReferenceController.onPageLoad().url

  lazy val returnTaskListControllerRoute: String = controllers.routes.ReturnTaskListController.onPageLoad().url

  private val userAnswersWithEffectiveDate: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
    stornId = "STORN",
    returnResourceRef = "REF",
    returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
    transaction = Some(Transaction(
      effectiveDate = Some("2019-04-01"),
      totalConsideration = Some(BigDecimal(300000)),
      claimingRelief = Some("no"),
      transactionDescription = Some("F"),
      isLinked = Some("no")
    )),
    land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
  )))


  "ConfirmEffectiveDateOfTransactionController" - {
    "onPageLoad" - {
      "must return Ok and the correct view when UserAnswers contains  valid effective date of transaction data " in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(Some(userAnswersWithEffectiveDate))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(app, request).value

          val view = app.injector.instanceOf[ConfirmEffectiveDateOfTransactionYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, "1 April 2019")(request, messages(app)).toString

        }

      }

      "must redirect to NoReturnReferenceController when full return data is missing " in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual noReturnReferenceControllerRoute

        }

      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswersWithConfirmEffectiveDateOfTransaction = userAnswersWithEffectiveDate.set(ConfirmEffectiveDateOfTransactionPage, true).success.value
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val app = applicationBuilder(Some(userAnswersWithConfirmEffectiveDateOfTransaction))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(app, request).value

          val view = app.injector.instanceOf[ConfirmEffectiveDateOfTransactionYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), "1 April 2019")(request, messages(app)).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect to TaxCalculationBeforeYouStartController when user selects `YES` " in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(Some(userAnswersWithEffectiveDate))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to TransactionEffectiveDateController when user selects `NO`" in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(Some(userAnswersWithEffectiveDate))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody(("value", "false"))
          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
        }

      }

      "must return BAD_REQUEST when there is error in the form " in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(Some(userAnswersWithEffectiveDate))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "yes")
          val result = route(app, request).value

          val formWithErrors = form.bind(Map("value" -> "invalid-value"))

          val view = app.injector.instanceOf[ConfirmEffectiveDateOfTransactionYesNoView]

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(formWithErrors, "1 April 2019")(request, messages(app)).toString
        }

      }

      "must redirect to NoReturnReferenceController when userAnswers does not have effective date of transaction" in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val userAnswersWithoutEffectiveDate: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
          stornId = "STORN",
          returnResourceRef = "REF",
          returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
          transaction = Some(Transaction(
            totalConsideration = Some(BigDecimal(300000)),
            claimingRelief = Some("no"),
            transactionDescription = Some("F"),
            isLinked = Some("no")
          )),
          land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
        )))

        val app = applicationBuilder(Some(userAnswersWithoutEffectiveDate))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(app) {

          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual returnTaskListControllerRoute
        }
      }

    }
  }

}
