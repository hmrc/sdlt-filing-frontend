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

import models.*
import models.address.*
import models.purchaser.*
import pages.purchaser.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

class PopulatePurchaserService {

  def createPurchaserName(purchaser: Purchaser): NameOfPurchaser = {
    (purchaser.companyName, purchaser.surname) match {
      case (Some(companyName), _) =>
        NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = companyName
        )

      case (_, Some(surname)) =>
        NameOfPurchaser(
          forename1 = purchaser.forename1,
          forename2 = purchaser.forename2,
          name = surname
        )
      case _ =>
        NameOfPurchaser(
          forename1 = purchaser.forename1,
          forename2 = purchaser.forename2,
          name = ""
        )
    }
  }

   def isMainPurchaser(purchaserId: String, userAnswers: UserAnswers): Boolean = {
    val mainPurchaserID: Option[String] = userAnswers.fullReturn
      .flatMap(_.returnInfo)
      .flatMap(_.mainPurchaserID)

    mainPurchaserID.contains(purchaserId)
  }

  private def buildAddress(line1: String, line2: Option[String], line3: Option[String], line4: Option[String], postcode: Option[String]): Address = {
    Address(
      line1 = line1,
      line2 = line2,
      line3 = line3,
      line4 = line4,
      postcode = postcode
    )
  }

  private def generateCompanyTypeDetails(availableCompanyDetails: CompanyDetails): PurchaserTypeOfCompanyAnswers = {
    PurchaserTypeOfCompanyAnswers(availableCompanyDetails.companyTypeBank.getOrElse("NO"),
      availableCompanyDetails.companyTypeBuildsoc.getOrElse("NO"),
      availableCompanyDetails.companyTypeCentgov.getOrElse("NO"),
      availableCompanyDetails.companyTypeIndividual.getOrElse("NO"),
      availableCompanyDetails.companyTypeInsurance.getOrElse("NO"),
      availableCompanyDetails.companyTypeLocalauth.getOrElse("NO"),
      availableCompanyDetails.companyTypePartnership.getOrElse("NO"),
      availableCompanyDetails.companyTypeProperty.getOrElse("NO"),
      availableCompanyDetails.companyTypePubliccorp.getOrElse("NO"),
      availableCompanyDetails.companyTypeOthercompany.getOrElse("NO"),
      availableCompanyDetails.companyTypeOtherfinancial.getOrElse("NO"),
      availableCompanyDetails.companyTypeOthercharity.getOrElse("NO"),
      availableCompanyDetails.companyTypePensionfund.getOrElse("NO"),
      availableCompanyDetails.companyTypeBuilder.getOrElse("NO"),
      availableCompanyDetails.companyTypeSoletrader.getOrElse("NO"))
  }

  private def purchaserPagesUpdate(userAnswers: UserAnswers,
                                   purchaserName: NameOfPurchaser,
                                   address: Address,
                                   purchaser: Purchaser,
                                   mainPurchaserCheck: Boolean,
                                   id: String
                                  ) = {

    val isCompany = purchaser.isCompany.contains("YES")
    val companyDetailsID = userAnswers.fullReturn.flatMap(_.companyDetails.map(_.companyDetailsID)).flatten
    val phoneDefined: Boolean = purchaser.phone.isDefined

    (purchaser, mainPurchaserCheck, isCompany, phoneDefined) match {
      case (purchaser, true, true, true) =>
        for {
          whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company)
          withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
          withAddress <- withName.set(PurchaserAddressPage, address)
          withAddPhoneNumberAnswer <- withAddress.set(AddPurchaserPhoneNumberPage, true)
          withPhoneNumber <- withAddPhoneNumberAnswer.set(EnterPurchaserPhoneNumberPage, purchaser.phone.get)
          finalAnswers <- withPhoneNumber.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, companyDetailsID))
        } yield finalAnswers
      case (purchaser, true, true, false) =>
        for {
          whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company)
          withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
          withAddress <- withName.set(PurchaserAddressPage, address)
          withAddPhoneNumberAnswer <- withAddress.set(AddPurchaserPhoneNumberPage, false)
          finalAnswers <- withAddPhoneNumberAnswer.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, companyDetailsID))
        } yield finalAnswers
      case (purchaser, true, false, true) => for {
        whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual)
        withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
        withAddress <- withName.set(PurchaserAddressPage, address)
        withAddPhoneNumberAnswer <- withAddress.set(AddPurchaserPhoneNumberPage, true)
        withPhoneNumber <- withAddPhoneNumberAnswer.set(EnterPurchaserPhoneNumberPage, purchaser.phone.get)
        finalAnswers <- withPhoneNumber.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, None))
      } yield finalAnswers
      case (purchaser, true, false, false) => for {
        whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual)
        withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
        withAddress <- withName.set(PurchaserAddressPage, address)
        withAddPhoneNumberAnswer <- withAddress.set(AddPurchaserPhoneNumberPage, false)
        finalAnswers <- withAddPhoneNumberAnswer.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, None))
      } yield finalAnswers
      case (purchaser, false, true, false) => for {
        whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company)
        withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
        withAddress <- withName.set(PurchaserAddressPage, address)
        finalAnswers <- withAddress.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, None))
      } yield finalAnswers
      case _ => for {
        whoIsThePurchaserPage <- userAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual)
        withName <- whoIsThePurchaserPage.set(NameOfPurchaserPage, purchaserName)
        withAddPhoneNumberAnswer <- withName.set(AddPurchaserPhoneNumberPage, false)
        withAddress <- withAddPhoneNumberAnswer.set(PurchaserAddressPage, address)
        finalAnswers <- withAddress.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, None))
      } yield finalAnswers
    }
  }

  private def individualPagesUpdate(previousPages: UserAnswers,
                                    purchaser: Purchaser
                                   ) = {
    (purchaser.nino, purchaser.dateOfBirth, purchaser.registrationNumber, purchaser.placeOfRegistration) match {
      case (Some(nino), Some(dob), _, _) if purchaser.nino.isDefined && purchaser.dateOfBirth.isDefined => for {
        withHasNationalInsurance <- previousPages.set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.Yes)
        withNationalInsurance <- withHasNationalInsurance.set(PurchaserNationalInsurancePage, nino)
        finalAnswers <- withNationalInsurance.set(PurchaserDateOfBirthPage, LocalDate.parse(dob, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
      } yield finalAnswers
      case (_, _, Some(reg), Some(placeOfReg)) => for {
        withHasNationalInsurance <- previousPages.set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No)
        finalAnswers <- withHasNationalInsurance.set(PurchaserFormOfIdIndividualPage,
          PurchaserFormOfIdIndividual(reg, placeOfReg))
      } yield finalAnswers
      case _ =>
        for {
          finalAnswers <- previousPages.set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No)
        } yield finalAnswers
    }
  }

  private def companyPagesUpdate(previousPages: UserAnswers,
                                 purchaser: Purchaser
                                ) = {
    val availableCompanyDetails: Option[CompanyDetails] = previousPages.fullReturn.flatMap(_.companyDetails)
    availableCompanyDetails match {
      case Some(availableCompanyDetails) if availableCompanyDetails.VATReference.isDefined => for {
        withPurchaserConfirmIdentity <- previousPages.set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.VatRegistrationNumber)
        withRegistrationNumber <- withPurchaserConfirmIdentity.set(RegistrationNumberPage, availableCompanyDetails.VATReference.get)
        finalAnswers <- withRegistrationNumber.set(PurchaserTypeOfCompanyPage, generateCompanyTypeDetails(availableCompanyDetails))
      } yield finalAnswers
      case Some(availableCompanyDetails) if availableCompanyDetails.UTR.isDefined => for {
        withUtr <- previousPages.set(PurchaserUTRPage, availableCompanyDetails.UTR.get)
        finalAnswers <- withUtr.set(PurchaserTypeOfCompanyPage, generateCompanyTypeDetails(availableCompanyDetails))
      } yield finalAnswers
      case Some(availableCompanyDetails) if purchaser.registrationNumber.isDefined && purchaser.placeOfRegistration.isDefined => for {
        withPurchaserConfirmIdentity <- previousPages.set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.AnotherFormOfID)
        withFormOfId <- withPurchaserConfirmIdentity.set(CompanyFormOfIdPage, CompanyFormOfId(purchaser.registrationNumber.get, purchaser.placeOfRegistration.get))
        finalAnswers <- withFormOfId.set(PurchaserTypeOfCompanyPage, generateCompanyTypeDetails(availableCompanyDetails))
      } yield finalAnswers
      case _ => Try(previousPages)
    }
  }

  private def finalPurchaserPages(previousPages: UserAnswers, purchaser: Purchaser) = {
    val isTrustee = purchaser.isTrustee.exists(_.toUpperCase == "YES")
    val isConnectedToVendor = purchaser.isConnectedToVendor.exists(_.toUpperCase == "YES")

    for {
      withTrustee <- previousPages.set(IsPurchaserActingAsTrusteePage,
        if (isTrustee) IsPurchaserActingAsTrustee.Yes else IsPurchaserActingAsTrustee.No)
      finalAnswers <- withTrustee.set(PurchaserAndVendorConnectedPage,
        if (isConnectedToVendor) PurchaserAndVendorConnected.Yes else PurchaserAndVendorConnected.No)
    } yield finalAnswers
  }
  
  def populatePurchaserInSession(purchaser: Purchaser,
                                 id: String,
                                 userAnswers: UserAnswers): Try[UserAnswers] = {

    val mainPurchaserCheck = isMainPurchaser(id, userAnswers)
    val companyDetailsID = userAnswers.fullReturn.flatMap(_.companyDetails.map(_.companyDetailsID)).flatten
    (mainPurchaserCheck, purchaser.isCompany, purchaser.address1, purchaser.surname, purchaser.companyName) match {
      case (true, Some("YES"), Some(line1), _, Some(name)) =>
        for {
          withPurchaserPages <- purchaserPagesUpdate(userAnswers, createPurchaserName(purchaser),
            buildAddress(line1, purchaser.address2, purchaser.address3, purchaser.address4, purchaser.postcode), purchaser, mainPurchaserCheck, id)
          withCompanyPages <- companyPagesUpdate(withPurchaserPages, purchaser)
          finalAnswers <- finalPurchaserPages(withCompanyPages, purchaser)
        } yield finalAnswers
      case (true, Some("NO"), Some(line1), Some(name), _) =>
        for {
          withPurchaserPages <- purchaserPagesUpdate(userAnswers, createPurchaserName(purchaser),
            buildAddress(line1, purchaser.address2, purchaser.address3, purchaser.address4, purchaser.postcode), purchaser, mainPurchaserCheck, id)
          withIndividualPages <- individualPagesUpdate(withPurchaserPages, purchaser)
          finalAnswers <- finalPurchaserPages(withIndividualPages, purchaser)
        } yield finalAnswers
      case (false, Some("YES"), Some(line1), _, Some(name)) =>
        for {
          purchaserPages <- purchaserPagesUpdate(userAnswers, createPurchaserName(purchaser),
            buildAddress(line1, purchaser.address2, purchaser.address3, purchaser.address4, purchaser.postcode), purchaser, mainPurchaserCheck, id)
          finalAnswers <- finalPurchaserPages(purchaserPages, purchaser)
        } yield finalAnswers
      case (false, Some("NO"), Some(line1), Some(name), _) =>
        for {
          purchaserPages <- purchaserPagesUpdate(userAnswers, createPurchaserName(purchaser),
            buildAddress(line1, purchaser.address2, purchaser.address3, purchaser.address4, purchaser.postcode), purchaser, mainPurchaserCheck, id)
          finalAnswers <- finalPurchaserPages(purchaserPages, purchaser)
        } yield finalAnswers
      case (true, Some("YES"), _, _, _) =>
        for {
          answersWithId <- userAnswers.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, companyDetailsID))
          typeOfPurchaser <- answersWithId.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company)
          finalAnswers <- finalPurchaserPages(typeOfPurchaser, purchaser)
        } yield finalAnswers
      case (true, Some("NO"), _, _, _) =>
        for {
          answersWithId <- userAnswers.set(PurchaserAndCompanyIdPage, PurchaserAndCompanyId(id, None))
          typeOfPurchaser <- answersWithId.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual)
          finalAnswers <- finalPurchaserPages(typeOfPurchaser, purchaser)
        } yield finalAnswers
      case _ =>
        Try(throw new IllegalStateException(s"Purchaser ${purchaser.purchaserID} is missing required data."))
    }
  }
}
