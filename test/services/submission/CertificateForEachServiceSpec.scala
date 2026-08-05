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
import models.requests.DataRequest
import models.{FullReturn, Land, ReturnInfo, ReturnInfoRequest, ReturnInfoReturn, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CertificateForEachServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  private val fullReturn = FullReturn(
    stornId = "TESTSTORN",
    returnResourceRef = "REF001",
    land = Some(Seq(Land(), Land())),
    returnInfo = Some(ReturnInfo(returnID = Some("R1")))
  )

  private def answers(returnInfo: Option[ReturnInfo]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(fullReturn.copy(returnInfo = returnInfo)))

  private def newService(connector: StampDutyLandTaxConnector): CertificateForEachService =
    new CertificateForEachService(connector)

  private def sentRequest(userAnswers: UserAnswers, answer: Boolean): ReturnInfoRequest = {
    val connector = mock[StampDutyLandTaxConnector]
    when(connector.updateReturnInfo(any())(any(), any())) thenReturn Future.successful(ReturnInfoReturn(true))

    implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", userAnswers)
    newService(connector).store(userAnswers, answer).futureValue

    val captor = ArgumentCaptor.forClass(classOf[ReturnInfoRequest])
    verify(connector).updateReturnInfo(captor.capture())(any(), any())
    captor.getValue
  }

  "CertificateForEachService store" - {

    "must send yes to the backend when the user answers yes" in {
      sentRequest(answers(Some(ReturnInfo(returnID = Some("R1")))), answer = true)
        .landCertForEachProp mustBe Some("yes")
    }

    "must send no when they answer no" in {
      sentRequest(answers(
        Some(ReturnInfo(returnID = Some("R1")))),
        answer = false
      )
        .landCertForEachProp mustBe Some("no")
    }

    "must carry over the storn and resource ref" in {
      val req = sentRequest(answers(
        Some(ReturnInfo(returnID = Some("R1")))),
        answer = true
      )

      req.storn mustBe "TESTSTORN"
      req.returnResourceRef mustBe "REF001"
    }

    "must give the caller back answers with the value already on them" in {
      val connector = mock[StampDutyLandTaxConnector]
      when(connector.updateReturnInfo(any())(any(), any())) thenReturn Future.successful(ReturnInfoReturn(true))

      val userAnswers = answers(Some(ReturnInfo(returnID = Some("R1"))))
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", userAnswers)

      newService(connector).store(userAnswers, certificateForEach = true).futureValue
        .fullReturn
        .flatMap(_.returnInfo)
        .flatMap(_.landCertForEachProp) mustBe Some("yes")
    }

    "must still hand back the answers when the backend reports no update" in {
      val connector = mock[StampDutyLandTaxConnector]
      when(connector.updateReturnInfo(any())(any(), any())) thenReturn Future.successful(ReturnInfoReturn(false))

      val userAnswers = answers(Some(ReturnInfo(returnID = Some("R1"))))
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", userAnswers)

      newService(connector).store(userAnswers, certificateForEach = true).futureValue
        .fullReturn
        .flatMap(_.returnInfo)
        .flatMap(_.landCertForEachProp) mustBe Some("yes")
    }

    "must not call the backend if there is no returnInfo" in {
      val connector = mock[StampDutyLandTaxConnector]

      val userAnswers = answers(None)
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", userAnswers)

      newService(connector).store(userAnswers, certificateForEach = true).futureValue mustBe userAnswers
      verify(connector, never()).updateReturnInfo(any())(any(), any())
    }
  }
}
