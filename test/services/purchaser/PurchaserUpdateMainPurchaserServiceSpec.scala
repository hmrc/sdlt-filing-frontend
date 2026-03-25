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

package services.purchaser

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.{completeReturnInfo, emptyFullReturn}
import models.purchaser.{DeleteCompanyDetailsReturn, UpdatePurchaserReturn}
import models.requests.DataRequest
import models.{CompanyDetails, Purchaser, ReturnInfo, ReturnInfoReturn, ReturnVersionUpdateReturn}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.ChangePurchaserOnePage
import play.api.http.Status.SEE_OTHER
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class PurchaserUpdateMainPurchaserServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockPurchaserService = mock[PurchaserService]
  private val mockSessionRepository = mock[SessionRepository]
  private val mockPopulatePurchaserService = mock[PopulatePurchaserService]

  private def service = new PurchaserUpdateMainPurchaserService(
    mockBackendConnector,
    mockPurchaserService,
    mockPopulatePurchaserService,
    mockSessionRepository
  )

  private def createPurchaser(
                               id: String,
                               forename1: Option[String] = None,
                               forename2: Option[String] = None,
                               surname: Option[String] = None,
                               companyName: Option[String] = None,
                               isCompany: Option[String] = Some("NO"),
                               nextPurchaserID: Option[String] = None,
                               purchaserRef: Option[String] = Some("1")
                             ): Purchaser = Purchaser(
    purchaserID = Some(id),
    forename1 = forename1,
    forename2 = forename2,
    surname = surname,
    companyName = companyName,
    isCompany = isCompany,
    nextPurchaserID = nextPurchaserID,
    isTrustee = Some("YES"),
    isConnectedToVendor = Some("NO"),
    isRepresentedByAgent = Some("NO"),
    address1 = Some("123 Test Street"),
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None,
    purchaserResourceRef = purchaserRef
  )

  "PurchaserUpdateMainPurchaserService" - {
    ".updateMainPurchaser" - {
      "must update new version, update Return Info and update old main purchaser details successfully when type is Individual" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, forename1 = Some("John"), surname = Some("Smith"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        val returnInfoWithNewMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(newMainPurchaserId))

        val fullReturnWithNewMainPurch = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithNewMainPurchaser))

        val userAnswersWithNewMainPurch = emptyUserAnswers
          .copy(fullReturn = Some(fullReturnWithNewMainPurch))

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(UpdatePurchaserReturn(true)))
        when(mockPurchaserService.isMainPurchaserCompany(eqTo(purchaser1)))
          .thenReturn(false)
        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), eqTo(userAnswers)))
          .thenReturn(Success(userAnswersWithNewMainPurch))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
      }

      "must update new version, update Return Info and update old main purchaser details and company details successfully when type is Company" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        val returnInfoWithNewMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(newMainPurchaserId))

        val fullReturnWithNewMainPurchNoCompanyDetails = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithNewMainPurchaser),
          companyDetails = None
        )

        val userAnswersWithNewMainPurchNoCompanyDetails = emptyUserAnswers
          .copy(fullReturn = Some(fullReturnWithNewMainPurchNoCompanyDetails))

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(UpdatePurchaserReturn(true)))
        when(mockPurchaserService.isMainPurchaserCompany(eqTo(purchaser1)))
          .thenReturn(true)
        when(mockBackendConnector.deleteCompanyDetails(any())(any(), any()))
          .thenReturn(Future.successful(DeleteCompanyDetailsReturn(true)))
        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), eqTo(userAnswers)))
          .thenReturn(Success(userAnswersWithNewMainPurchNoCompanyDetails))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
      }

      "must redirect to Change Purchaser One Page when the new main purchaser id is missing from session" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.ChangePurchaserOneController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when updating the version is unsuccessful" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.failed(new UpstreamErrorResponse("Upstream error", 400, 400, Map())))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }

      "must redirect to Journey Recovery when updating the ReturnInfo is unsuccessful" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.failed(new UpstreamErrorResponse("Upstream error", 400, 400, Map())))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when the previous main purchaser ID is missing from ReturnInfo" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = None)

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when the previous main purchaser is missing from the purchasers" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"
        val randomPurchaserId = "PUR099"

        val purchaser1 = createPurchaser(randomPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(None)

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when updating the old main purchaser details is unsuccessful" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.failed(new UpstreamErrorResponse("Upstream error", 400, 400, Map())))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when removing the old main purchaser company details is unsuccessful" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(UpdatePurchaserReturn(true)))
        when(mockPurchaserService.isMainPurchaserCompany(eqTo(purchaser1)))
          .thenReturn(true)
        when(mockBackendConnector.deleteCompanyDetails(any())(any(), any()))
          .thenReturn(Future.failed(new UpstreamErrorResponse("Upstream error", 400, 400, Map())))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when populating the session with the new main purchaser is unsuccessful" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(UpdatePurchaserReturn(true)))
        when(mockPurchaserService.isMainPurchaserCompany(eqTo(purchaser1)))
          .thenReturn(true)
        when(mockBackendConnector.deleteCompanyDetails(any())(any(), any()))
          .thenReturn(Future.successful(DeleteCompanyDetailsReturn(true)))
        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), eqTo(userAnswers)))
          .thenReturn(Success(new IllegalStateException(s"Purchaser PUR002 is missing required data")))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when session repository fails" in {
        val oldMainPurchaserId = "PUR001"
        val newMainPurchaserId = "PUR002"

        val purchaser1 = createPurchaser(oldMainPurchaserId, companyName = Some("Company"), isCompany = Some("YES"))
        val purchaser2 = createPurchaser(newMainPurchaserId, forename1 = Some("Anthos"), surname = Some("Smith"))

        val returnInfoWithOldMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(oldMainPurchaserId))

        val companyDetails = CompanyDetails(companyDetailsID = Some("id"), returnID = Some("returnid"), purchaserID = Some(oldMainPurchaserId))

        val fullReturn = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithOldMainPurchaser),
          companyDetails = Some(companyDetails)
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))
          .set(ChangePurchaserOnePage, newMainPurchaserId).success.value

        val returnInfoWithNewMainPurchaser: ReturnInfo = completeReturnInfo.copy(mainPurchaserID = Some(newMainPurchaserId))

        val fullReturnWithNewMainPurchNoCompanyDetails = emptyFullReturn.copy(
          purchaser = Some(Seq(purchaser1, purchaser2)),
          returnInfo = Some(returnInfoWithNewMainPurchaser),
          companyDetails = None
        )

        val userAnswersWithNewMainPurchNoCompanyDetails = emptyUserAnswers
          .copy(fullReturn = Some(fullReturnWithNewMainPurchNoCompanyDetails))

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
        when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
          .thenReturn(Future.successful(ReturnInfoReturn(true)))
        when(mockPurchaserService.findById(eqTo(Seq(purchaser1, purchaser2)), eqTo(oldMainPurchaserId)))
          .thenReturn(Some(purchaser1))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(UpdatePurchaserReturn(true)))
        when(mockPurchaserService.isMainPurchaserCompany(eqTo(purchaser1)))
          .thenReturn(true)
        when(mockBackendConnector.deleteCompanyDetails(any())(any(), any()))
          .thenReturn(Future.successful(DeleteCompanyDetailsReturn(true)))
        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), eqTo(userAnswers)))
          .thenReturn(Success(userAnswersWithNewMainPurchNoCompanyDetails))
        when(mockSessionRepository.set(any())).thenReturn(Future.failed(new RuntimeException("Database error")))

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "must redirect to Journey Recovery when return info is empty" in {
        val fullReturn = emptyFullReturn.copy(
          returnInfo = None
        )

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(fullReturn))

        implicit val request: DataRequest[AnyContent] = DataRequest(
          FakeRequest(),
          "id",
          userAnswers
        )

        val result = service.updateMainPurchaser(userAnswers)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}