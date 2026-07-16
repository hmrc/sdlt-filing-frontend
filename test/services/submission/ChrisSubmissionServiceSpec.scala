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

package services.submission

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import models.submission.{SubmissionResponse, SubmitRequest}
import models.{FullReturn, ReturnAgent, ReturnInfo, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.submission.SubmissionFailedPage
import play.api.mvc.Request
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ChrisSubmissionServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  "ChrisSubmissionService" - {

    ".submit" - {

      "must fail with a NoSuchElementException and never call the connector when there is no fullReturn" in new Setup {
        val userAnswers: UserAnswers = baseAnswers(fullReturn = None)

        service.submit(userAnswers).failed.futureValue mustBe a[NoSuchElementException]

        verify(connector, never()).submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]])
        verify(connector, never()).updateVendor(any)(any[HeaderCarrier], any[Request[_]])
        verify(connector, never()).updatePurchaser(any)(any[HeaderCarrier], any[Request[_]])
      }

      "when a fullReturn is present with no agents" - {

        "must call connector.submit exactly once and return its response" in new Setup {
          when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
            .thenReturn(Future.successful(submittedResponse))

          val userAnswers: UserAnswers = baseAnswers(fullReturn = Some(noAgentReturn))

          service.submit(userAnswers).futureValue mustBe submittedResponse

          val captor: ArgumentCaptor[SubmitRequest] = ArgumentCaptor.forClass(classOf[SubmitRequest])
          verify(connector).submit(captor.capture())(any[HeaderCarrier], any[Request[_]])
          captor.getValue.email      mustBe None
          captor.getValue.fullReturn mustBe noAgentReturn
        }

        "must never touch updateVendor or updatePurchaser" in new Setup {
          when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
            .thenReturn(Future.successful(submittedResponse))

          val userAnswers: UserAnswers = baseAnswers(fullReturn = Some(noAgentReturn))

          service.submit(userAnswers).futureValue

          verify(connector, never()).updateVendor(any)(any[HeaderCarrier], any[Request[_]])
          verify(connector, never()).updatePurchaser(any)(any[HeaderCarrier], any[Request[_]])
        }
      }

      "when a vendor agent is present but the main vendor cannot be resolved" - {

        "must still submit, and must not call updateVendor (no main vendor to flag)" in new Setup {
          when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
            .thenReturn(Future.successful(submittedResponse))

          val vendorAgentReturn: FullReturn = noAgentReturn.copy(
            returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
            returnInfo  = Some(ReturnInfo(mainVendorID = Some("V1"))),
            vendor      = None
          )

          val userAnswers: UserAnswers = baseAnswers(fullReturn = Some(vendorAgentReturn))

          service.submit(userAnswers).futureValue mustBe submittedResponse

          verify(connector).submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]])
          verify(connector, never()).updateVendor(any)(any[HeaderCarrier], any[Request[_]])
        }
      }
    }

    ".submitInBackground" - {

      "must NOT flag the submission as failed when the outcome is Submitted" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(submittedResponse))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.after(500).never()).set(any[UserAnswers])
      }

      "must NOT flag the submission as failed when the outcome is Acknowledged" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(SubmissionResponse.Acknowledged(returnId)))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.after(500).never()).set(any[UserAnswers])
      }

      "must flag the submission as failed when the outcome is Rejected" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(SubmissionResponse.Rejected(returnId, Nil)))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.timeout(1000)).set(flaggedAnswersMatcher)
      }

      "must flag the submission as failed when the outcome is Failed" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(SubmissionResponse.Failed(returnId, Nil)))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.timeout(1000)).set(flaggedAnswersMatcher)
      }

      "must flag the submission as failed when the outcome is Retryable" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(SubmissionResponse.Retryable(returnId)))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.timeout(1000)).set(flaggedAnswersMatcher)
      }

      "must flag the submission as failed when the underlying submit fails" in new Setup {
        when(connector.submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        service.submitInBackground(baseAnswers(fullReturn = Some(noAgentReturn)))

        verify(sessionRepository, org.mockito.Mockito.timeout(1000)).set(flaggedAnswersMatcher)
      }

      "must flag the submission as failed when there is no fullReturn (submit fails fast)" in new Setup {
        service.submitInBackground(baseAnswers(fullReturn = None))

        verify(sessionRepository, org.mockito.Mockito.timeout(1000)).set(flaggedAnswersMatcher)
        verify(connector, never()).submit(any[SubmitRequest])(any[HeaderCarrier], any[Request[_]])
      }
    }
  }

  private trait Setup {
    implicit val ec: ExecutionContext         = ExecutionContext.global
    implicit val hc: HeaderCarrier            = HeaderCarrier()
    implicit val request: Request[?]          = FakeRequest()

    val connector: StampDutyLandTaxConnector  = mock[StampDutyLandTaxConnector]
    val sessionRepository: SessionRepository  = mock[SessionRepository]

    val service = new ChrisSubmissionService(connector, sessionRepository)

    val returnId          = "100001"
    val submittedResponse = SubmissionResponse.Submitted(returnId, "UTRN123", receipt = true)

    val noAgentReturn: FullReturn =
      FullReturn(stornId = "TESTSTORN", returnResourceRef = "REF-1")

    when(sessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    def baseAnswers(fullReturn: Option[FullReturn]): UserAnswers =
      UserAnswers(userAnswersId, storn = "TESTSTORN", returnId = Some(returnId), fullReturn = fullReturn)

    def flaggedAnswersMatcher: UserAnswers =
      org.mockito.ArgumentMatchers.argThat[UserAnswers] { ua =>
        ua.get(SubmissionFailedPage).contains(true)
      }
  }
}