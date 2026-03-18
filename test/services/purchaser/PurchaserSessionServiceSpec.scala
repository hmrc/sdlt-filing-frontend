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
import models.purchaser.*
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.*
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PurchaserSessionServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private val mockNavigator: Navigator = mock[Navigator]

  private val service = new PurchaserSessionService(mockSessionRepository, mockNavigator)

  private val nextPageCall = Call("GET", "/next-page")

  val typeOfCompany = PurchaserTypeOfCompanyAnswers(
    bank = "YES",
    buildingAssociation = "NO",
    centralGovernment = "NO",
    individualOther = "NO",
    insuranceAssurance = "NO",
    localAuthority = "NO",
    partnership = "NO",
    propertyCompany = "NO",
    publicCorporation = "NO",
    otherCompany = "NO",
    otherFinancialInstitute = "NO",
    otherIncludingCharity = "NO",
    superannuationOrPensionFund = "NO",
    unincorporatedBuilder = "NO",
    unincorporatedSoleTrader = "NO"
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockNavigator)
  }

  "PurchaserSessionService" - {

    "companyOrIndividualPurchaserRemoveFromSession" - {

      "when value is Individual" - {

        "must remove all company pages from session and set WhoIsMakingThePurchasePage" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(CompanyFormOfIdPage, CompanyFormOfId("REG123", "London")).success.value
            .set(PurchaserUTRPage, "UTR123").success.value
            .set(NameOfPurchaserPage, NameOfPurchaser(Some("fore"), Some("forename"), name = "name")).success.value
            .set(RegistrationNumberPage, "UTR123567").success.value
            .set(PurchaserTypeOfCompanyPage, typeOfCompany).success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Individual,
            NormalMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          val capturedAnswers = {
            import org.mockito.ArgumentCaptor
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(captor.capture())
            captor.getValue
          }

          capturedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          capturedAnswers.get(CompanyFormOfIdPage) mustBe None
          capturedAnswers.get(PurchaserUTRPage) mustBe None
          capturedAnswers.get(PurchaserTypeOfCompanyPage) mustBe None
          capturedAnswers.get(NameOfPurchaserPage) mustBe None
          capturedAnswers.get(RegistrationNumberPage) mustBe None
        }

        "must preserve individual pages when removing company pages" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), Some("Michael"), "Smith")).success.value
            .set(PurchaserDateOfBirthPage, LocalDate.of(1985, 3, 15)).success.value
            .set(PurchaserNationalInsurancePage, "AB123456C").success.value
            .set(CompanyFormOfIdPage, CompanyFormOfId("REG123", "London")).success.value
            .set(PurchaserUTRPage, "UTR123").success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Individual,
            NormalMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          val capturedAnswers = {
            import org.mockito.ArgumentCaptor
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(captor.capture())
            captor.getValue
          }

          capturedAnswers.get(NameOfPurchaserPage) mustBe None
          capturedAnswers.get(PurchaserDateOfBirthPage) mustBe Some(LocalDate.of(1985, 3, 15))
          capturedAnswers.get(PurchaserNationalInsurancePage) mustBe Some("AB123456C")
          capturedAnswers.get(CompanyFormOfIdPage) mustBe None
          capturedAnswers.get(PurchaserUTRPage) mustBe None
        }

        "must work correctly in CheckMode" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(PurchaserUTRPage, "UTR123").success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(CheckMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Individual,
            CheckMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          verify(mockNavigator).nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(CheckMode), any())
        }
      }

      "when value is Company" - {

        "must remove all individual pages from session and set WhoIsMakingThePurchasePage" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), Some("Michael"), "Smith")).success.value
            .set(PurchaserDateOfBirthPage, LocalDate.of(1985, 3, 15)).success.value
            .set(PurchaserFormOfIdIndividualPage, PurchaserFormOfIdIndividual("REG123", "London")).success.value
            .set(PurchaserNationalInsurancePage, "AB123456C").success.value
            .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.Yes).success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Company,
            NormalMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          val capturedAnswers = {
            import org.mockito.ArgumentCaptor
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(captor.capture())
            captor.getValue
          }

          capturedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          capturedAnswers.get(NameOfPurchaserPage) mustBe None
          capturedAnswers.get(PurchaserDateOfBirthPage) mustBe None
          capturedAnswers.get(PurchaserFormOfIdIndividualPage) mustBe None
          capturedAnswers.get(PurchaserNationalInsurancePage) mustBe None
          capturedAnswers.get(DoesPurchaserHaveNIPage) mustBe None
        }

        "must preserve company pages when removing individual pages" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(CompanyFormOfIdPage, CompanyFormOfId("COMP123", "Birmingham")).success.value
            .set(PurchaserUTRPage, "UTR456").success.value
            .set(PurchaserTypeOfCompanyPage, typeOfCompany).success.value
            .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
            .set(PurchaserNationalInsurancePage, "AB123456C").success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Company,
            NormalMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          val capturedAnswers = {
            import org.mockito.ArgumentCaptor
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(captor.capture())
            captor.getValue
          }

          capturedAnswers.get(CompanyFormOfIdPage) mustBe Some(CompanyFormOfId("COMP123", "Birmingham"))
          capturedAnswers.get(PurchaserUTRPage) mustBe Some("UTR456")
          capturedAnswers.get(PurchaserTypeOfCompanyPage) mustBe Some(typeOfCompany)
          capturedAnswers.get(NameOfPurchaserPage) mustBe None
          capturedAnswers.get(PurchaserNationalInsurancePage) mustBe None
        }

        "must work correctly in CheckMode" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .set(PurchaserNationalInsurancePage, "AB123456C").success.value

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(CheckMode), any()))
            .thenReturn(nextPageCall)

          val result = service.companyOrIndividualPurchaserRemoveFromSession(
            userAnswers,
            WhoIsMakingThePurchase.Company,
            CheckMode
          ).futureValue

          result mustBe Redirect(nextPageCall)

          verify(mockNavigator).nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(CheckMode), any())
        }
      }

      "must handle empty UserAnswers" in {

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
          .thenReturn(nextPageCall)

        val result = service.companyOrIndividualPurchaserRemoveFromSession(
          userAnswers,
          WhoIsMakingThePurchase.Individual,
          NormalMode
        ).futureValue

        result mustBe Redirect(nextPageCall)

        val capturedAnswers = {
          import org.mockito.ArgumentCaptor
          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(captor.capture())
          captor.getValue
        }

        capturedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
      }

      "must call navigator.nextPage with correct parameters" in {

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
          .thenReturn(nextPageCall)

        service.companyOrIndividualPurchaserRemoveFromSession(
          userAnswers,
          WhoIsMakingThePurchase.Company,
          NormalMode
        ).futureValue

        verify(mockNavigator).nextPage(
          eqTo(WhoIsMakingThePurchasePage),
          eqTo(NormalMode),
          any()
        )
      }

      "must fail when sessionRepository.set fails" in {

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        when(mockSessionRepository.set(any())).thenReturn(Future.failed(new RuntimeException("Database error")))
        when(mockNavigator.nextPage(eqTo(WhoIsMakingThePurchasePage), eqTo(NormalMode), any()))
          .thenReturn(nextPageCall)

        val result = service.companyOrIndividualPurchaserRemoveFromSession(
          userAnswers,
          WhoIsMakingThePurchase.Individual,
          NormalMode
        )

        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Database error"
        }
      }
    }
  }
}