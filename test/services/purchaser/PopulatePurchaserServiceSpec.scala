/*
 * Copyright 2025 HM Revenue & Customs
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
import models.*
import models.address.Address
import models.purchaser.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.*
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PopulatePurchaserServiceSpec extends SpecBase with MockitoSugar {

  val service = new PopulatePurchaserService()

  val companyDetails = CompanyDetails(
    companyDetailsID = Some("COMP001"),
    VATReference = Some("VAT123"),
    companyTypeBank = Some("NO"),
    companyTypeBuildsoc = Some("NO"),
    companyTypeCentgov = Some("NO"),
    companyTypeIndividual = Some("NO"),
    companyTypeInsurance = Some("NO"),
    companyTypeLocalauth = Some("NO"),
    companyTypePartnership = Some("NO"),
    companyTypeProperty = Some("YES"),
    companyTypePubliccorp = Some("NO"),
    companyTypeOthercompany = Some("NO"),
    companyTypeOtherfinancial = Some("NO"),
    companyTypeOthercharity = Some("NO"),
    companyTypePensionfund = Some("NO"),
    companyTypeBuilder = Some("NO"),
    companyTypeSoletrader = Some("NO")
  )

  private val individualPurchaser = Purchaser(
    purchaserID = Some("PUR001"),
    forename1 = Some("John"),
    forename2 = Some("Michael"),
    surname = Some("Smith"),
    address1 = Some("20 Test Road"),
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = Some("L1 1AA"),
    isCompany = Some("NO"),
    phone = Some("07123456789"),
    nino = Some("AB123456C"),
    dateOfBirth = Some("10/03/1992")
  )

  private val companyPurchaser = Purchaser(
    purchaserID = Some("PUR002"),
    companyName = Some("Test Company Ltd"),
    address1 = Some("10 Test Street"),
    address2 = Some("Floor 2"),
    address3 = Some("Manchester"),
    address4 = Some("Greater Manchester"),
    postcode = Some("M1 1AA"),
    isCompany = Some("YES"),
    phone = Some("01234567890")
  )

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "2123",
    stornId = "STORN",
    vendor = None,
    purchaser = None,
    transaction = None
  )

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

  private def fullReturnWithIndividualMainPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaser)),
      returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001"))),
      companyDetails = Some(companyDetails))

  private def fullReturnWithCompanyMainPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaser)),
      returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR002"))),
      companyDetails = Some(companyDetails))

  private def fullReturnWithIndividualPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaser)),
      returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR00"))))

  private def fullReturnWithCompanyPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH00"))))

  "PopulatePurchaserService" - {

    "createPurchaserName" - {

      "must create name from company name when company name is present" in {
        val purchaser = Purchaser(
          forename1 = None,
          forename2 = None,
          surname = Some("Mr. Company")
        )

        val result = service.createPurchaserName(purchaser)

        result mustBe NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = "Mr. Company"
        )
      }

      "must create name from surname when surname is present and company name is not" in {
        val purchaser = Purchaser(
          forename1 = Some("John"),
          forename2 = Some("Michael"),
          surname = Some("Smith"),
          companyName = None
        )

        val result = service.createPurchaserName(purchaser)

        result mustBe NameOfPurchaser(
          forename1 = Some("John"),
          forename2 = Some("Michael"),
          name = "Smith"
        )
      }

      "must create empty name when both company name and surname are missing" in {
        val purchaser = Purchaser(
          companyName = None,
          surname = None,
          forename1 = Some("John"),
          forename2 = Some("Michael")
        )

        val result = service.createPurchaserName(purchaser)

        result mustBe NameOfPurchaser(
          forename1 = Some("John"),
          forename2 = Some("Michael"),
          name = ""
        )
      }
    }

    "PopulatePurchaserService" - {

      "createPurchaserName" - {

        "must create name from company name when company name is present" in {
          val purchaser = Purchaser(
            companyName = Some("ABC Corporation"),
            surname = Some("Smith"),
            forename1 = Some("John"),
            forename2 = Some("Michael")
          )

          val result = service.createPurchaserName(purchaser)

          result mustBe NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "ABC Corporation"
          )
        }

        "must create name from surname when surname is present and company name is not" in {
          val purchaser = Purchaser(
            companyName = None,
            surname = Some("Smith"),
            forename1 = Some("John"),
            forename2 = Some("Michael")
          )

          val result = service.createPurchaserName(purchaser)

          result mustBe NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          )
        }

        "must create empty name when both company name and surname are missing" in {
          val purchaser = Purchaser(
            companyName = None,
            surname = None,
            forename1 = Some("John"),
            forename2 = Some("Michael")
          )

          val result = service.createPurchaserName(purchaser)

          result mustBe NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = ""
          )
        }
      }

      "populatePurchaserInSession" - {

        "must successfully populate session for main purchaser individual with phone number and NINO" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualMainPurchaser))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(individualPurchaser, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          ))
          updatedAnswers.get(AddPurchaserPhoneNumberPage) mustBe Some(true)
          updatedAnswers.get(EnterPurchaserPhoneNumberPage) mustBe Some("07123456789")
          updatedAnswers.get(DoesPurchaserHaveNIPage) mustBe Some(DoesPurchaserHaveNI.Yes)
          updatedAnswers.get(PurchaserNationalInsurancePage) mustBe Some("AB123456C")
          updatedAnswers.get(PurchaserDateOfBirthPage) mustBe Some(LocalDate.of(1992, 03, 10))
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser company with phone number" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyMainPurchaser))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(companyPurchaser, "PUR002", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Test Company Ltd"
          ))
          updatedAnswers.get(PurchaserAddressPage) mustBe Some(Address(
            line1 = "10 Test Street",
            line2 = Some("Floor 2"),
            line3 = Some("Manchester"),
            line4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA")
          ))
          updatedAnswers.get(AddPurchaserPhoneNumberPage) mustBe Some(true)
          updatedAnswers.get(EnterPurchaserPhoneNumberPage) mustBe Some("01234567890")
          updatedAnswers.get(PurchaserAndCompanyIdPage) mustBe Some(PurchaserAndCompanyId("PUR002", Some("COMP001")))
          updatedAnswers.get(PurchaserConfirmIdentityPage) mustBe Some(PurchaserConfirmIdentity.VatRegistrationNumber)
          updatedAnswers.get(RegistrationNumberPage) mustBe Some("VAT123")
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser individual without phone number" in {

          val individualPurchaserNoPhone = Purchaser(
            purchaserID = Some("PUR001"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            address1 = Some("20 Test Road"),
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = Some("L1 1AA"),
            isCompany = Some("NO"),
            phone = None,
            nino = Some("AB123456C"),
            dateOfBirth = Some("10/03/1992")
          )

          val fullReturnWithIndividualMainPurchaserNoPhone: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaserNoPhone)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001"))),
              companyDetails = Some(companyDetails))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualMainPurchaserNoPhone))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(individualPurchaserNoPhone, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          ))
          updatedAnswers.get(AddPurchaserPhoneNumberPage) mustBe Some(false)
          updatedAnswers.get(EnterPurchaserPhoneNumberPage) mustBe None
          updatedAnswers.get(DoesPurchaserHaveNIPage) mustBe Some(DoesPurchaserHaveNI.Yes)
          updatedAnswers.get(PurchaserNationalInsurancePage) mustBe Some("AB123456C")
          updatedAnswers.get(PurchaserDateOfBirthPage) mustBe Some(LocalDate.of(1992, 03, 10 ))
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for non-main purchaser company" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(companyPurchaser, "PUR002", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Test Company Ltd"
          ))
          updatedAnswers.get(PurchaserAddressPage) mustBe Some(Address(
            line1 = "10 Test Street",
            line2 = Some("Floor 2"),
            line3 = Some("Manchester"),
            line4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA")
          ))
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser company without phone number" in {

          val companyPurchaserNoPhone = Purchaser(
            purchaserID = Some("PUR002"),
            companyName = Some("Test Company Ltd"),
            address1 = Some("10 Test Street"),
            address2 = Some("Floor 2"),
            address3 = Some("Manchester"),
            address4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA"),
            isCompany = Some("YES"),
            phone = None
          )

          val fullReturnWithCompanyMainPurchaserNoPhone: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaserNoPhone)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR002"))),
              companyDetails = Some(companyDetails))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyMainPurchaserNoPhone))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(companyPurchaserNoPhone, "PUR002", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Test Company Ltd"
          ))
          updatedAnswers.get(PurchaserAddressPage) mustBe Some(Address(
            line1 = "10 Test Street",
            line2 = Some("Floor 2"),
            line3 = Some("Manchester"),
            line4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA")
          ))
          updatedAnswers.get(AddPurchaserPhoneNumberPage) mustBe Some(false)
          updatedAnswers.get(EnterPurchaserPhoneNumberPage) mustBe None
          updatedAnswers.get(PurchaserAndCompanyIdPage) mustBe Some(PurchaserAndCompanyId("PUR002", Some("COMP001")))
          updatedAnswers.get(PurchaserConfirmIdentityPage) mustBe Some(PurchaserConfirmIdentity.VatRegistrationNumber)
          updatedAnswers.get(RegistrationNumberPage) mustBe Some("VAT123")
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for purchaser company without phone number" in {

          val companyPurchaserNoPhone = Purchaser(
            purchaserID = Some("PUR003"),
            companyName = Some("Test Company Ltd"),
            address1 = Some("10 Test Street"),
            address2 = Some("Floor 2"),
            address3 = Some("Manchester"),
            address4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA"),
            isCompany = Some("YES"),
            phone = None
          )

          val fullReturnWithCompanyMainPurchaserNoPhone: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaserNoPhone)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR002"))))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyMainPurchaserNoPhone))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(companyPurchaserNoPhone, "PUR003", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Test Company Ltd"
          ))
          updatedAnswers.get(PurchaserAddressPage) mustBe Some(Address(
            line1 = "10 Test Street",
            line2 = Some("Floor 2"),
            line3 = Some("Manchester"),
            line4 = Some("Greater Manchester"),
            postcode = Some("M1 1AA")
          ))
          updatedAnswers.get(AddPurchaserPhoneNumberPage) mustBe None
          updatedAnswers.get(EnterPurchaserPhoneNumberPage) mustBe None
          updatedAnswers.get(PurchaserAndCompanyIdPage) mustBe Some(PurchaserAndCompanyId("PUR003", None))
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for non-main purchaser individual" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(individualPurchaser, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          ))
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser individual with registration number instead of NINO" in {

          val purchaser = Purchaser(
            purchaserID = Some("PUR003"),
            forename1 = Some("Robert"),
            surname = Some("Johnson"),
            address1 = Some("50 Park Lane"),
            isCompany = Some("NO"),
            phone = Some("02012345678"),
            registrationNumber = Some("REG123"),
            placeOfRegistration = Some("London")
          )

          val fullReturnWithIndividualMainPurchaserReg: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR003"))),
              companyDetails = Some(companyDetails))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualMainPurchaserReg))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaser, "PUR003", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(DoesPurchaserHaveNIPage) mustBe Some(DoesPurchaserHaveNI.No)
          updatedAnswers.get(PurchaserFormOfIdIndividualPage) mustBe Some(
            PurchaserFormOfIdIndividual("REG123", "London")
          )
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser individual with no NINO or registration number" in {

          val purchaser = Purchaser(
            purchaserID = Some("PUR004"),
            forename1 = Some("Alice"),
            surname = Some("Williams"),
            address1 = Some("60 Main Street"),
            isCompany = Some("NO"),
            phone = Some("03012345678")
          )

          val fullReturnWithIndividualMainPurchaserNoNino: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR004"))),
              companyDetails = Some(companyDetails))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualMainPurchaserNoNino))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaser, "PUR004", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(DoesPurchaserHaveNIPage) mustBe Some(DoesPurchaserHaveNI.No)
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser company with UTR" in {

          val purchaser = Purchaser(
            purchaserID = Some("PUR005"),
            companyName = Some("Bank Company Ltd"),
            address1 = Some("70 Financial Street"),
            isCompany = Some("YES"),
            phone = Some("04012345678")
          )

          val companyDetailsUTR = CompanyDetails(
            companyDetailsID = Some("COMP002"),
            UTR = Some("UTR456"),
            companyTypeBank = Some("YES"),
            companyTypeBuildsoc = Some("NO"),
            companyTypeCentgov = Some("NO"),
            companyTypeIndividual = Some("NO"),
            companyTypeInsurance = Some("NO"),
            companyTypeLocalauth = Some("NO"),
            companyTypePartnership = Some("NO"),
            companyTypeProperty = Some("NO"),
            companyTypePubliccorp = Some("NO"),
            companyTypeOthercompany = Some("NO"),
            companyTypeOtherfinancial = Some("NO"),
            companyTypeOthercharity = Some("NO"),
            companyTypePensionfund = Some("NO"),
            companyTypeBuilder = Some("NO"),
            companyTypeSoletrader = Some("NO")
          )

          val fullReturnWithCompanyMainPurchaserUTR: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR005"))),
              companyDetails = Some(companyDetailsUTR))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyMainPurchaserUTR))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaser, "PUR005", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserUTRPage) mustBe Some("UTR456")
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session for main purchaser company with another form of ID" in {

          val purchaser = Purchaser(
            purchaserID = Some("PUR006"),
            companyName = Some("Partnership Ltd"),
            address1 = Some("80 Commerce Street"),
            isCompany = Some("YES"),
            phone = Some("05012345678"),
            registrationNumber = Some("COMREG789"),
            placeOfRegistration = Some("Birmingham")
          )

          val companyDetailsFormId = CompanyDetails(
            companyDetailsID = Some("COMP003"),
            companyTypePartnership = Some("YES"),
            companyTypeBank = Some("NO"),
            companyTypeBuildsoc = Some("NO"),
            companyTypeCentgov = Some("NO"),
            companyTypeIndividual = Some("NO"),
            companyTypeInsurance = Some("NO"),
            companyTypeLocalauth = Some("NO"),
            companyTypeProperty = Some("NO"),
            companyTypePubliccorp = Some("NO"),
            companyTypeOthercompany = Some("NO"),
            companyTypeOtherfinancial = Some("NO"),
            companyTypeOthercharity = Some("NO"),
            companyTypePensionfund = Some("NO"),
            companyTypeBuilder = Some("NO"),
            companyTypeSoletrader = Some("NO")
          )

          val fullReturnWithCompanyMainPurchaserUTR: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR005"))),
              companyDetails = Some(companyDetailsFormId))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithCompanyMainPurchaserUTR))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaser, "PUR005", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserConfirmIdentityPage) mustBe Some(PurchaserConfirmIdentity.AnotherFormOfID)
          updatedAnswers.get(CompanyFormOfIdPage) mustBe Some(
            CompanyFormOfId("COMREG789", "Birmingham")
          )
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session with trustee and connected vendor flags" in {
          val purchaserTrusteeVendor = Purchaser(
            purchaserID = Some("PUR006"),
            companyName = Some("Partnership Ltd"),
            address1 = Some("80 Commerce Street"),
            isCompany = Some("YES"),
            phone = Some("05012345678"),
            registrationNumber = Some("COMREG789"),
            placeOfRegistration = Some("Birmingham"),
            isTrustee = Some("YES"),
            isConnectedToVendor = Some("YES")
          )

          val fullReturnWithIndividualPurchaserTrusteeVendor: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaserTrusteeVendor)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR006"))))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualPurchaserTrusteeVendor))


          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaserTrusteeVendor, "PUR006", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.Yes)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.Yes)
        }

        "must successfully populate session with trustee flag only" in {
          val purchaserTrustee = Purchaser(
            purchaserID = Some("PUR007"),
            companyName = Some("Partnership Ltd"),
            address1 = Some("80 Commerce Street"),
            isCompany = Some("YES"),
            phone = Some("05012345678"),
            registrationNumber = Some("COMREG789"),
            placeOfRegistration = Some("Birmingham"),
            isTrustee = Some("YES"),
            isConnectedToVendor = Some("NO")
          )

          val fullReturnWithIndividualPurchaserTrustee: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaserTrustee)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR007"))))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualPurchaserTrustee))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaserTrustee, "PUR007", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.Yes)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session with connected vendor flag only" in {
          val purchaserConnectedVendor = Purchaser(
            purchaserID = Some("PUR001"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            address1 = Some("20 Test Road"),
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = Some("L1 1AA"),
            isCompany = Some("NO"),
            phone = Some("07123456789"),
            nino = Some("AB123456C"),
            dateOfBirth = Some("10/03/1992"),
            isTrustee = Some("NO"),
            isConnectedToVendor = Some("YES")
          )

          val fullReturn = fullReturnWithIndividualMainPurchaser.copy(
            purchaser = Some(Seq(purchaserConnectedVendor))
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaserConnectedVendor, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.Yes)
        }

        "must successfully populate session with isTrustee NO and isConnectedToVendor NO explicitly set" in {
          val purchaserExplicitNo = Purchaser(
            purchaserID = Some("PUR001"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            address1 = Some("20 Test Road"),
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = Some("L1 1AA"),
            isCompany = Some("NO"),
            phone = Some("07123456789"),
            nino = Some("AB123456C"),
            dateOfBirth = Some("10/03/1992"),
            isTrustee = Some("NO"),
            isConnectedToVendor = Some("NO")
          )

          val fullReturn = fullReturnWithIndividualMainPurchaser.copy(
            purchaser = Some(Seq(purchaserExplicitNo))
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(purchaserExplicitNo, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session with no trustee or connected vendor flags" in {

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithIndividualMainPurchaser))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(individualPurchaser, "PUR001", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must not leak trustee/vendor flags from another purchaser in the full return" in {
          val mainPurchaserWithFlags = Purchaser(
            purchaserID = Some("PUR001"),
            forename1 = Some("Glenn"),
            surname = Some("Goodwin"),
            address1 = Some("39 Silkmore Crescent"),
            postcode = Some("ST17 4JL"),
            isCompany = Some("NO"),
            phone = Some("07123456789"),
            nino = Some("AB123456C"),
            dateOfBirth = Some("10/03/1992"),
            isTrustee = Some("YES"),
            isConnectedToVendor = Some("YES")
          )

          val secondPurchaser = Purchaser(
            purchaserID = Some("PUR002"),
            forename1 = Some("Scott"),
            surname = Some("Goodwin"),
            address1 = Some("39 Silkmore Crescent"),
            postcode = Some("ST17 4JL"),
            isCompany = Some("NO"),
            isTrustee = Some("NO"),
            isConnectedToVendor = Some("NO")
          )

          val fullReturn = emptyFullReturn.copy(
            purchaser = Some(Seq(mainPurchaserWithFlags, secondPurchaser)),
            returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001")))
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.populatePurchaserInSession(secondPurchaser, "PUR002", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session with incomplete company purchaser" in {
          val incompleteCompanyPurchaser = Purchaser(
            purchaserID = Some("PUR008"),
            surname = None,
            address1 = None,
            isCompany = Some("YES")
          )

          val companyDetails = CompanyDetails(companyDetailsID = Some("CD001"))

          val fullReturn: FullReturn =
            emptyFullReturn.copy(
              purchaser = Some(Seq(incompleteCompanyPurchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR008"))),
              companyDetails = Some(companyDetails)
            )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))


          val result = service.populatePurchaserInSession(incompleteCompanyPurchaser, "PUR008", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get
          updatedAnswers.get(PurchaserAndCompanyIdPage) mustBe Some(PurchaserAndCompanyId("PUR008", Some("CD001")))
          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must successfully populate session with incomplete individual purchaser" in {
          val incompleteIndividualPurchaser = Purchaser(
            purchaserID = Some("PUR008"),
            surname = None,
            address1 = None,
            isCompany = Some("NO")
          )

          val fullReturnWithNoAddress: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(incompleteIndividualPurchaser)),
              returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR008"))))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnWithNoAddress))


          val result = service.populatePurchaserInSession(incompleteIndividualPurchaser, "PUR008", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get
          updatedAnswers.get(PurchaserAndCompanyIdPage) mustBe Some(PurchaserAndCompanyId("PUR008", None))
          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(IsPurchaserActingAsTrusteePage) mustBe Some(IsPurchaserActingAsTrustee.No)
          updatedAnswers.get(PurchaserAndVendorConnectedPage) mustBe Some(PurchaserAndVendorConnected.No)
        }

        "must fail when isCompany is missing" in {
          val purchaserMissingIsCompany = Purchaser(
            purchaserID = Some("PUR019"),
            surname = Some("Martinez"),
            address1 = Some("150 Main Boulevard"),
            isCompany = None
          )

          val fullReturnMissingIsCompany: FullReturn =
            emptyFullReturn.copy(purchaser = Some(Seq(purchaserMissingIsCompany)))

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturnMissingIsCompany))


          val result = service.populatePurchaserInSession(purchaserMissingIsCompany, "PUR019", userAnswers)

          result mustBe a[Failure[_]]
          result.failed.get mustBe an[IllegalStateException]
        }
      }
    }
  }
}