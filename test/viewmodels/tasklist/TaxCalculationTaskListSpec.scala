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

package viewmodels.tasklist

import base.SpecBase
import config.FrontendAppConfig
import constants.FullReturnConstants.*
import models.TaxCalculation
import play.api.i18n.Messages
import play.api.test.Helpers.running

class TaxCalculationTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnSomeMandatoryFieldsMissingScenario1_3 = fullReturnComplete.copy(
    taxCalculation = Some(TaxCalculation(
      amountPaid = None,
      includesPenalty = Some("YES"),
      taxDue = Some("1000.00")
    )))
  private val fullReturnAllMandatoryFieldsMissingScenario1_3 = fullReturnComplete.copy(
    taxCalculation = Some(TaxCalculation(
      taxDue = None,
      amountPaid = None,
      includesPenalty = None
    )))
  private val fullReturnMandatoryFieldsPresentScenario4 =  fullReturnComplete.copy(
    taxCalculation = Some(TaxCalculation(
      taxDueNPV = Some("1000.00"),
      taxDuePremium = Some("1000.00"),
      amountPaid = Some("1000.00"),
      includesPenalty = Some("YES")
    )))
  private val fullReturnSomeMandatoryFieldsMissingScenario4 =  fullReturnComplete.copy(
    taxCalculation = Some(TaxCalculation(
      taxDueNPV = Some("1000.00"),
      taxDuePremium = None,
      amountPaid = Some("1000.00"),
      includesPenalty = None
    )))
  private val fullReturnAllMandatoryFieldsMissingScenario4 = fullReturnComplete.copy(
    taxCalculation = Some(TaxCalculation(
      taxDueNPV = None,
      taxDuePremium = None,
      amountPaid = None,
      includesPenalty = None
    )))


  "TaxCalculationTaskList" - {

    ".build" - {

      "must return TaskListSection with correct heading when tax calculation is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when tax calculation is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".mandatoryFieldsDefined" - {

      "when scenario 1-3" - {

        "must return a sequence of true if all mandatory fields of tax calc are defined" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnComplete)
          result mustBe Seq(true, true, true)
        }

        "must return a sequence of true and false if some mandatory fields of tax calc are missing" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissingScenario1_3)
          result mustBe Seq(false, true, true)
        }

        "must return a sequence of false if all mandatory fields of tax calc are missing" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissingScenario1_3)
          result mustBe Seq(false, false, false, false)
        }
      }

      "when scenario 4" - {

        "must return a sequence of true if all mandatory fields of tax calc are defined" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnMandatoryFieldsPresentScenario4)
          result mustBe Seq(true, true, true, true)
        }

        "must return a sequence of true and false if some mandatory fields of tax calc are missing" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissingScenario4)
          result mustBe Seq(true, false, false, true)
        }

        "must return a sequence of false if all mandatory fields of tax calc are missing" in {
          val result = TaxCalculationTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissingScenario4)
          result mustBe Seq(false, false, false, false)
        }
      }
    }

    ".isTaxCalculationComplete" - {

      "must return true if tax calculation exists and mandatory fields are defined" in {
        val result = TaxCalculationTaskList.isTaxCalculationComplete(fullReturnComplete)

        result mustBe true
      }

      "must return false if tax calculation exists but some mandatory field are missing" in {
        val result = TaxCalculationTaskList.isTaxCalculationComplete(fullReturnSomeMandatoryFieldsMissingScenario1_3)

        result mustBe false
      }

      "must return false if tax calculation exists but all mandatory fields are missing" in {
        val result = TaxCalculationTaskList.isTaxCalculationComplete(fullReturnAllMandatoryFieldsMissingScenario1_3)

        result mustBe false
      }
    }

    ".buildTaxCalculationRow" - {
      
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result.tagId mustBe "taxCalculationQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
        }
      }

      "must show 'Complete' status when tax calculation is present and all mandatory fields are defined" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show 'In progress' status when tax calculation is present and some mandatory fields are missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnSomeMandatoryFieldsMissingScenario1_3)

          result.status mustBe TLInProgress
        }
      }

      "must show 'Not yet started' status when tax calculation is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(taxCalculation = None))

          result.status mustBe TLNotStarted
        }
      }

      "must show 'Cannot start yet' status and hint when purchaser section is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(purchaser = None))

          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
          result.status mustBe TLCannotStart
        }
      }
      
      "must show 'Cannot start yet' status and hint when vendor section is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(vendor = None))

          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
          result.status mustBe TLCannotStart
        }
      }
      
      "must show 'Cannot start yet' status and hint when land section is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(land = None))

          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
          result.status mustBe TLCannotStart
        }
      }
      
      "must show 'Cannot start yet' status and hint when transaction section is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(transaction = None))

          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
          result.status mustBe TLCannotStart
        }
      }
      
      "must show 'Cannot start yet' status and display hint when vendor agent started but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(completeFullReturn
            .copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = None)))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
        }
      }

      "must show 'Cannot start yet' status and display hint when purchaser agent started but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(completeFullReturn
            .copy(returnAgent = Some(Seq(completeReturnAgent.copy(name = None)))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
        }
      }

      "must show 'Cannot start yet' status and display hint when lease is required but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(transaction = Some(completeTransaction
              .copy(transactionDescription = Some("L"))), lease = Some(completeLease
              .copy(leaseType = None))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
        }
      }

      "must show 'Cannot start yet' status and display hint when residency is required but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(residency = Some(completeResidency
              .copy(isCrownRelief = None)), land = Some(Seq(completeLand
              .copy(propertyType = Some("01"))))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.taxCalculationQuestion.hint")
        }
      }

      "must show 'Not yet started' status and hide hint when no vendor agent exists but purchaser agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(taxCalculation = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when vendor agent is complete and purchaser agent not started" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(taxCalculation = None, returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = Some("Test"))))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when no purchaser agent exists but vendor agent is complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(
            taxCalculation = None, returnAgent = Some(Seq(completeReturnAgentVendor))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when purchaser agent is complete and vendor agent not started" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(taxCalculation = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when lease is not required" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete.copy(taxCalculation = None, lease = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when lease is required and complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result =TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(
              taxCalculation = None,
              transaction = Some(completeTransaction.copy(transactionDescription = Some("L"))),
              lease = Some(completeLease.copy(netPresentValue = Some("100000"), totalPremiumPayable = Some("100000"), isAnnualRentOver1000 = Some("YES"))),
            ))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when residency is not required" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(taxCalculation = None, residency = None, land = Some(Seq(completeLand.copy(propertyType = Some("02"))))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when residency is required and complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete
            .copy(taxCalculation = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must link to the confirm effective date page whether complete or not" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete).url mustBe
            controllers.taxCalculation.routes.TaxCalculationConfirmEffectiveDateOfTransactionController.onPageLoad().url

          TaxCalculationTaskList.buildTaxCalculationRow(emptyFullReturn).url mustBe
            controllers.taxCalculation.routes.TaxCalculationConfirmEffectiveDateOfTransactionController.onPageLoad().url
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with 'Complete' row when tax calculation present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TaxCalculationTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.taxCalculation.routes.TaxCalculationConfirmEffectiveDateOfTransactionController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with 'Not yet started' row when tax calculation absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TaxCalculationTaskList.build(fullReturnComplete.copy(taxCalculation = None))
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.taxCalculation.routes.TaxCalculationConfirmEffectiveDateOfTransactionController.onPageLoad().url
        }
      }
    }
  }

}
