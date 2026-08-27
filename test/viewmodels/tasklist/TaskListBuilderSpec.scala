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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.running
import services.crossflow.ReturnSection
import services.crossflow.fields.CrossFlowValidationService

class TaskListBuilderSpec extends SpecBase with MockitoSugar {

  private val fullReturnComplete = completeFullReturn

  "TaskListBuilder" - {

    ".displaySections" - {

      "must return an empty sequence when fullReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = None)

          builder.displaySections(userAnswers) mustBe Seq.empty
        }
      }

      "must include vendor agent, purchaser agent, uk residency and lease sections when applicable" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            fullReturnComplete.copy(
              transaction = Some(completeTransaction.copy(transactionDescription = Some("L"))),
              land = Some(Seq(completeLand.copy(propertyType = Some("01"))))
            )
          ))

          val result = builder.displaySections(userAnswers)

          result.size mustBe 9
        }
      }

      "must exclude vendor agent, purchaser agent, uk residency and lease sections when not applicable" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            incompleteFullReturn.copy(
              transaction = Some(completeTransaction.copy(transactionDescription = Some("F"))),
              land = Some(Seq(completeLand.copy(propertyType = Some("02"))))
            )
          ))

          val result = builder.displaySections(userAnswers)

          result.size mustBe 5
        }
      }
    }

    ".completenessSections" - {

      "must return an empty sequence when fullReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = None)

          builder.completenessSections(userAnswers) mustBe Seq.empty
        }
      }

      "must include uk residency section when residency is required" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            fullReturnComplete.copy(land = Some(Seq(completeLand.copy(propertyType = Some("01")))))
          ))

          val result = builder.completenessSections(userAnswers)

          result.size mustBe 9
        }
      }

      "must exclude uk residency section when residency is not required" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            fullReturnComplete.copy(land = Some(Seq(completeLand.copy(propertyType = Some("02")))))
          ))

          val result = builder.completenessSections(userAnswers)

          result.size mustBe 8
        }
      }

      "must use the section status returned by the cross flow service" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val failingStatus = services.crossflow.SectionStatus(
            ReturnSection.Land, hasFailures = true, ruleIds = Seq("Cf-6"), messageKeys = Nil, targets = Nil
          )

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map(ReturnSection.Land -> failingStatus))

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            fullReturnComplete.copy(land = Some(Seq(completeLand.copy(propertyType = Some("02")))))
          ))

          val result = builder.completenessSections(userAnswers)

          result must not be empty
        }
      }
    }

    ".allComplete" - {

      "must return false when fullReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = None)

          builder.allComplete(userAnswers) mustBe false
        }
      }

      "must return true when all completeness sections are complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnComplete))

          builder.allComplete(userAnswers) mustBe true
        }
      }

      "must return false when a completeness section is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnComplete.copy(vendor = None)))

          builder.allComplete(userAnswers) mustBe false
        }
      }
    }

    ".sections" - {

      "must return an empty sequence when fullReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = None)

          builder.sections(userAnswers) mustBe Seq.empty
        }
      }

      "must append the submission section to the display sections when fullReturn is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val crossFlowService = mock[CrossFlowValidationService]
          when(crossFlowService.sectionStatuses(any())).thenReturn(Map.empty)

          val builder = new TaskListBuilder(crossFlowService)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            fullReturnComplete.copy(
              transaction = Some(completeTransaction.copy(transactionDescription = Some("L"))),
              land = Some(Seq(completeLand.copy(propertyType = Some("01"))))
            )
          ))

          val result = builder.sections(userAnswers)
          val displaySize = builder.displaySections(userAnswers).size

          result.size mustBe displaySize + 1
          result.last.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
        }
      }
    }
  }
}