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
import models.{Purchaser, Residency}
import org.scalatest.prop.TableDrivenPropertyChecks.*
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.tasklist.UkResidencyTaskList.mandatoryFieldsDefined

class UkResidencyTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnSomeMandatoryFieldsPresent = fullReturnComplete.copy(
    residency = Some(completeResidency.copy(
      isNonUkResidents = Some("YES"),
      isCloseCompany = Some("YES"),
      isCrownRelief = Some("YES")
    )),
    purchaser = Some(Seq(completePurchaser1.copy(
      isCompany = Some("YES")
    ))))
  private val fullReturnSomeMandatoryFieldsMissing = fullReturnComplete.copy(
    residency = Some(completeResidency.copy(
      isNonUkResidents = Some("YES"),
      isCloseCompany = Some("YES"),
      isCrownRelief = None
    )),
    purchaser = Some(Seq(completePurchaser1.copy(
      isCompany = Some("YES")
    )))
  )
  private val fullReturnAllMandatoryFieldsMissing = fullReturnComplete.copy(
    residency = Some(completeResidency.copy(
      isNonUkResidents = None,
      isCloseCompany = None,
      isCrownRelief = None
    )),
    purchaser = Some(Seq(completePurchaser1.copy(
      isCompany = Some("YES")
    ))))

  private val fullReturnMissingResidency = fullReturnComplete.copy(residency = None)

  "UkResidencyTaskList" - {

    ".build" - {

      "must return TaskListSection with correct heading when uk residency is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when uk residency is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
        }
      }
    }

    ".mandatoryFieldsDefined" - {

      "return correct sequence when isCompany is YES and isNonUkResidents is YES" in {
        val cases = Table(
          ("isCloseCompanyDefined", "isCrownReliefDefined", "expected"),
          (true, true, Seq(true, true)),
          (true, false, Seq(true, false)),
          (false, true, Seq(false, true)),
          (false, false, Seq(false, false))
        )

        forAll(cases) { (isCloseCompanyDefined: Boolean, isCrownReliefDefined: Boolean, expected: Seq[Boolean]) =>
          val fullReturn = completeFullReturn.copy(
            purchaser = Some(Seq(Purchaser(isCompany = Some("YES")))),
            residency = Some(
              Residency(
                isNonUkResidents = Some("YES"),
                isCloseCompany = if (isCloseCompanyDefined) Some("YES") else None,
                isCrownRelief = if (isCrownReliefDefined) Some("YES") else None
              )
            )
          )

          mandatoryFieldsDefined(fullReturn) mustBe expected
        }
      }

      "return correct sequence when isCompany is YES and isNonUkResidents is NO" in {
        val cases = Table(
          ("isCloseCompanyDefined", "expected"),
          (true, Seq(true)),
          (false, Seq(false))
        )

        forAll(cases) { (isCloseCompanyDefined: Boolean, expected: Seq[Boolean]) =>
          val fullReturn = completeFullReturn.copy(
            purchaser = Some(Seq(Purchaser(isCompany = Some("YES")))),
            residency = Some(
              Residency(
                isNonUkResidents = Some("NO"),
                isCloseCompany = if (isCloseCompanyDefined) Some("YES") else None,
                isCrownRelief = None // irrelevant
              )
            )
          )

          mandatoryFieldsDefined(fullReturn) mustBe expected
        }
      }

      "return correct sequence when isCompany is NO and isNonUkResidents is YES" in {
        val cases = Table(
          ("isCrownReliefDefined", "expected"),
          (true, Seq(true)),
          (false, Seq(false))
        )

        forAll(cases) { (isCrownReliefDefined: Boolean, expected: Seq[Boolean]) =>
          val fullReturn = completeFullReturn.copy(
            purchaser = Some(Seq(Purchaser(isCompany = Some("NO")))),
            residency = Some(
              Residency(
                isNonUkResidents = Some("YES"),
                isCloseCompany = None, // irrelevant
                isCrownRelief = if (isCrownReliefDefined) Some("YES") else None
              )
            )
          )

          mandatoryFieldsDefined(fullReturn) mustBe expected
        }
      }

      "return correct sequence when isCompany is NO and isNonUkResidents is NO" in {
        val cases = Table(
          ("isNonUkResidentsDefined", "expected"),
          (true, Seq(true)),
          (false, Seq(false))
        )

        forAll(cases) { (isNonUkResidentsDefined: Boolean, expected: Seq[Boolean]) =>
          val residency =
            if (isNonUkResidentsDefined)
              Some(Residency(isNonUkResidents = Some("NO"), isCloseCompany = None, isCrownRelief = None))
            else
              Some(Residency(isNonUkResidents = None, isCloseCompany = None, isCrownRelief = None))

          val fullReturn = completeFullReturn.copy(
            purchaser = Some(Seq(Purchaser(isCompany = Some("NO")))),
            residency = residency
          )

          mandatoryFieldsDefined(fullReturn) mustBe expected
        }
      }
    }

    ".isResidencyComplete" - {

      "must return true if residency exists and mandatory fields defined" in {
        val result = UkResidencyTaskList.isResidencyComplete(fullReturnSomeMandatoryFieldsPresent)

        result mustBe true
      }

      "must return false if residency exists and some mandatory fields not defined" in {
        val result = UkResidencyTaskList.isResidencyComplete(fullReturnSomeMandatoryFieldsMissing)

        result mustBe false
      }

      "must return false if residency exists and all mandatory fields not defined" in {
        val result = UkResidencyTaskList.isResidencyComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
      }

      "must return false if residency does not exist" in {
        val result = UkResidencyTaskList.isResidencyComplete(fullReturnMissingResidency)

        result mustBe false
      }
    }

    ".buildUkResidencyRow" - {

      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "ukResidencyQuestionRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
        }
      }

      "must have Uk Residency Before You Start url when residency is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnMissingResidency)

          result.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Uk Residency Before You Start url and show 'Not yet started' status when all mandatory fields are missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnAllMandatoryFieldsMissing)

          result.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Uk Residency Before You Start url and show 'In progress' status when some mandatory fields are missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnSomeMandatoryFieldsMissing)

          result.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have  Uk Residency Check your answers url and show 'Complete' status when all mandatory fields are present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result.url mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url

          result.status mustBe TLCompleted
        }
      }

      "must show 'Complete' status when Uk Residency is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Not yet started' status when Uk Residency is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnMissingResidency)

          result.status mustBe TLNotStarted
        }
      }
    }

    "integration" - {

      "must build complete TaskListSection with completed row when Uk Residency present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = UkResidencyTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with not started row when Uk Residency absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = UkResidencyTaskList.build(fullReturnMissingResidency)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
