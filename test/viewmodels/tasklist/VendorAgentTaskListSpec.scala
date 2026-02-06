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
import play.api.i18n.Messages
import play.api.test.Helpers.running

class VendorAgentTaskListSpec extends SpecBase {

  private val fullReturnCompleteWithVendorAgent = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor)))
  private val fullReturnCompleteWithOtherAgent = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgent)))

  "VendorAgentTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when vendor agent is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.build(fullReturnCompleteWithVendorAgent)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.vendorAgentQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when no vendor agent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.build(fullReturnCompleteWithOtherAgent)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.vendorAgentQuestion.heading")
        }
      }
    }

    ".buildVendorAgentRow" - {
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnCompleteWithVendorAgent)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "vendorAgentQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.vendorAgentQuestion.details")
        }
      }

      "must have VendorAgent Before You Start url and show optional status when vendor agent is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnCompleteWithOtherAgent)

          result.url mustBe controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
          result.status mustBe TLOptional
        }
      }

      "must have VendorAgent Overview url and show completed status when vendor agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnCompleteWithVendorAgent)

          result.url mustBe controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when preliminary section is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }
  }
}
