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
import services.crossflow.{ReturnSection, SectionStatus}

class LandTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnCompleteWithOneMainLand = fullReturnComplete.copy(
    land = Some(Seq(completeLand)))
  private val fullReturnCompleteWithMultipleLands = completeFullReturn.copy(
    land = Some(Seq(completeLand, completeLand.copy(landID = Some("LAND-ID-2")), completeLand.copy(landID = Some("LAND-ID-3")))))
  private val fullReturnIncompleteLand = fullReturnComplete.copy(
    land = Some(Seq(completeLand.copy(mineralRights = None))))
  private val fullReturnMissingLand = fullReturnComplete.copy(land = None)

  private val noFailuresStatus: SectionStatus =
    SectionStatus(ReturnSection.Land, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  private val withFailuresStatus: SectionStatus =
    SectionStatus(
      section     = ReturnSection.Land,
      hasFailures = true,
      ruleIds     = Seq("F17-6996-6997"),
      messageKeys = Seq("crossflow.land.authority.welsh6996_6997.beforeEffectiveDate"),
      targets     = Nil
    )

  "LandTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when land is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.landQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.landQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }

      "must apply the default no-failures status when called without status (backwards-compat)" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnCompleteWithOneMainLand)

          result.rows.size mustBe 1
          result.rows.head.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must route the row to the multi-entity controller when status has failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnCompleteWithOneMainLand, withFailuresStatus)

          result.rows.head.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
          result.rows.head.status mustBe TLInvalid
        }
      }

      "must keep heading consistent when status has failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnComplete, withFailuresStatus)

          result.heading mustBe messagesInstance("tasklist.landQuestion.heading")
        }
      }
    }

    ".buildLandRow" - {
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete, noFailuresStatus)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete, noFailuresStatus)

          result.tagId mustBe "landQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete, noFailuresStatus)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
        }
      }

      "must have Land Before You Start url when main land is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnMissingLand, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Land Before You Start url when main land is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnIncompleteLand, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Land Overview url when main land is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must have Land Overview url when main land complete among other lands" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must show completed status when a main land is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete, noFailuresStatus)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(emptyFullReturn, noFailuresStatus)

          result.status mustBe TLCannotStart
        }
      }
    }

    ".buildLandRow with cross-flow failures" - {

      "must route to the multi-entity controller, not the overview" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must route to the multi-entity controller for multiple lands" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must route to the multi-entity controller even when main land is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnIncompleteLand, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must route to the multi-entity controller even when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnMissingLand, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must mark the row status as TLInvalid" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, withFailuresStatus)

          result.status mustBe TLInvalid
        }
      }

      "must not mark the row status as TLInvalid when status has no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, noFailuresStatus)

          result.status must not be TLInvalid
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when land present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(fullReturnCompleteWithOneMainLand)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when land absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must build complete TaskListSection routing to multi-entity controller when failures present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(fullReturnCompleteWithOneMainLand, withFailuresStatus)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
          row.status mustBe TLInvalid
        }
      }
    }
  }
}