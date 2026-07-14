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
  private val fullReturnSomeMandatoryFieldsMissing = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(address1 = None, name = Some("Jon")))))
  private val fullReturnAllMandatoryFieldsMissing = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(address1 = None, name = None))))

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

    ".mandatoryFieldsDefined" - {

      "must return a sequence of true if all mandatory fields are defined" in {
        val result = VendorAgentTaskList.mandatoryFieldsDefined(fullReturnCompleteWithVendorAgent)
        result mustBe Seq(true, true)
      }

      "must return a sequence of true and false if some mandatory fields are missing" in {
        val result = VendorAgentTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissing)
        result mustBe Seq(true, false)
      }

      "must return a sequence of false if all mandatory fields are missing" in {
        val result = VendorAgentTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissing)
        result mustBe Seq(false, false)
      }
    }

    ".isVendorAgentComplete" - {

      "must return true if VendorAgent exists and mandatory fields are defined" in {
        val result = VendorAgentTaskList.isVendorAgentComplete(fullReturnCompleteWithVendorAgent)

        result mustBe true
      }

      "must return false if VendorAgent exists but some mandatory field are missing" in {
        val result = VendorAgentTaskList.isVendorAgentComplete(fullReturnSomeMandatoryFieldsMissing)

        result mustBe false
      }

      "must return false if VendorAgent exists but all mandatory fields are missing" in {
        val result = VendorAgentTaskList.isVendorAgentComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
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

      "must have VendorAgent Overview url and show 'Complete' status when all mandatory fields are present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnCompleteWithVendorAgent)

          result.url mustBe controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
          result.status mustBe TLCompleted
        }
      }

      "must have VendorAgent Before you Start url and show 'In progress' status when some mandatory fields are missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnSomeMandatoryFieldsMissing)

          result.url mustBe controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
          result.status mustBe TLInProgress
        }
      }

      "must have VendorAgent Before You Start url and show 'Optional' status when all mandatory fields are missing"  in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorAgentTaskList.buildVendorAgentRow(fullReturnAllMandatoryFieldsMissing)

          result.url mustBe controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
          result.status mustBe TLOptional
        }
      }
    }
  }
}
