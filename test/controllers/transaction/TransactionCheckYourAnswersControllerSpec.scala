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

package controllers.transaction

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.incompleteFullReturn
import models.prelimQuestions.TransactionType
import models.transaction._
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.*
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.transaction.PopulateTransactionService
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TransactionCheckYourAnswersControllerSpec
  extends SpecBase
    with SummaryListFluency
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockSessionRepository          = mock[SessionRepository]
  private val mockPopulateTransactionService = mock[PopulateTransactionService]
  private val mockBackendConnector           = mock[StampDutyLandTaxConnector]

  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext    = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockPopulateTransactionService)
    reset(mockBackendConnector)
  }

  private val baseUserAnswers = UserAnswers(
    id       = "12345",
    returnId = Some("AB2346"),
    storn    = "TESTSTORN",
    data     = Json.obj("key" -> "value")
  )

  private val transactionCurrentData = Json.obj(
    "transactionCurrent" -> Json.obj(
      "typeOfTransaction"                             -> "GrantOfLease",
      "transactionEffectiveDate"                      -> "2024-01-01",
      "transactionAddDateOfContract"                  -> false,
      "transactionLinkedTransactions"                 -> false,
      "purchaserEligibleToClaimRelief"                -> false,
      "transactionPartialRelief"                      -> false,
      "considerationsAffectedUncertain"               -> false,
      "transactionDeferringPayment"                   -> false,
      "saleOfBusiness"                                -> false,
      "cap1OrNsbc"                                    -> false,
      "transactionRestrictionsCovenantsAndConditions" -> false,
      "isLandOrPropertyExchanged"                     -> false,
      "transactionExercisingAnOption"                 -> false,
      "transactionAddress" -> Json.obj(
        "houseNumber" -> "1",
        "line1"       -> "Test Street",
        "line2"       -> "Test Area",
        "line3"       -> "Test City",
        "line4"       -> "Test County",
        "line5"       -> "UK",
        "postcode"    -> "AB1 2CD",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        ),
        "addressValidated" -> true
      )
    )
  )

  private def buildFullReturn(propertyType: String) = incompleteFullReturn.copy(
    returnInfo = Some(ReturnInfo(
      version    = Some("1"),
      mainLandID = Some("LAND001")
    )),
    transaction = Some(Transaction()),
    land = Some(
      Seq(
        Land(
          landID                     = Some("LAND001"),
          landResourceRef            = Some("LAND-REF-001"),
          propertyType               = Some(propertyType),
          interestCreatedTransferred = Some("Transfer"),
          address1                   = Some("1 Test Street"),
          address2                   = Some("Test Town"),
          postcode                   = Some("AB1 2CD"),
          localAuthorityNumber       = Some("1234")
        )
      )
    )
  )

  private val completeLandFullReturn = buildFullReturn("NonResidential")

  private def buildCompleteUserAnswers(fullReturn: FullReturn) =
    UserAnswers(
      id         = "12345",
      returnId   = Some("AB2346"),
      storn      = "TESTSTORN",
      fullReturn = Some(fullReturn),
      data       = transactionCurrentData
    )
      .set(TypeOfTransactionPage, TransactionType.GrantOfLease).success.value
      .set(TransactionEffectiveDatePage, LocalDate.of(2024, 1, 1)).success.value
      .set(TransactionAddDateOfContractPage, false).success.value
      .set(TransactionLinkedTransactionsPage, false).success.value
      .set(PurchaserEligibleToClaimReliefPage, false).success.value
      .set(TransactionPartialReliefPage, false).success.value
      .set(ConsiderationsAffectedUncertainPage, false).success.value
      .set(TransactionDeferringPaymentPage, false).success.value
      .set(SaleOfBusinessPage, false).success.value
      .set(Cap1OrNsbcPage, false).success.value
      .set(TransactionRestrictionsCovenantsAndConditionsPage, false).success.value
      .set(IsLandOrPropertyExchangedPage, false).success.value
      .set(TransactionExercisingAnOptionPage, false).success.value

  private val completeUserAnswers         = buildCompleteUserAnswers(completeLandFullReturn)
  private val userAnswersWithValidSession = buildCompleteUserAnswers(completeLandFullReturn)

  private val userAnswersWithTransaction = UserAnswers(
    id         = "12345",
    returnId   = Some("AB2346"),
    storn      = "TESTSTORN",
    fullReturn = Some(
      FullReturn(
        stornId           = "TESTSTORN",
        returnResourceRef = "AB2346",
        transaction       = Some(Transaction())
      )
    )
  )

  "TransactionCheckYourAnswersController" - {

    "onPageLoad" - {

      "must redirect to JourneyRecovery when no session exists" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when returnId is missing" in {

        val ua = completeUserAnswers.copy(returnId = None)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must render the page when data is complete" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to first missing page when required answer missing" in {

        val ua = completeUserAnswers.remove(TransactionEffectiveDatePage).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionEffectiveDateController.onPageLoad(models.CheckMode).url
        }
      }

      "must populate transaction data when transactionCurrent is empty" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersWithTransaction)))

        when(mockPopulateTransactionService.populateTransactionInSession(any(), any()))
          .thenReturn(Success(completeUserAnswers))

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithTransaction))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PopulateTransactionService].toInstance(mockPopulateTransactionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to before you start when population fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersWithTransaction)))

        when(mockPopulateTransactionService.populateTransactionInSession(any(), any()))
          .thenReturn(Failure(new RuntimeException("boom")))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithTransaction))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PopulateTransactionService].toInstance(mockPopulateTransactionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      "must render non grant of lease rows" in {

        val ua = completeUserAnswers
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value
          .set(TotalConsiderationOfTransactionPage, "12").success.value
          .set(TransactionVatIncludedPage, false).success.value
          .set(TransactionFormsOfConsiderationPage, TransactionFormsOfConsiderationAnswers(
            cash                      = "yes",
            debt                      = "no",
            buildingWorks             = "no",
            employment                = "no",
            other                     = "no",
            sharesInAQuotedCompany    = "no",
            sharesInAnUnquotedCompany = "no",
            otherLand                 = "no",
            services                  = "no",
            contingent                = "no"
          )).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render part exchange rows" in {

        val ua = completeUserAnswers
          .set(PurchaserEligibleToClaimReliefPage, true).success.value
          .set(ReasonForReliefPage, ReasonForRelief.PartExchange).success.value
          .set(IsPurchaserRegisteredWithCISPage, true).success.value
          .set(TransactionCisNumberPage, "1234567890").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render sale of business rows" in {

        val ua = completeUserAnswers
          .set(SaleOfBusinessPage, true).success.value
          .set(TransactionSaleOfBusinessAssetsPage, TransactionSaleOfBusinessAssetsAnswers(
            stock                = "yes",
            goodwill             = "no",
            chattelsAndMoveables = "no",
            others               = "no"
          )).success.value
          .set(TotalAssetsConsiderationPage, "12").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render cap1 rows" in {

        val ua = completeUserAnswers
          .set(Cap1OrNsbcPage, true).success.value
          .set(TransactionRulingFollowedPage, TransactionRulingFollowed.Yes).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render restrictions rows" in {

        val ua = completeUserAnswers
          .set(TransactionRestrictionsCovenantsAndConditionsPage, true).success.value
          .set(DescriptionOfRestrictionsPage, "restriction").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render exchanged land rows" in {

        val ua = completeUserAnswers
          .set(IsLandOrPropertyExchangedPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "propertyTypeCheck" - {

        "must render use of land row when property type is NonResidential" in {

          val ua = buildCompleteUserAnswers(buildFullReturn("NonResidential"))
            .set(TransactionUseOfLandOrPropertyPage, TransactionUseOfLandOrPropertyAnswers(
              office              = "yes",
              hotel               = "no",
              shop                = "no",
              warehouse           = "no",
              factory             = "no",
              otherIndustrialUnit = "no",
              other               = "no"
            )).success.value

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
          }
        }

        "must render use of land row when property type is Mixed" in {

          val ua = buildCompleteUserAnswers(buildFullReturn("Mixed"))
            .set(TransactionUseOfLandOrPropertyPage, TransactionUseOfLandOrPropertyAnswers(
              office              = "yes",
              hotel               = "no",
              shop                = "no",
              warehouse           = "no",
              factory             = "no",
              otherIndustrialUnit = "no",
              other               = "no"
            )).success.value

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
          }
        }

        "must not render use of land row when property type is Residential" in {

          val ua = buildCompleteUserAnswers(buildFullReturn("Residential"))

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
          }
        }

        "must not render use of land row when property type is absent" in {

          val fullReturnNoPropertyType = incompleteFullReturn.copy(
            returnInfo = Some(ReturnInfo(
              version    = Some("1"),
              mainLandID = Some("LAND001")
            )),
            transaction = Some(Transaction()),
            land = Some(
              Seq(
                Land(
                  landID                     = Some("LAND001"),
                  landResourceRef            = Some("LAND-REF-001"),
                  propertyType               = None,
                  interestCreatedTransferred = Some("Transfer"),
                  address1                   = Some("1 Test Street"),
                  address2                   = Some("Test Town"),
                  postcode                   = Some("AB1 2CD"),
                  localAuthorityNumber       = Some("1234")
                )
              )
            )
          )

          val ua = buildCompleteUserAnswers(fullReturnNoPropertyType)

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
          }
        }

        "must not render use of land row when land data is absent" in {

          val fullReturnNoLand = incompleteFullReturn.copy(
            returnInfo = Some(ReturnInfo(
              version    = Some("1"),
              mainLandID = Some("LAND001")
            )),
            transaction = Some(Transaction()),
            land        = None
          )

          val ua = buildCompleteUserAnswers(fullReturnNoLand)

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
          }
        }
      }
    }

    "onSubmit" - {

      "must redirect to task list when update succeeds" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswersWithValidSession)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.updateTransaction(any[UpdateTransactionRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateTransactionReturn(updated = true)))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithValidSession))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect back to cya when update fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(completeUserAnswers)))

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))

        when(mockBackendConnector.updateTransaction(any[UpdateTransactionRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateTransactionReturn(updated = false)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to cya when validation fails" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(baseUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to journey recovery when no session exists on submit" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "cross-flow handling" - {

      import services.crossflow.{CrossFlowBody, CrossFlowFailure, CrossFlowTarget, Pages, ReturnSection}
      import services.crossflow.fields.CrossFlowValidationService

      def stubCrossFlow(failures: Seq[CrossFlowFailure]): CrossFlowValidationService =
        new CrossFlowValidationService(Set.empty, Set.empty) {
          override def failuresAffecting(section: ReturnSection, ua: UserAnswers): Seq[CrossFlowFailure] = failures
        }

      def failure(targetPage: services.crossflow.PageId, ruleId: String = "TEST"): CrossFlowFailure =
        CrossFlowFailure(
          ruleId         = ruleId,
          affects        = ReturnSection.Transaction,
          messageKey     = "test.message",
          inlineErrorKey = "test.message",
          body           = CrossFlowBody.Single("test.message"),
          targets        = Seq(CrossFlowTarget(targetPage, "value")),
          headingKey     = "crossflow.relief.heading"
        )

      "must redirect to the relief reason page when a single cross-flow failure targets it" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(failure(Pages.ReliefReason))))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.ReasonForReliefController.onPageLoad(models.CheckMode).url
        }
      }

      "must redirect to the effective date page when a single cross-flow failure targets it" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(failure(Pages.EffectiveDate))))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionEffectiveDateController.onPageLoad(models.CheckMode).url
        }
      }

      "must redirect to the contract date page when a single cross-flow failure targets it" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(failure(Pages.ContractDate))))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionDateOfContractController.onPageLoad(models.CheckMode).url
        }
      }

      "must redirect to the use-of-land-or-property page when a cross-flow failure targets it (Cf-17)" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(failure(Pages.UseOfProperty, "Cf-17"))))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionUseOfLandOrPropertyController.onPageLoad(models.CheckMode).url
        }
      }

      "must fall back to CYA when a cross-flow failure has no recognised target page" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(failure(Pages.LandPropertyType))))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect on the first failure when there are multiple" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Seq(
              failure(Pages.ReliefReason,  "A"),
              failure(Pages.EffectiveDate, "B")
            )))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.ReasonForReliefController.onPageLoad(models.CheckMode).url
        }
      }

      "must render the page normally when there are no cross-flow failures" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(stubCrossFlow(Nil))
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }
  }
}