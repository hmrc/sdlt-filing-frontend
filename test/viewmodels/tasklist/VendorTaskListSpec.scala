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

class VendorTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnCompleteWithOneMainVendor = fullReturnComplete.copy(
    vendor = Some(Seq(completeVendor)))
  private val fullReturnCompleteWithMultipleVendors = completeFullReturn.copy(
    vendor = Some(Seq(completeVendor, completeVendor2, completeVendor3))
  )
  private val fullReturnAllMandatoryFieldsMissing = fullReturnCompleteWithOneMainVendor.copy(
    vendor = Some(Seq(completeVendor.copy(name = None, address1 = None))))
  private val fullReturnSomeMandatoryFieldsMissing = fullReturnCompleteWithOneMainVendor.copy(
    vendor = Some(Seq(completeVendor.copy(name = Some("Batman"), address1 = None))))
  private val fullReturnMissingVendor = fullReturnComplete.copy(vendor = None)

  "VendorTaskList" - {

    ".build" - {
      
      "must return TaskListSection with correct heading when vendor is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.vendorQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when vendor is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.vendorQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".mandatoryFieldsDefined" - {
      
      "must return a sequence of true if all mandatory fields of main Vendor are defined" in {
        val result = VendorTaskList.mandatoryFieldsDefined(fullReturnCompleteWithOneMainVendor)
        result mustBe Seq(true, true)
      }

      "must return a sequence of true and false if some mandatory fields of main Vendor are missing" in {
        val result = VendorTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissing)
        result mustBe Seq(true, false)
      }

      "must return a sequence of false if all mandatory fields of main Vendor are missing" in {
        val result = VendorTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissing)
        result mustBe Seq(false, false)
      }
    }

    ".isVendorComplete" - {
      
      "must return true if vendor exists and mandatory fields are defined" in {
          val result = VendorTaskList.isVendorComplete(fullReturnComplete)

          result mustBe true
      }

      "must return false if vendor exists but some mandatory field are missing" in {
          val result = VendorTaskList.isVendorComplete(fullReturnAllMandatoryFieldsMissing)

          result mustBe false
      }

      "must return false if vendor exists but all mandatory fields are missing" in {
          val result = VendorTaskList.isVendorComplete(fullReturnAllMandatoryFieldsMissing)

          result mustBe false
      }
    }

    ".buildVendorRow" - {
      
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnComplete)

          result.tagId mustBe "vendorQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.vendorQuestion.details")
        }
      }

      "must have Vendor Before You Start url when main vendor is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnMissingVendor)

          result.url mustBe controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Vendor Before You Start url and show 'Not yet started' status when all mandatory fields are missing from main vendor" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnAllMandatoryFieldsMissing)

          result.url mustBe controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Vendor Before You Start url and show 'In progress' status when some mandatory fields are missing from main vendor" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnSomeMandatoryFieldsMissing)

          result.url mustBe controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Vendor Overview url and show in 'Complete' status when all mandatory fields are present in main vendor" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnCompleteWithOneMainVendor)

          result.url mustBe controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must have Vendor Overview url when main vendor complete among other vendors" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnCompleteWithMultipleVendors)

          result.url mustBe controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must show 'Not yet started' status when vendor is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = VendorTaskList.buildVendorRow(fullReturnMissingVendor)

          result.status mustBe TLNotStarted
        }
      }
    }

    "integration" - {
      
      "must build complete TaskListSection with completed row when vendor present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = VendorTaskList.build(fullReturnCompleteWithOneMainVendor)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.vendorQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.vendorQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with not started row when vendor absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = VendorTaskList.build(fullReturnMissingVendor)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.vendorQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.vendorQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
