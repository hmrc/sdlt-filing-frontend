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

package controllers.submission

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import models.{FullReturn, GetReturnByRefRequest, Submission}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.submission.SubmissionFailedPage
import play.api.inject.bind
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.submission.LoadingScreenView

import scala.concurrent.Future

class LoadingScreenControllerSpec extends SpecBase with MockitoSugar {

  private def fullReturnWithStatus(status: Option[String]): FullReturn =
    FullReturn(
      stornId = "TESTSTORN",
      returnResourceRef = "ref",
      submission = Some(Submission(submissionStatus = status))
    )

  private def connectorReturning(status: Option[String]): StampDutyLandTaxConnector = {
    val connector = mock[StampDutyLandTaxConnector]
    when(connector.getFullReturn(any[GetReturnByRefRequest])(any[HeaderCarrier], any[Request[_]]))
      .thenReturn(Future.successful(fullReturnWithStatus(status)))
    connector
  }

  private def failingConnector: StampDutyLandTaxConnector = {
    val connector = mock[StampDutyLandTaxConnector]
    when(connector.getFullReturn(any[GetReturnByRefRequest])(any[HeaderCarrier], any[Request[_]]))
      .thenReturn(Future.failed(new RuntimeException("boom")))
    connector
  }

  val testFullReturn = completeFullReturn.copy(submission = Some(Submission(None)))
  val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

  "LoadingScreen Controller" - {

    "show" - {

      "must return OK and the correct view for a GET when there is no returnId" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[LoadingScreenView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(controllers.submission.routes.LoadingScreenController.query)(request, messages(application)).toString
        }
      }

      "must return OK and the loading screen view when the status is still in progress" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers.copy(returnId = Some("ret-123"))))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(connectorReturning(Some("PENDING"))))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      val redirectScenarios: Seq[(String, String, () => Call)] = Seq(
        ("SUBMITTED",            "the submission complete page",       () => controllers.submission.routes.SubmissionCompleteController.onPageLoad()),
        ("SUBMITTED_NO_RECEIPT", "the submission complete page",       () => controllers.submission.routes.SubmissionCompleteController.onPageLoad()),
        ("ACCEPTED",             "the awaiting confirmation page",     () => controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad()),
        ("STARTED",              "the before you start page",          () => controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad()),
        ("DEPARTMENTAL_ERROR",   "the submission failed page",         () => controllers.submission.routes.SubmissionFailedController.onPageLoad()),
        ("FATAL_ERROR",          "the submission failed page",         () => controllers.submission.routes.SubmissionFailedController.onPageLoad())
      )

      redirectScenarios.foreach { case (statusValue, description, expectedCall) =>
        s"must redirect to $description when the submission status is $statusValue" in {

          val application = applicationBuilder(userAnswers = Some(testUserAnswers.copy(returnId = Some("ret-123"))))
            .overrides(bind[StampDutyLandTaxConnector].toInstance(connectorReturning(Some(statusValue))))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual expectedCall().url
          }
        }
      }

      redirectScenarios.foreach { case (statusValue, description, expectedCall) =>
        s"must redirect to $description when the submission status is $statusValue in lower case" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("ret-123"))))
            .overrides(bind[StampDutyLandTaxConnector].toInstance(connectorReturning(Some(statusValue.toLowerCase))))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual expectedCall().url
          }
        }
      }

      "must return OK and the loading screen view when the connector fails" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers.copy(returnId = Some("ret-123"))))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(failingConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to the Submission Failed page when SubmissionFailedPage is set" in {

        val answers     = testUserAnswers.set(SubmissionFailedPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.show.url)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.submission.routes.SubmissionFailedController.onPageLoad().url
        }
      }
    }

    "query" - {

      "must return NO_CONTENT for a GET when there is no returnId" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.query.url)

          val result = route(application, request).value

          status(result) mustEqual NO_CONTENT
        }
      }

      "must return NO_CONTENT while the submission is still in progress" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("ret-123"))))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(connectorReturning(Some("PENDING"))))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.query.url)

          val result = route(application, request).value

          status(result) mustEqual NO_CONTENT
        }
      }

      "must return OK once the submission reaches a terminal status" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("ret-123"))))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(connectorReturning(Some("SUBMITTED"))))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.query.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return NO_CONTENT when the connector fails" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("ret-123"))))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(failingConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.query.url)

          val result = route(application, request).value

          status(result) mustEqual NO_CONTENT
        }
      }

      "must return OK when SubmissionFailedPage is set" in {

        val answers     = emptyUserAnswers.set(SubmissionFailedPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.submission.routes.LoadingScreenController.query.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }
  }
}