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
import models.NormalMode
import play.api.i18n.Messages
import play.api.test.Helpers.running

class PurchaserAgentTaskListSpec extends SpecBase {

  private val fullReturnCompleteWithPurchaserAgent = completeFullReturn
  private val fullReturnCompleteWithOtherAgent = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor)))
  private val fullReturnSomeMandatoryFieldsMissing = completeFullReturn.copy(
    returnAgent = Some(Seq(completeReturnAgent.copy(
      name = Some("Jon"),
      address1 = None,
      isAuthorised = None))))
  private val fullReturnAllMandatoryFieldsMissing = completeFullReturn.copy(
    returnAgent = Some(Seq(completeReturnAgent.copy(
      address1 = None,
      name = None,
      isAuthorised = None))))

  "PurchaserAgentTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when purchaser agent is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.build(fullReturnCompleteWithPurchaserAgent)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserAgentQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when no purchaser agent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.build(fullReturnCompleteWithOtherAgent)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserAgentQuestion.heading")
        }
      }
    }

    ".mandatoryFieldsDefined" - {
      
      "must return a sequence of true if all mandatory fields are defined" in {
        val result = PurchaserAgentTaskList.mandatoryFieldsDefined(fullReturnCompleteWithPurchaserAgent)
        result mustBe Seq(true, true, true)
      }

      "must return a sequence of true and false if some mandatory fields are missing" in {
        val result = PurchaserAgentTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissing)
        result mustBe Seq(true, false, false)
      }

      "must return a sequence of false if all mandatory fields are missing" in {
        val result = PurchaserAgentTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissing)
        result mustBe Seq(false, false, false)
      }
    }

    ".isPurchaserAgentComplete" - {
      
      "must return true if purchaserAgent exists and mandatory fields are defined" in {
        val result = PurchaserAgentTaskList.isPurchaserAgentComplete(fullReturnCompleteWithPurchaserAgent)

        result mustBe true
      }

      "must return false if purchaserAgent exists but some mandatory field are missing" in {
        val result = PurchaserAgentTaskList.isPurchaserAgentComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
      }

      "must return false if purchaserAgent exists but all mandatory fields are missing" in {
        val result = PurchaserAgentTaskList.isPurchaserAgentComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
      }
    }

    ".buildPurchaserAgentRow" - {
      
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnCompleteWithPurchaserAgent)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "purchaserAgentQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.purchaserAgentQuestion.details")
        }
      }

      "must have purchaserAgent Before You Start url and show 'Optional' status when purchaser agent is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnCompleteWithOtherAgent)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
          
          result.status mustBe TLOptional
        }
      }

      "must have purchaserAgent Overview url and show 'Complete' status when all mandatory fields are present in purchaser agent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnCompleteWithPurchaserAgent)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url
          
          result.status mustBe TLCompleted
        }
      }

      "must have purchaserAgent Before You Start url and show 'In progress' status when some mandatory fields are missing from purchaser agent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnSomeMandatoryFieldsMissing)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
          
          result.status mustBe TLInProgress
        }
      }

      "must have purchaserAgent Before You Start url and show 'Optional' status when all mandatory fields are missing from purchaser agent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnAllMandatoryFieldsMissing)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
          
          result.status mustBe TLOptional
        }
      }
    }
  }
}
