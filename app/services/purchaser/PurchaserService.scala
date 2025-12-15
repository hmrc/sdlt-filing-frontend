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
import pages.purchaser.{ConfirmNameOfThePurchaserPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}

import scala.util.Try

class PurchaserService {

  private def createPurchaserName(purchaser: Purchaser): Option[NameOfPurchaser] = {
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

  def checkPurchaserTypeAndCompanyDetails(purchaserType: WhoIsMakingThePurchase, userAnswers: UserAnswers, continueRoute: Result): Result =
    userAnswers.get(WhoIsMakingThePurchasePage) match {
      case Some(value) if value == purchaserType && purchaserType == WhoIsMakingThePurchase.Company =>
        handleCompanyPurchaser(userAnswers, continueRoute)
      case Some(value) if value == purchaserType && purchaserType == WhoIsMakingThePurchase.Individual =>
        continueRoute
      case Some(_) =>
        Redirect(controllers.routes.GenericErrorController.onPageLoad()) // TODO DTR-1788: redirect to CYA
      case None =>
        Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode))
    }

  private def handleCompanyPurchaser(userAnswers: UserAnswers, continueRoute: Result): Result = {
    val purchasers = userAnswers.fullReturn.flatMap(_.purchaser)

    if (shouldShowCompanyDetailsPage(purchasers, userAnswers)) {
      continueRoute
    } else {
      // TODO Don't show company details page, go to CYA
      Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)) // TODO: change to check your answers
    }
  }

  private def shouldShowCompanyDetailsPage(purchasers: Option[Seq[Purchaser]], userAnswers: UserAnswers): Boolean = {

    val companyDetailsAlreadyExist = userAnswers.fullReturn
      .flatMap(_.companyDetails)
      .flatMap(_.companyTypePensionfund)
      .isDefined

    purchasers match {
      case None => true
      case Some(p) if p.isEmpty => true
      case Some(p) if p.size == 1 => !companyDetailsAlreadyExist
      case _ => false
    }
  }

  def confirmIdentityNextPage(value: PurchaserConfirmIdentity, mode: Mode): Call = {
    if (mode == CheckMode) {
      // TODO: change this to check your answers
      controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad()
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
}
