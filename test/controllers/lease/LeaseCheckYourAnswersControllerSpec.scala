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
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.{completeFullReturn, completeTransaction, emptyFullReturn, incompleteLease}
import models.lease.{CreateLeaseRequest, CreateLeaseReturn, UpdateLeaseRequest, UpdateLeaseReturn}
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.lease.PopulateLeaseService
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class LeaseCheckYourAnswersControllerSpec
  extends SpecBase
    with SummaryListFluency
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockSessionRepository          = mock[SessionRepository]
  private val mockPopulateLeaseService       = mock[PopulateLeaseService]
  private val mockBackendConnector           = mock[StampDutyLandTaxConnector]

  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext    = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockPopulateLeaseService)
    reset(mockBackendConnector)
  }

  private val userAnswersNoSession = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = Json.obj("key" -> "value"),
    fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L")))))
  )

  private val userAnswersNoSessionTypeO = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = Json.obj("key" -> "value"),
    fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("O")))))
  )

  private val leaseCurrentData = Json.obj(
    "leaseCurrent" -> Json.obj(
          "typeOfLease" -> "R",
          "leaseStartDate" -> "2000-01-01",
          "leaseEndDate" -> "2026-01-01",
          "rentFreePeriod" -> true,
          "leaseEnterRentFreePeriod" -> "50",
          "annualStartingRent" -> "50.00",
          "leaseStartingRentEndDate" -> "2024-01-01",
          "laterRent" -> true,
          "leaseThousandPoundsThreshold" -> true,
          "leaseIsVatPayable" -> true,
          "enterAnnualRentVat" -> "50.00",
          "leaseEnterTotalPremiumPayable" -> "50.00",
          "leaseNetPresentValue" -> "50.00"
    )
  )

  private val leaseCurrentDataTransactionTypeA = Json.obj(
    "leaseCurrent" -> Json.obj(
      "typeOfLease" -> "R",
      "leaseStartDate" -> "2000-01-01",
      "leaseEndDate" -> "2026-01-01",
      "rentFreePeriod" -> true,
      "leaseEnterRentFreePeriod" -> "50",
      "annualStartingRent" -> "50.00",
      "leaseStartingRentEndDate" -> "2024-01-01",
      "laterRent" -> true,
      "leaseThousandPoundsThreshold" -> true,
      "leaseIsVatPayable" -> true,
      "enterAnnualRentVat" -> "50.00",
    )
  )

  private val incompleteLeaseCurrentData = Json.obj(
    "leaseCurrent" -> Json.obj(
      "leaseStartDate" -> "2000-01-01",
      "leaseEndDate" -> "2026-01-01",
      "rentFreePeriod" -> true,
      "leaseEnterRentFreePeriod" -> "50",
      "annualStartingRent" -> "50.00",
      "leaseStartingRentEndDate" -> "2024-01-01",
      "laterRent" -> true,
      "leaseThousandPoundsThreshold" -> true,
      "leaseIsVatPayable" -> true,
      "enterAnnualRentVat" -> "50.00",
      "leaseEnterTotalPremiumPayable" -> "50.00",
      "leaseNetPresentValue" -> "50.00"
    )
  )

  private val completeUserAnswers = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = leaseCurrentData,
    fullReturn = Some(completeFullReturn.copy(
      returnInfo = Some(ReturnInfo(
        version = Some("1")
      )),
      lease = Some(incompleteLease),
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L")))))
  )

  private val completeUserAnswersTransactionTypeA = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = leaseCurrentDataTransactionTypeA,
    fullReturn = Some(completeFullReturn.copy(
      returnInfo = Some(ReturnInfo(
        version = Some("1")
      )),
      lease = Some(incompleteLease),
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("A")))))
  )

  private val incompleteUserAnswers = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = incompleteLeaseCurrentData,
    fullReturn = Some(emptyFullReturn.copy(
      returnInfo = Some(ReturnInfo(
        version = Some("1")
      )),
      lease = Some(incompleteLease),
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L")))))
  )

  private val completeUserAnswersNoLease = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = leaseCurrentData,
    fullReturn = Some(completeFullReturn.copy(
      returnInfo = Some(ReturnInfo(
        version = Some("1")
      )),
      lease = None,
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L")))))
  )

  private val userAnswersCompleteReturnNoLeaseCurrent = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = Json.obj("key" -> "value"),
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L")))))
  )

  private val userAnswersWithLease = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    fullReturn = Some(
      FullReturn(
        stornId = "TESTSTORN",
        returnResourceRef = "AB2346",
        lease = Some(Lease()),
        transaction = Some(completeTransaction.copy(
          transactionDescription = Some("L"))
        )
      )
    )
  )

  "LeaseCheckYourAnswersController" - {

    "onPageLoad" - {

      "must redirect to JourneyRecovery when no session exists" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSession))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect ReturnTaskList when transaction type is not L or A" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSessionTypeO))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when returnId is missing" in {

        val ua = userAnswersNoSession.copy(returnId = None)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must render the page when data is complete" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {

          val request =
            FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK

          val page = contentAsString(result)

          page must include(messages(application)("Check your answers"))
        }
      }

      "must redirect to first missing page when required answer missing" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(incompleteUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TypeOfLeaseController.onPageLoad(models.CheckMode).url
        }
      }

      "must populate lease data when leaseCurrent is empty" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersCompleteReturnNoLeaseCurrent)))

        when(mockPopulateLeaseService.populateLeaseInSession(any(), any()))
          .thenReturn(Success(completeUserAnswers))

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSession))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PopulateLeaseService].toInstance(mockPopulateLeaseService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to before you start when population fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersWithLease)))

        when(mockPopulateLeaseService.populateLeaseInSession(any(), any()))
          .thenReturn(Failure(new RuntimeException("boom")))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithLease))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PopulateLeaseService].toInstance(mockPopulateLeaseService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to LeaseBeforeYouStart when no lease exists in the full return" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswersNoSession)))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSession))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must not show the Total Premium Payable row when transaction type is 'A'" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswersTransactionTypeA)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswersTransactionTypeA))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK

          val html = contentAsString(result)
          html must not include messages(application)("lease.enterTotalPremiumPayable.checkYourAnswersLabel")
          html must include(messages(application)("Check your answers"))
        }
      }

      "must not show the NPV row when transaction type is 'A'" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswersTransactionTypeA)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswersTransactionTypeA))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.LeaseCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK

          val html = contentAsString(result)
          html must not include messages(application)("lease.leaseNetPresentValue.checkYourAnswersLabel")
          html must include(messages(application)("Check your answers"))
        }
      }
    }


    "onSubmit" - {

      "must redirect to task list when updateLease succeeds" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswers)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.updateLease(any[UpdateLeaseRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLeaseReturn(updated = true)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to task list when createLease succeeds" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswersNoLease)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.createLease(any[CreateLeaseRequest])(any(), any()))
          .thenReturn(Future.successful(CreateLeaseReturn(created = true)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswersNoLease))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect back to cya when updateLease fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(incompleteUserAnswers)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.updateLease(any[UpdateLeaseRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLeaseReturn(updated = false)))

        val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to cya when createLease fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswersNoLease)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.createLease(any[CreateLeaseRequest])(any(), any()))
          .thenReturn(Future.successful(CreateLeaseReturn(created = false)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswersNoLease))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to cya when validation fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersNoSession)))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSession))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to journey recovery when no session exists on submit" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswersNoSession))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.LeaseCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}