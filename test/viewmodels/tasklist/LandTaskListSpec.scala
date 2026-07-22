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
  private val fullReturnAllMandatoryFieldsMissing = fullReturnComplete.copy(
    land = Some(Seq(completeLand.copy(
      propertyType = None,
      interestCreatedTransferred = None,
      address1 = None,
      localAuthorityNumber = None,
      willSendPlanByPost = None,
      mineralRights = None,
    ))))
  private val fullReturnSomeMandatoryFieldsMissing = fullReturnComplete.copy(
    land = Some(Seq(completeLand.copy(
      propertyType = None,
      interestCreatedTransferred = None,
      address1 = Some("123 Fake Street"),
      localAuthorityNumber = None,
      willSendPlanByPost = Some("YES"),
      mineralRights = None,
    ))))
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
  private val cf6OnlyStatus: SectionStatus =
    SectionStatus(
      section     = ReturnSection.Land,
      hasFailures = true,
      ruleIds     = Seq("Cf-6"),
      messageKeys = Seq("crossflow.land.Cf-6.body"),
      targets     = Nil
    )
  private val cf6PerLandStatus: SectionStatus =
    SectionStatus(
      section     = ReturnSection.Land,
      hasFailures = true,
      ruleIds     = Seq("Cf-6", "Cf-6"),
      messageKeys = Seq("crossflow.land.Cf-6.body"),
      targets     = Nil
    )
  private val cf6AndOtherStatus: SectionStatus =
    SectionStatus(
      section     = ReturnSection.Land,
      hasFailures = true,
      ruleIds     = Seq("Cf-6", "Cf-9a"),
      messageKeys = Seq("crossflow.land.Cf-6.body", "crossflow.land.Cf-9.welsh6996_6997.body"),
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

      "must route the row to the Cf-6 property type multi-entity controller when only Cf-6 failures present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnCompleteWithOneMainLand, cf6OnlyStatus)

          result.rows.head.url mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url
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

    ".mandatoryFieldsDefined" - {

      "must return a sequence of true if main land exists and mandatory fields are defined" in {
        val result = LandTaskList.mandatoryFieldsDefined(fullReturnCompleteWithOneMainLand)

        result mustBe Seq(true, true, true, true, true, true)
      }

      "must return a sequence of true and false if main land exists but some mandatory field are missing" in {
        val result = LandTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissing)

        result mustBe Seq(false, false, true, false, true, false)
      }

      "must return a sequence of false if main land exists but all mandatory fields are missing" in {
        val result = LandTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissing)

        result mustBe Seq(false, false, false, false, false, false)
      }
    }

    ".isLandComplete" - {

      "must return true if land exists and mandatory fields are defined" in {
          val result = LandTaskList.isLandComplete(fullReturnComplete)
          result mustBe true
      }

      "must return false if land exists but some mandatory field are missing" in {
          val result = LandTaskList.isLandComplete(fullReturnSomeMandatoryFieldsMissing)
          result mustBe false
      }

      "must return false if land exists but all mandatory fields are missing" in {
        val result = LandTaskList.isLandComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
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

      "must have Land Before You Start url and show 'Not yet started' status when main land is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnMissingLand, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Land Before You Start url and show 'Not yet started' status when all mandatory fields are missing from main land" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnAllMandatoryFieldsMissing, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Land Before You Start url and show 'In progress' status when some mandatory fields are missing from main land" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnSomeMandatoryFieldsMissing, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Land Overview url and show 'Complete' status when all mandatory fields are present in main land" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url

          result.status mustBe TLCompleted
        }
      }

      "must have Land Overview url when main land complete among other lands" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, noFailuresStatus)

          result.url mustBe controllers.land.routes.LandOverviewController.onPageLoad().url

          result.status mustBe TLCompleted
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

      "must route to the multi-entity controller for complete multiple lands" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must route to the before you start controller when main land is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnSomeMandatoryFieldsMissing, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must route to the before you start controller even when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnMissingLand, withFailuresStatus)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
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

    ".buildLandRow with Cf-6 routing" - {

      "must route to LandPropertyTypeMultiEntity when only Cf-6 failures present (single rule id)" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, cf6OnlyStatus)

          result.url mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url
        }
      }

      "must route to LandPropertyTypeMultiEntity when Cf-6 fires on multiple lands (all rule ids are Cf-6)" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, cf6PerLandStatus)

          result.url mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url
        }
      }

      "must route to LandAuthorityCodeMultiEntity when Cf-6 coexists with other rule ids (non-Cf-6 takes priority)" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands, cf6AndOtherStatus)

          result.url mustBe controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must mark the row status as TLInvalid when Cf-6 fires" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand, cf6OnlyStatus)

          result.status mustBe TLInvalid
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

      "must build complete TaskListSection with not started row when land absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(fullReturnMissingLand)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.status mustBe TLNotStarted
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

      "must build complete TaskListSection routing to Cf-6 multi-entity controller when only Cf-6 failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(fullReturnCompleteWithMultipleLands, cf6PerLandStatus)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.url mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url
          row.status mustBe TLInvalid
        }
      }
    }
  }
}