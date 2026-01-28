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
import models.purchaser.*
import pages.purchaser.*
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.purchaser.*

import scala.util.Try

class PurchaserService {

  def createPurchaserName(purchaser: Purchaser): Option[NameOfPurchaser] = {
    purchaser.companyName match {
      case Some(companyName) =>
        Some(NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = companyName
        ))

      case None =>
        purchaser.surname.map { surname =>
          NameOfPurchaser(
            forename1 = purchaser.forename1,
            forename2 = purchaser.forename2,
            name = surname
          )
        }
    }
  }

  def whoIsMakingThePurchase(isCompany: Option[String]): WhoIsMakingThePurchase = {
    isCompany match {
      case Some(value) if value.toLowerCase == "yes" => WhoIsMakingThePurchase.Company
      case Some(value) if value.toLowerCase == "no" => WhoIsMakingThePurchase.Individual
      case _ => WhoIsMakingThePurchase.Company
    }
  }

  def populatePurchaserNameInSession(
                                      purchaserCheck: String,
                                      userAnswers: UserAnswers
                                    ): Try[UserAnswers] = {
    val confirmName = if (purchaserCheck == "yes") ConfirmNameOfThePurchaser.Yes else ConfirmNameOfThePurchaser.No

    val purchaserOpt: Option[Purchaser] = userAnswers.fullReturn
      .flatMap(_.purchaser)
      .flatMap(_.headOption)

    purchaserOpt match {
      case Some(purchaser) if purchaserCheck == "yes" && purchaser.purchaserID.isDefined =>
        createPurchaserName(purchaser) match {
          case Some(purchaserName) =>

            for {
              withName <- userAnswers.set(NameOfPurchaserPage, purchaserName)
              withIndividualOrCompany <- withName.set(WhoIsMakingThePurchasePage, whoIsMakingThePurchase(purchaser.isCompany))
              withConfirm <- withIndividualOrCompany.set(ConfirmNameOfThePurchaserPage, confirmName)
            } yield withConfirm

          case None =>
            userAnswers.set(ConfirmNameOfThePurchaserPage, confirmName)
        }

      case _ =>
        userAnswers.set(ConfirmNameOfThePurchaserPage, confirmName)
    }
  }

  def continueIfAddingMainPurchaserWithPurchaserTypeCheck(purchaserType: WhoIsMakingThePurchase,
                                                          userAnswers: UserAnswers,
                                                          continueRoute: Result,
                                                          mode: Mode): Result = {
    continueIfAddingMainPurchaser(
      userAnswers,
      checkPurchaserTypeAndCompanyDetails(
        purchaserType,
        userAnswers,
        continueRoute,
        mode
      ),
      mode
    )
  }

  def continueIfAddingMainPurchaser(userAnswers: UserAnswers, continueRoute: Result, mode: Mode): Result = {
      val mainPurchaserID = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
      val confirmNameOfThePurchaser = userAnswers.get(ConfirmNameOfThePurchaserPage)
      (confirmNameOfThePurchaser, mainPurchaserID, mode) match {
        case (_, _, _) if mode == CheckMode => continueRoute
        case (Some(ConfirmNameOfThePurchaser.Yes), _, _) => continueRoute
        case (Some(ConfirmNameOfThePurchaser.No), _, _) => Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
        case (None, None, _) => continueRoute
        case (None, Some(_), _) => Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
      }
  }
  
  def checkPurchaserTypeAndCompanyDetails(purchaserType: WhoIsMakingThePurchase, userAnswers: UserAnswers, continueRoute: Result, mode: Mode): Result =
    userAnswers.get(WhoIsMakingThePurchasePage) match {
      case Some(value) if value == purchaserType && purchaserType == WhoIsMakingThePurchase.Company =>
        handleCompanyPurchaser(userAnswers, continueRoute, mode)
      case Some(value) if value == purchaserType && purchaserType == WhoIsMakingThePurchase.Individual =>
        continueRoute
      case Some(_) =>
        Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
      case None =>
        Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode))
    }

  private def handleCompanyPurchaser(userAnswers: UserAnswers, continueRoute: Result, mode: Mode): Result = {
    val purchasers = userAnswers.fullReturn.flatMap(_.purchaser)

    if (shouldShowCompanyDetailsPage(purchasers, userAnswers, mode)) {
      continueRoute
    } else {
      Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
    }
  }

  private def shouldShowCompanyDetailsPage(purchasers: Option[Seq[Purchaser]], userAnswers: UserAnswers, mode: Mode): Boolean = {

    val companyDetailsAlreadyExist = userAnswers.fullReturn
      .flatMap(_.companyDetails)
      .flatMap(_.companyTypePensionfund)
      .isDefined

    purchasers match {
      case _ if mode == CheckMode => true
      case None => true
      case Some(p) if p.isEmpty => true
      case Some(p) if p.size == 1 => !companyDetailsAlreadyExist
      case _ => false
    }
  }

  def confirmIdentityNextPage(value: PurchaserConfirmIdentity, mode: Mode): Call = {
    if (mode == CheckMode) {
      value match {
        case PurchaserConfirmIdentity.PartnershipUTR =>
          controllers.purchaser.routes.PurchaserPartnershipUtrController.onPageLoad(CheckMode)
        case PurchaserConfirmIdentity.CorporationTaxUTR =>
          controllers.purchaser.routes.PurchaserCorporationTaxUTRController.onPageLoad(CheckMode)
        case PurchaserConfirmIdentity.VatRegistrationNumber =>
          controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        case PurchaserConfirmIdentity.AnotherFormOfID =>
          controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        case _ =>
          controllers.routes.ReturnTaskListController.onPageLoad()
      }
    } else {
      value match {
        case PurchaserConfirmIdentity.PartnershipUTR =>
          controllers.purchaser.routes.PurchaserPartnershipUtrController.onPageLoad(NormalMode)
        case PurchaserConfirmIdentity.CorporationTaxUTR =>
          controllers.purchaser.routes.PurchaserCorporationTaxUTRController.onPageLoad(NormalMode)
        case PurchaserConfirmIdentity.VatRegistrationNumber =>
          controllers.purchaser.routes.RegistrationNumberController.onPageLoad(NormalMode)
        case PurchaserConfirmIdentity.AnotherFormOfID =>
          controllers.purchaser.routes.CompanyFormOfIdController.onPageLoad(NormalMode)
        case _ =>
          controllers.routes.ReturnTaskListController.onPageLoad()
      }
    }
  }

   def mainPurchaserName(userAnswers: UserAnswers): Option[NameOfPurchaser] =
      for {
        fullReturn <- userAnswers.fullReturn
        purchasers <- fullReturn.purchaser
        returnInfo <- fullReturn.returnInfo
        mainId <- returnInfo.mainPurchaserID
        purchaser <- purchasers.find(_.purchaserID.contains(mainId))
        name <- createPurchaserName(purchaser)
      } yield name

  def companyConditionalSummaryRows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    userAnswers.get(WhoIsMakingThePurchasePage) match {
      case Some(WhoIsMakingThePurchase.Company) =>
        val typeOfCompany = Seq(PurchaserTypeOfCompanySummary.row(Some(userAnswers)))
        val purchaserConfirmIdentity = Seq(PurchaserConfirmIdentitySummary.row(Some(userAnswers)))
        val extraRows = userAnswers.get(PurchaserConfirmIdentityPage) match {
          case Some(PurchaserConfirmIdentity.VatRegistrationNumber) =>
            Seq(RegistrationNumberSummary.row(Some(userAnswers)))
          case Some(PurchaserConfirmIdentity.CorporationTaxUTR) =>
            Seq(PurchaserUTRSummary.row(Some(userAnswers)))
          case Some(PurchaserConfirmIdentity.PartnershipUTR) =>
            Seq(PurchaserUTRSummary.row(Some(userAnswers)))
          case Some(PurchaserConfirmIdentity.AnotherFormOfID) =>
            Seq(CompanyFormOfIdSummary.row(Some(userAnswers)))
          case Some(PurchaserConfirmIdentity.Divider) =>
            Seq(CompanyFormOfIdSummary.row(Some(userAnswers)))
            Seq.empty
          case None =>
            userAnswers.get(PurchaserUTRPage) match {
              case Some(utr) =>
                  Seq(PurchaserUTRSummary.row(Some(userAnswers)))
              case None =>
                Seq.empty
            }
        }
        purchaserConfirmIdentity ++ extraRows ++ typeOfCompany

      case _ =>
        Seq.empty
    }
  }

  def individualConditionalSummaryRows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    userAnswers.get(WhoIsMakingThePurchasePage) match {
      case Some(WhoIsMakingThePurchase.Individual) =>
        val nIQuestion = DoesPurchaserHaveNISummary.row(Some(userAnswers))
        val extraRows = userAnswers.get(DoesPurchaserHaveNIPage) match {
          case Some(DoesPurchaserHaveNI.Yes) =>
            Seq(
              PurchaserNationalInsuranceSummary.row(Some(userAnswers)),
              PurchaserDateOfBirthSummary.row(Some(userAnswers)),
            )
          case Some(DoesPurchaserHaveNI.No) =>
            Seq(PurchaserFormOfIdIndividualSummary.row(Some(userAnswers)))
          case None =>
            Seq.empty
        }
        Seq(nIQuestion) ++ extraRows
      case _ =>
        Seq.empty
    }

  }

  def initialSummaryRows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    WhoIsMakingThePurchaseSummary.row(Some(userAnswers)),
    NameOfPurchaserSummary.row(Some(userAnswers)),
    PurchaserAddressSummary.row(Some(userAnswers)),
    AddPurchaserPhoneNumberSummary.row(Some(userAnswers)),
  ) ++ {
    if (userAnswers.get(AddPurchaserPhoneNumberPage).contains(true)) {
      Seq(EnterPurchaserPhoneNumberSummary.row(Some(userAnswers)))
    }
    else {
      Seq.empty
    }
  }

  def purchaserSessionOptionalQuestionsValidation(sessionData: PurchaserSessionQuestions, userAnswers: UserAnswers): Boolean = {
      val isPurchaserMain: Boolean =
        sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID) == userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
      val isPurchaserFirstMain = sessionData.purchaserCurrent.ConfirmNameOfThePurchaser.contains(ConfirmNameOfThePurchaser.Yes)

      if(isPurchaserMain || isPurchaserFirstMain) {
        sessionData.purchaserCurrent.whoIsMakingThePurchase match {
          case WhoIsMakingThePurchase.Individual.toString =>
            individualMainPurchase(sessionData)
          case WhoIsMakingThePurchase.Company.toString =>
            companyMainPurchaser(sessionData)
        }
      } else {
        nonMainPurchaser(sessionData)
      }
    }

  private def individualMainPurchase(sessionData: PurchaserSessionQuestions): Boolean = {
    val isPhoneNumberYes = sessionData.purchaserCurrent.addPurchaserPhoneNumber
    val isPhoneNumberPresent = sessionData.purchaserCurrent.enterPurchaserPhoneNumber.isDefined
    val doesPurchaserHaveNI = sessionData.purchaserCurrent.doesPurchaserHaveNI.contains(DoesPurchaserHaveNI.Yes)
    val isNIPresent = sessionData.purchaserCurrent.nationalInsuranceNumber.isDefined
    val isPurchaserFormOfIdIndividualPresent = sessionData.purchaserCurrent.purchaserFormOfIdIndividual.isDefined
    val isDobPresent = sessionData.purchaserCurrent.purchaserDateOfBirth.isDefined
    val isPurchaserActingAsTrusteePresent = sessionData.purchaserCurrent.isPurchaserActingAsTrustee.isDefined
    val isPurchaserAndVendorConnectedPresent = sessionData.purchaserCurrent.purchaserAndVendorConnected.isDefined

    if (isPhoneNumberYes && !isPhoneNumberPresent) return false // User said yes to phone number and phone number is not present
    if (doesPurchaserHaveNI) {
      if (!isNIPresent) return false // User said yes to NiNo and NiNO is not present
      if (!isDobPresent) return false // User has said yes to Nino and DoB is not present
    } else {
      if (!isPurchaserFormOfIdIndividualPresent) return false // User said no to NiNO and form Of Id individual is not present
    }
    if (!isPurchaserActingAsTrusteePresent || !isPurchaserAndVendorConnectedPresent) return false // Any of Acting as a Trustee/Purchaser and Vendor connected is not present

    true
  }

  private def companyMainPurchaser(sessionData: PurchaserSessionQuestions): Boolean = {
    val isPhoneNumberYes = sessionData.purchaserCurrent.addPurchaserPhoneNumber
    val isPhoneNumberPresent = sessionData.purchaserCurrent.enterPurchaserPhoneNumber.isDefined
    val isUTRPresent = sessionData.purchaserCurrent.purchaserUTRPage.isDefined
    val isVATPresent = sessionData.purchaserCurrent.registrationNumber.isDefined
    val isAnotherFormOfIdPresent = sessionData.purchaserCurrent.purchaserFormOfIdCompany.isDefined
    val isTypeOfCompanyPresent = sessionData.purchaserCurrent.purchaserTypeOfCompany.isDefined
    val isPurchaserActingAsTrusteePresent = sessionData.purchaserCurrent.isPurchaserActingAsTrustee.isDefined
    val isPurchaserAndVendorConnectedPresent = sessionData.purchaserCurrent.purchaserAndVendorConnected.isDefined

    if (isPhoneNumberYes && !isPhoneNumberPresent) return false // User said yes to phone number and phone number is not present
    if (!isUTRPresent && !isVATPresent && !isAnotherFormOfIdPresent) return false // Any Form of ID is not present
    if (!isTypeOfCompanyPresent || !isPurchaserActingAsTrusteePresent || !isPurchaserAndVendorConnectedPresent) return false // Any of Company type/Acting as a Trustee/Purchaser and Vendor connected is not present

    true
  }

  private def nonMainPurchaser(sessionData: PurchaserSessionQuestions): Boolean = {
    val isPhoneNumberYes = sessionData.purchaserCurrent.addPurchaserPhoneNumber
    val isPhoneNumberPresent = sessionData.purchaserCurrent.enterPurchaserPhoneNumber.isDefined

    if (isPhoneNumberYes && !isPhoneNumberPresent) return false // User said yes to phone number and phone number is not present

    true
  }

}
