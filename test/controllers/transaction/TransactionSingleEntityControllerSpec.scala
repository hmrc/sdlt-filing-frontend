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
import constants.FullReturnConstants.emptyFullReturn
import models.{CheckMode, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import services.transaction.PopulateTransactionService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TransactionSingleEntityControllerSpec extends SpecBase with MockitoSugar {

  lazy val singleEntityRoute: String =
    controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url

  /** F23-32 FTB property-type failure. Targets the relief reason and land property type pages. */
  private val propertyTypeFailure = CrossFlowFailure(
    ruleId         = "F23-32",
    affects        = ReturnSection.Transaction,
    messageKey     = "crossflow.relief.firstTimeBuyer.notResidential",
    inlineErrorKey = "crossflow.relief.firstTimeBuyer.notResidential.inline",
    body           = CrossFlowBody.Single("crossflow.relief.firstTimeBuyer.notResidential"),
    targets        = Seq(
      CrossFlowTarget(Pages.ReliefReason,     "value"),
      CrossFlowTarget(Pages.LandPropertyType, "value")
    ),
    headingKey     = "crossflow.relief.heading"
  )

  /** F23-34 Pre-completion effective-date failure. Targets the relief reason and effective date pages. */
  private val effectiveDateFailure = CrossFlowFailure(
    ruleId         = "F23-34",
    affects        = ReturnSection.Transaction,
    messageKey     = "crossflow.relief.preCompletion.dateTooEarly",
    inlineErrorKey = "crossflow.relief.preCompletion.dateTooEarly.inline",
    body           = CrossFlowBody.Single("crossflow.relief.preCompletion.dateTooEarly"),
    targets        = Seq(
      CrossFlowTarget(Pages.ReliefReason,  "value"),
      CrossFlowTarget(Pages.EffectiveDate, "value")
    ),
    headingKey     = "crossflow.relief.heading"
  )

  /** F25 contract-date failure. Targets the relief reason and contract date pages. */
  private val contractDateFailure = CrossFlowFailure(
    ruleId         = "F25-contract",
    affects        = ReturnSection.Transaction,
    messageKey     = "crossflow.relief.multipleDwellings.contractDateTooLate",
    inlineErrorKey = "crossflow.relief.multipleDwellings.contractDateTooLate.inline",
    body           = CrossFlowBody.Single("crossflow.relief.multipleDwellings.contractDateTooLate"),
    targets        = Seq(
      CrossFlowTarget(Pages.ReliefReason, "value"),
      CrossFlowTarget(Pages.ContractDate, "value")
    ),
    headingKey     = "crossflow.relief.heading"
  )

  /** Generic relief-reason-only failure (falls through to default CTA / continue URL). */
  private val reliefReasonOnlyFailure = CrossFlowFailure(
    ruleId         = "F23-36",
    affects        = ReturnSection.Transaction,
    messageKey     = "crossflow.relief.freeport.outsideWindow",
    inlineErrorKey = "crossflow.relief.freeport.outsideWindow.inline",
    body           = CrossFlowBody.Single("crossflow.relief.freeport.outsideWindow"),
    targets        = Seq(CrossFlowTarget(Pages.ReliefReason, "value")),
    headingKey     = "crossflow.relief.heading"
  )

  /** Cf-17 Mural BF failure. Targets the use-of-property page only. */
  private val useOfPropertyFailure = CrossFlowFailure(
    ruleId         = "Cf-17",
    affects        = ReturnSection.Transaction,
    messageKey     = "crossflow.transaction.Cf-17.body",
    inlineErrorKey = "crossflow.transaction.Cf-17.body",
    body           = CrossFlowBody.WithBullets(
      leadKey    = "crossflow.transaction.Cf-17.body",
      bulletKeys = Seq(
        "crossflow.transaction.Cf-17.bullet1",
        "crossflow.transaction.Cf-17.bullet2"
      )
    ),
    targets        = Seq(CrossFlowTarget(Pages.UseOfProperty, "value")),
    headingKey     = "crossflow.transaction.Cf-17.heading"
  )

  private def crossFlowWithFailures(failures: Seq[CrossFlowFailure]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def failuresAffecting(section: ReturnSection, ua: UserAnswers): Seq[CrossFlowFailure] =
        failures
    }

  private val crossFlowNoFailures = crossFlowWithFailures(Nil)

  private val populatePassthrough = new PopulateTransactionService() {
    override def populateTransactionInSession(transaction: Transaction, ua: UserAnswers) =
      Success(ua)
  }

  private val populateFails = new PopulateTransactionService() {
    override def populateTransactionInSession(transaction: Transaction, ua: UserAnswers) =
      Failure(new IllegalStateException("populate failed"))
  }

  private def userAnswersWithTransaction(transaction: Option[Transaction]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(transaction = transaction)))

  private val userAnswersWithCommittedTransaction =
    userAnswersWithTransaction(Some(Transaction(transactionID = Some("TXN001"))))

  private val userAnswersWithoutTransaction =
    userAnswersWithTransaction(None)

  /** Common app builder for happy-path tests. */
  private def appWith(
                       userAnswers:  UserAnswers,
                       crossFlow:    CrossFlowValidationService,
                       populate:     PopulateTransactionService = populatePassthrough,
                       session:      SessionRepository           = mock[SessionRepository]
                     ) = {
    when(session.set(any())) thenReturn Future.successful(true)

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[CrossFlowValidationService].toInstance(crossFlow),
        bind[PopulateTransactionService].toInstance(populate),
        bind[SessionRepository].toInstance(session)
      )
      .build()
  }

  "TransactionSingleEntity Controller" - {

    "onPageLoad" - {

      "must redirect to TransactionBeforeYouStart when the user has no committed transaction" in {

        val application = appWith(userAnswersWithoutTransaction, crossFlowNoFailures)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to TransactionBeforeYouStart when populate fails" in {

        val application = appWith(userAnswersWithCommittedTransaction, crossFlowNoFailures, populateFails)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      // -----------------------------------------------------------------------
      // Post-population: failure rendering.
      // -----------------------------------------------------------------------

      "must return OK and render the view when there is a transaction failure" in {

        val crossFlow   = crossFlowWithFailures(Seq(propertyTypeFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the failure's body message" in {

        val crossFlow   = crossFlowWithFailures(Seq(propertyTypeFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.firstTimeBuyer.notResidential"))
        }
      }

      "must render the failure's heading" in {

        val crossFlow   = crossFlowWithFailures(Seq(propertyTypeFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.heading"))
        }
      }

      "must render the first failure when multiple failures are present" in {

        val crossFlow = crossFlowWithFailures(Seq(propertyTypeFailure, contractDateFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.firstTimeBuyer.notResidential"))
        }
      }

      "must redirect to TransactionCheckYourAnswers when there are no transaction failures" in {

        val application = appWith(userAnswersWithCommittedTransaction, crossFlowNoFailures)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must render the Cf-17 heading and body when a Mural Business Function failure is surfaced" in {

        val crossFlow   = crossFlowWithFailures(Seq(useOfPropertyFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.transaction.Cf-17.heading"))
          content must include(messages(application)("crossflow.transaction.Cf-17.body"))
        }
      }
    }

    "onPageLoad continue URL routing" - {

      "must point Continue at the relief reason page in CheckMode when failure targets land property type" in {

        val crossFlow   = crossFlowWithFailures(Seq(propertyTypeFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url)
        }
      }

      "must point Continue at the effective date page in CheckMode when failure targets effective date" in {

        val crossFlow   = crossFlowWithFailures(Seq(effectiveDateFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url)
        }
      }

      "must point Continue at the contract date page in CheckMode when failure targets contract date" in {

        val crossFlow   = crossFlowWithFailures(Seq(contractDateFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(CheckMode).url)
        }
      }

      "must point Continue at the use-of-land-or-property page in CheckMode when failure targets use of property (Cf-17)" in {

        val crossFlow   = crossFlowWithFailures(Seq(useOfPropertyFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url)
        }
      }

      "must default Continue to the relief reason page when targets don't match a date/property page" in {

        val crossFlow   = crossFlowWithFailures(Seq(reliefReasonOnlyFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url)
        }
      }
    }

    "onPageLoad CTA key routing" - {

      "must use the property-type CTA key when failure targets land property type" in {

        val crossFlow   = crossFlowWithFailures(Seq(propertyTypeFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.cta.changePropertyType"))
        }
      }

      "must use the effective-date CTA key when failure targets effective date" in {

        val crossFlow   = crossFlowWithFailures(Seq(effectiveDateFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.cta.changeEffectiveDate"))
        }
      }

      "must use the contract-date CTA key when failure targets contract date" in {

        val crossFlow   = crossFlowWithFailures(Seq(contractDateFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.cta.changeContractDate"))
        }
      }

      "must use the use-of-property CTA key when failure targets use of property (Cf-17)" in {

        val crossFlow   = crossFlowWithFailures(Seq(useOfPropertyFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.transaction.cta.enterUseOfProperty"))
        }
      }

      "must default to the relief reason CTA key when no specific target page is matched" in {

        val crossFlow   = crossFlowWithFailures(Seq(reliefReasonOnlyFailure))
        val application = appWith(userAnswersWithCommittedTransaction, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.relief.cta.changeReliefReason"))
        }
      }
    }

    "session repository" - {

      "must persist the populated UserAnswers before rendering" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val crossFlow = crossFlowWithFailures(Seq(propertyTypeFailure))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCommittedTransaction))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlow),
            bind[PopulateTransactionService].toInstance(populatePassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
          org.mockito.Mockito.verify(mockSessionRepository).set(any[UserAnswers])
        }
      }
    }

    "auth and prerequisites" - {

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}