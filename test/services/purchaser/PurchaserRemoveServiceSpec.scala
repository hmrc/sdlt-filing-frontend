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
import forms.purchaser.PurchaserRemoveFormProvider
import models.*
import models.purchaser.*
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{PurchaserOverviewRemovePage, PurchaserRemovePage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import views.html.purchaser.PurchaserRemoveView

import scala.concurrent.{ExecutionContext, Future}

class PurchaserRemoveServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockView = mock[PurchaserRemoveView]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockPurchaserService = mock[PurchaserService]

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def service = new PurchaserRemoveService(mockView, mockBackendConnector, mockPurchaserService)

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None,
    returnInfo = None
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

  private val formProvider = new PurchaserRemoveFormProvider()
  private def form: Form[PurchaserRemove] = formProvider()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView, mockBackendConnector)
  }

  "PurchaserRemoveService" - {

    "isMainPurchaser" - {

      "must return true when purchaserId matches mainPurchaserID" in {
        val purchaserId = "PURCH001"
        val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
        val fullReturn = emptyFullReturn.copy(returnInfo = Some(returnInfo))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.isMainPurchaser(purchaserId, userAnswers)

        result mustBe true
      }

      "must return false when purchaserId does not match mainPurchaserID" in {
        val returnInfo = ReturnInfo(mainPurchaserID = Some("PURCH001"))
        val fullReturn = emptyFullReturn.copy(returnInfo = Some(returnInfo))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.isMainPurchaser("PURCH002", userAnswers)

        result mustBe false
      }

      "must return false when mainPurchaserID is None" in {
        val returnInfo = ReturnInfo(mainPurchaserID = None)
        val fullReturn = emptyFullReturn.copy(returnInfo = Some(returnInfo))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.isMainPurchaser("PURCH001", userAnswers)

        result mustBe false
      }

      "must return false when returnInfo is None" in {
        val fullReturn = emptyFullReturn.copy(returnInfo = None)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.isMainPurchaser("PURCH001", userAnswers)

        result mustBe false
      }

      "must return false when fullReturn is None" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = None)

        val result = service.isMainPurchaser("PURCH001", userAnswers)

        result mustBe false
      }
    }

    "allPurchasersSeq" - {

      "must return all purchasers when they exist" in {
        val purchaser1 = createPurchaser("PURCH001", forename1 = Some("John"), surname = Some("Doe"))
        val purchaser2 = createPurchaser("PURCH002", forename1 = Some("Jane"), surname = Some("Smith"))
        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser1, purchaser2)))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.allPurchasersSeq(userAnswers)

        result mustBe Seq(purchaser1, purchaser2)
      }

      "must return empty sequence when no purchasers exist" in {
        val fullReturn = emptyFullReturn.copy(purchaser = None)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.allPurchasersSeq(userAnswers)

        result mustBe Seq.empty
      }

      "must return empty sequence when purchaser list is empty" in {
        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq.empty))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.allPurchasersSeq(userAnswers)

        result mustBe Seq.empty
      }

      "must return empty sequence when fullReturn is None" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = None)

        val result = service.allPurchasersSeq(userAnswers)

        result mustBe Seq.empty
      }
    }

    "purchaserRemoveView" - {

      implicit val messages: Messages = stubMessages()

      "when PurchaserOverviewRemovePage exists" - {

        "must return view with single main purchaser data" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must return view with company purchaser data" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(
            purchaserId,
            companyName = Some("ACME Corporation"),
            isCompany = Some("YES")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, Some("COMP001"))
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must return view with double purchaser data" in {
          val purchaserId = "PURCH001"
          val remainingId = "PURCH002"
          val purchaser1 = createPurchaser(
            purchaserId,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(remainingId)
          )
          val purchaser2 = createPurchaser(
            remainingId,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must return view with multiple purchaser data" in {
          val purchaserId = "PURCH001"
          val purchaser1 = createPurchaser(
            purchaserId,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some("PURCH002")
          )
          val purchaser2 = createPurchaser(
            "PURCH002",
            companyName = Some("ABC Company Ltd"),
            isCompany = Some("YES"),
            nextPurchaserID = Some("PURCH003")
          )
          val purchaser3 = createPurchaser(
            "PURCH003",
            companyName = Some("XYZ Corporation"),
            isCompany = Some("YES")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2, purchaser3)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must return view with non-main purchaser data" in {
          val mainPurchaserID = "PURCH001"
          val nonmainPurchaserID = "PURCH002"
          val purchaser1 = createPurchaser(
            mainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(nonmainPurchaserID)
          )
          val purchaser2 = createPurchaser(
            nonmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(mainPurchaserID))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(nonmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must prepopulate form with existing PurchaserRemovePage answer" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value
            .set(PurchaserRemovePage, PurchaserRemove.No).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must handle purchaser with middle name" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(
            purchaserId,
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must filter out remaining purchasers without names" in {
          val purchaserId = "PURCH001"
          val purchaser1 = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val purchaser2 = createPurchaser("PURCH002")
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must filter out remaining purchasers without purchaserID" in {
          val purchaserId = "PURCH001"
          val purchaser1 = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val purchaser2 = createPurchaser("PURCH002", forename1 = Some("Jane"), surname = Some("Doe"))
            .copy(purchaserID = None)
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }

        "must handle case where purchaser to remove is not found" in {
          val purchaserId = "PURCH001"
          val otherPurchaser = createPurchaser("PURCH999", forename1 = Some("Other"), surname = Some("Person"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(otherPurchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockView.apply(any(), any(), any())(any(), any()))
            .thenReturn(Html("test view"))

          val result = service.purchaserRemoveView(form, NormalMode)

          result mustBe a[Right[_, _]]
          verify(mockView).apply(any(), any(), any())(any(), any())
        }
      }
    }

    "handleRemoval" - {

      "when PurchaserOverviewRemovePage exists" - {

        "must handle No" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          val result = service.handleRemoval(PurchaserRemove.No, userAnswers)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url)
        }

        "must handle Remove for single main purchaser" in {
          val purchaserId = "PURCH001"

          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(version = Some("0"), mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(purchaserId),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector).deletePurchaser(any())(any(), any())
        }

        "must handle Remove for non-main purchaser with multiple purchasers" in {
          val mainPurchaserID = "PURCH001"
          val nonmainPurchaserID = "PURCH002"
          val thirdPurchaserId = "PURCH003"

          val purchaser1 = createPurchaser(
            mainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(nonmainPurchaserID),
            purchaserRef = Some("1")
          )
          val purchaser2 = createPurchaser(
            nonmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe"),
            nextPurchaserID = Some(thirdPurchaserId),
            purchaserRef = Some("2")
          )
          val purchaser3 = createPurchaser(
            thirdPurchaserId,
            forename1 = Some("Bob"),
            surname = Some("Wilson"),
            purchaserRef = Some("3")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(mainPurchaserID), version = Some("0"))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2, purchaser3)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(nonmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((DeletePurchaserReturn(deleted = true))))
          when(mockBackendConnector.updatePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((UpdatePurchaserReturn(updated = true))))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(nonmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector).deletePurchaser(any())(any(), any())
          verify(mockBackendConnector).updatePurchaser(any())(any(), any())
        }

        "must handle Remove for main purchaser with exactly two purchasers" in {
          val mainPurchaserID = "PURCH001"
          val remainingPurchaserId = "PURCH002"

          val purchaser1 = createPurchaser(
            mainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(remainingPurchaserId)
          )
          val purchaser2 = createPurchaser(
            remainingPurchaserId,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(mainPurchaserID), version = Some("0"))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(mainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((DeletePurchaserReturn(deleted = true))))
          when(mockBackendConnector.updatePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((UpdatePurchaserReturn(updated = true))))
          when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
            .thenReturn(Future.successful((ReturnInfoReturn(updated = true))))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(mainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector, times(2)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector).deletePurchaser(any())(any(), any())
          verify(mockBackendConnector).updatePurchaser(any())(any(), any())
          verify(mockBackendConnector).updateReturnInfo(any())(any(), any())
        }

        "must handle Remove with company details present" in {
          val purchaserId = "PURCH001"
          val companyDetailsId = "COMP001"
          val purchaser = createPurchaser(
            purchaserId,
            companyName = Some("ACME Corp"),
            isCompany = Some("YES")
          )
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId), version = Some("0"))
          val fullReturn = emptyFullReturn.copy(
            companyDetails = Some(CompanyDetails(companyDetailsID = Some(companyDetailsId))),
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, Some(companyDetailsId))
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((DeletePurchaserReturn(deleted = true))))
          when(mockBackendConnector.deleteCompanyDetails(any())(any(), any()))
            .thenReturn(Future.successful((DeleteCompanyDetailsReturn(deleted = true))))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(purchaserId),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector).deleteCompanyDetails(any())(any(), any())
        }

        "must handle SelectNewMain with new main being next in chain" in {
          val oldmainPurchaserID = "PURCH001"
          val newmainPurchaserID = "PURCH002"
          val thirdPurchaserId = "PURCH003"

          val purchaser1 = createPurchaser(
            oldmainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(newmainPurchaserID)
          )
          val purchaser2 = createPurchaser(
            newmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe"),
            nextPurchaserID = Some(thirdPurchaserId)
          )
          val purchaser3 = createPurchaser(
            thirdPurchaserId,
            forename1 = Some("Bob"),
            surname = Some("Wilson")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(oldmainPurchaserID), version = Some("0"))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2, purchaser3)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((DeletePurchaserReturn(deleted = true))))
          when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
            .thenReturn(Future.successful((ReturnInfoReturn(updated = true))))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector, times(2)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector).deletePurchaser(any())(any(), any())
          verify(mockBackendConnector).updateReturnInfo(any())(any(), any())
        }

        "must handle SelectNewMain with new main not being next in chain" in {
          val oldmainPurchaserID = "PURCH001"
          val secondPurchaserId = "PURCH002"
          val newmainPurchaserID = "PURCH003"

          val purchaser1 = createPurchaser(
            oldmainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(secondPurchaserId)
          )
          val purchaser2 = createPurchaser(
            secondPurchaserId,
            forename1 = Some("Middle"),
            surname = Some("Person"),
            nextPurchaserID = Some(newmainPurchaserID)
          )
          val purchaser3 = createPurchaser(
            newmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(oldmainPurchaserID), version = Some("0"))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2, purchaser3)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((DeletePurchaserReturn(deleted = true))))
          when(mockBackendConnector.updatePurchaser(any())(any(), any()))
            .thenReturn(Future.successful((UpdatePurchaserReturn(updated = true))))
          when(mockBackendConnector.updateReturnInfo(any())(any(), any()))
            .thenReturn(Future.successful((ReturnInfoReturn(updated = true))))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          verify(mockBackendConnector, times(2)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector).deletePurchaser(any())(any(), any())
          verify(mockBackendConnector, times(2)).updatePurchaser(any())(any(), any())
          verify(mockBackendConnector).updateReturnInfo(any())(any(), any())
        }

        "must redirect to JourneyRecovery when updateReturnVersion fails" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Update failed")))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(purchaserId),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when updateReturnVersion returns None for newVersion" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(None)))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(purchaserId),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when deletePurchaser fails" in {
          val purchaserId = "PURCH001"
          val purchaser = createPurchaser(purchaserId, forename1 = Some("John"), surname = Some("Smith"))
          val returnInfo = ReturnInfo(mainPurchaserID = Some(purchaserId))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(purchaserId, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Delete failed")))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(purchaserId),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when SelectNewMain cannot find new main purchaser" in {
          val oldmainPurchaserID = "PURCH001"
          val newmainPurchaserID = "PURCH999"

          val purchaser1 = createPurchaser(
            oldmainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(oldmainPurchaserID))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when SelectNewMain missing returnInfo" in {
          val oldmainPurchaserID = "PURCH001"
          val newmainPurchaserID = "PURCH002"

          val purchaser1 = createPurchaser(
            oldmainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(newmainPurchaserID)
          )
          val purchaser2 = createPurchaser(
            newmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = None
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when SelectNewMain has missing original main purchaser" in {
          val oldmainPurchaserID = "PURCH001"
          val newmainPurchaserID = "PURCH002"

          val purchaser2 = createPurchaser(
            newmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(oldmainPurchaserID))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must redirect to JourneyRecovery when SelectNewMain has missing previous purchaser in list" in {
          val oldmainPurchaserID = "PURCH001"
          val newmainPurchaserID = "PURCH002"

          val purchaser1 = createPurchaser(
            oldmainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(newmainPurchaserID)
          )
          val purchaser2 = createPurchaser(
            newmainPurchaserID,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val returnInfo = ReturnInfo(mainPurchaserID = Some(oldmainPurchaserID))
          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = Some(returnInfo)
          )
          val purchaserRefs = PurchaserAndCompanyId(oldmainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.SelectNewMain(newmainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }

        "must handle Remove for double purchaser scenario when missing returnInfo" in {
          val mainPurchaserID = "PURCH001"
          val remainingPurchaserId = "PURCH002"

          val purchaser1 = createPurchaser(
            mainPurchaserID,
            forename1 = Some("John"),
            surname = Some("Smith"),
            nextPurchaserID = Some(remainingPurchaserId)
          )
          val purchaser2 = createPurchaser(
            remainingPurchaserId,
            forename1 = Some("Jane"),
            surname = Some("Doe")
          )

          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(purchaser1, purchaser2)),
            returnInfo = None
          )
          val purchaserRefs = PurchaserAndCompanyId(mainPurchaserID, None)
          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(PurchaserOverviewRemovePage, purchaserRefs).success.value

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(2))))
          when(mockBackendConnector.deletePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.handleRemoval(
            PurchaserRemove.Remove(mainPurchaserID),
            userAnswers
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "when PurchaserOverviewRemovePage does not exist" - {

        "must redirect to purchaser overview" in {
          val userAnswers = emptyUserAnswers

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            "id",
            userAnswers
          )

          val result = service.handleRemoval(PurchaserRemove.No, userAnswers)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url)
        }
      }
    }
  }
}