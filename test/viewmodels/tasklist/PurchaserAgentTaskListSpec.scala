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

    ".isPurchaserAgentComplete" - {

      "must return true if purchaserAgent exists and agent name is defined" in {
          val result = PurchaserAgentTaskList.isPurchaserAgentComplete(fullReturnCompleteWithPurchaserAgent)

          result mustBe true
      }

      "must return false if purchaserAgent but agent name is missing" in {
          val result = PurchaserAgentTaskList.isPurchaserAgentComplete(fullReturnCompleteWithPurchaserAgent
            .copy(returnAgent = Some(Seq(completeReturnAgent
              .copy(name = None)))))

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

      "must have purchaserAgent Before You Start url and show optional status when purchaser agent is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnCompleteWithOtherAgent)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
          result.status mustBe TLOptional
        }
      }

      "must have purchaserAgent Overview url and show completed status when purchaser agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(fullReturnCompleteWithPurchaserAgent)

          result.url mustBe controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url
        }
      }

      "must show cannot start status when preliminary section is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserAgentTaskList.buildPurchaserAgentRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }
  }
}
