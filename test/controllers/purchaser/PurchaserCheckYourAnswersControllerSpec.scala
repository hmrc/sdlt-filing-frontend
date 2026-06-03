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

package controllers.purchaser

import base.SpecBase
import models.address.Address
import models.prelimQuestions.CompanyOrIndividualRequest
import models.purchaser.{NameOfPurchaser, PurchaserAndCompanyId, PurchaserConfirmIdentity, PurchaserTypeOfCompanyAnswers, WhoIsMakingThePurchase}
import models.{CheckMode, FullReturn, Purchaser, ReturnInfo, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.preliminary.PurchaserIsIndividualPage
import pages.purchaser.*
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserCreateOrUpdateService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def getFullReturn(mainPurchaserID: Option[String] = None): FullReturn = {
    FullReturn(
      stornId = "STORN123456",
      returnResourceRef = "RRF-2024-001",
      returnInfo = Some(ReturnInfo(
        returnID = Some("RET123456789"),
        storn = Some("STORN123456"),
        mainPurchaserID = mainPurchaserID
      ))
    )
  }

  val typeOfCompany = PurchaserTypeOfCompanyAnswers(
    bank = "yes",
    buildingSociety = "no",
    centralGovernment = "no",
    individualOther = "no",
    insuranceAssurance = "no",
    localAuthority = "no",
    partnership = "no",
    propertyCompany = "no",
    publicCorporation = "no",
    otherCompany = "no",
    otherFinancialInstitute = "no",
    otherIncludingCharity = "no",
    superannuationOrPensionFund = "no",
    unincorporatedBuilder = "no",
    unincorporatedSoleTrader = "no"
  )

  val minimalIndividualUserAnswers: UserAnswers = emptyUserAnswers.copy(returnId = Some("RE12345"))
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Doe")).success.value
    .set(PurchaserAddressPage, Address(
      line1 = "Test Street",
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postcode = None,
      country = None
    )).success.value
    .set(IsPurchaserActingAsTrusteePage, true).success.value
    .set(PurchaserAndVendorConnectedPage, true).success.value

  val fullIndividualUserAnswers: UserAnswers = minimalIndividualUserAnswers
    .set(DoesPurchaserHaveNIPage, true).success.value
    .set(PurchaserNationalInsurancePage, "AA0000000A").success.value
    .set(PurchaserDateOfBirthPage, LocalDate.of(1985, 3, 15)).success.value
    .set(AddPurchaserPhoneNumberPage, true).success.value
    .set(EnterPurchaserPhoneNumberPage, "07477777777").success.value

  val minimalCompanyUserAnswers: UserAnswers = emptyUserAnswers.copy(returnId = Some("RE12345"))
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "Company ltd")).success.value
    .set(PurchaserAddressPage, Address(
      line1 = "Test Street",
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postcode = None,
      country = None
    )).success.value
    .set(IsPurchaserActingAsTrusteePage, true).success.value
    .set(PurchaserAndVendorConnectedPage, true).success.value

  val fullCompanyUserAnswers: UserAnswers = minimalCompanyUserAnswers
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.VatRegistrationNumber).success.value
    .set(RegistrationNumberPage, "1234567").success.value
    .set(PurchaserCompanyTypeKnownPage, true).success.value
    .set(PurchaserTypeOfCompanyPage, typeOfCompany).success.value
    .set(AddPurchaserPhoneNumberPage, true).success.value
    .set(EnterPurchaserPhoneNumberPage, "07477777777").success.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockPurchaserCreateOrUpdateService)
  }

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when the return ID is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to prelim before you start page when the UserAnswers data is empty" in {

        val userAnswers = emptyUserAnswers.copy(returnId = Some("RE12345"))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to purchaser before you start page when the purchaser current UserAnswers data is empty" in {

        val userAnswers = emptyUserAnswers.copy(returnId = Some("RE12345"))
          .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains complete individual answers for non main purchaser" in {
        val userAnswers = minimalIndividualUserAnswers.copy(fullReturn = Some(getFullReturn(Some("OTHERPUR001"))))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view without mainPurchaserID for Individual" in {
        val userAnswers = fullIndividualUserAnswers.copy(fullReturn = Some(getFullReturn()))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view when purchaserId matches mainPurchaserID for Individual" in {
        val userAnswers = fullIndividualUserAnswers.copy(fullReturn = Some(getFullReturn(Some("PUR001"))))
          .set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId("PUR001", None)).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view when UserAnswers contains complete company answers for non main purchaser" in {

        val userAnswers = minimalCompanyUserAnswers.copy(fullReturn = Some(getFullReturn(Some("OTHERPUR001"))))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view without mainPurchaserID for company" in {
        val userAnswers = fullCompanyUserAnswers.copy(fullReturn = Some(getFullReturn()))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view when purchaserId matches mainPurchaserID for company" in {
        val userAnswers = fullCompanyUserAnswers.copy(fullReturn = Some(getFullReturn(Some("PUR001"))))
          .set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId("PUR001", None)).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to the appropriate page when UserAnswers is incomplete" in {

        val userAnswers = minimalCompanyUserAnswers.remove(NameOfPurchaserPage).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(CheckMode).url
        }
      }

      "must return OK and the correct view when UserAnswers contains complete individual answers when confirmNameOfPurchaser and mainPurchaserID are set" in {

        val userAnswers = fullIndividualUserAnswers.copy(fullReturn = Some(getFullReturn(Some("PUR001"))))
          .set(ConfirmNameOfThePurchaserPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view when UserAnswers contains complete company answers when confirmNameOfPurchaser and mainPurchaserID are set" in {

        val userAnswers = fullCompanyUserAnswers.copy(fullReturn = Some(getFullReturn(Some("PUR001"))))
          .set(ConfirmNameOfThePurchaserPage, false).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must update purchaser and redirect to PurchaserOverview when all required data is present and valid and purchaser ID present" in {

        val userAnswers = fullCompanyUserAnswers
          .set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId("PUR001", Some("COMPDET001"))).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockPurchaserCreateOrUpdateService.updatePurchaser(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())))
        when(mockPurchaserCreateOrUpdateService.isVendorPurchaserCountBelowMaximum(any()))
          .thenReturn(true)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[PurchaserCreateOrUpdateService].toInstance(mockPurchaserCreateOrUpdateService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must create purchaser and redirect to PurchaserOverview when all required data is present and valid and no purchaser ID present" in {

        val userAnswers = minimalCompanyUserAnswers.copy(fullReturn = Some(getFullReturn(Some("OTHERPUR001"))))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockPurchaserCreateOrUpdateService.createPurchaser(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())))
        when(mockPurchaserCreateOrUpdateService.isVendorPurchaserCountBelowMaximum(any()))
          .thenReturn(true)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[PurchaserCreateOrUpdateService].toInstance(mockPurchaserCreateOrUpdateService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must redirect to overview when vendor and purchaser count exceeds maximum" in {
        val vendors = (1 to 50).map(i => mock[models.Vendor])
        val purchasers = (1 to 49).map(i => mock[models.Purchaser])

        val userAnswers = minimalCompanyUserAnswers.copy(
          fullReturn = Some(getFullReturn(Some("OTHERPUR001")).copy(
            vendor = Some(vendors),
            purchaser = Some(purchasers)
          ))
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }


      "must redirect to ReturnTaskList when return ID is missing" in {

        val userAnswers = emptyUserAnswers

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect back to PurchaserCheckYourAnswersController on JSError" in {

        val userAnswers = minimalIndividualUserAnswers.remove(NameOfPurchaserPage).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url
        }
      }

      "must redirect to JourneyRecoveryController when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
