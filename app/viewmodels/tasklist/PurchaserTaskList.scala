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

package viewmodels.tasklist

import config.FrontendAppConfig
import models.{CompanyDetails, FullReturn, Purchaser}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object PurchaserTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.purchaserQuestion.heading"),
      rows = Seq(
        buildPurchaserRow(fullReturn)
      )
    )

  def commonFieldsDefined(purchaser: Purchaser): Seq[Boolean] =
    Seq(
      purchaser.isCompany.isDefined,
      purchaser.address1.isDefined,
      purchaser.isTrustee.isDefined,
      purchaser.isConnectedToVendor.isDefined
    )

  private def prelimFieldsDefinedOnly(purchaser: Purchaser, companyDetails: Option[CompanyDetails]): Seq[Boolean] = {
    val isCompany = purchaser.isCompany.exists(_.equalsIgnoreCase("YES"))
    Seq(
      purchaser.isCompany.isDefined,
      if isCompany then purchaser.companyName.isDefined else purchaser.surname.isDefined,
      purchaser.nino.isEmpty,
      purchaser.dateOfBirth.isEmpty,
      purchaser.registrationNumber.isEmpty,
      purchaser.placeOfRegistration.isEmpty,
      if (isCompany) {
        companyDetails.exists(cd =>
          cd.VATReference.isEmpty && cd.UTR.isEmpty
        )
      } else companyDetails.isEmpty,
      purchaser.isTrustee.isEmpty,
      purchaser.isConnectedToVendor.isEmpty
    )
  }

  def mainSpecificFieldsDefined(purchaser: Purchaser, companyDetails: Option[CompanyDetails]): Seq[Boolean] = {
    val isPurchaserCompany      = purchaser.isCompany.exists(_.equalsIgnoreCase("yes"))
    val isCompanyDetailsDefined = companyDetails.isDefined

    val companyFieldsDefined = Seq(
      purchaser.companyName.isDefined,
      companyDetails.exists(x => x.VATReference.isDefined || x.UTR.isDefined) ||
        (purchaser.registrationNumber.isDefined && purchaser.placeOfRegistration.isDefined)
    )

    val isNinoDefined = purchaser.nino.isDefined
    val isDOBDefined  = purchaser.dateOfBirth.isDefined
    val isRegDefined  = purchaser.registrationNumber.isDefined && purchaser.placeOfRegistration.isDefined
    val individualFieldsDefined = Seq(
      purchaser.surname.isDefined
    ) ++ (
      if (isNinoDefined) Seq(isNinoDefined, isDOBDefined)
      else Seq(isRegDefined)
      )

    (isPurchaserCompany, isCompanyDetailsDefined) match {
      case (true, true)  => companyFieldsDefined
      case (true, false) => Seq(isCompanyDetailsDefined)
      case (false, _)    => individualFieldsDefined
    }
  }

  def mandatoryFieldsDefined(purchaser: Purchaser, isMainPurchaser: Boolean, companyDetails: Option[CompanyDetails]): Seq[Boolean] =
    if (isMainPurchaser) commonFieldsDefined(purchaser) ++ mainSpecificFieldsDefined(purchaser, companyDetails)
    else commonFieldsDefined(purchaser)

  def isPrelimPurchaser(fullReturn: FullReturn): Boolean = {
    if (purchasers(fullReturn).size == 1) {
      val purchaser = purchasers(fullReturn).head
      if isMainPurchaser(purchaser, fullReturn) && prelimFieldsDefinedOnly(purchaser, fullReturn.companyDetails).forall(identity) then true
      else false
    } else {
      false
    }
  }

  def isPurchaserComplete(purchaser: Purchaser, isMainPurchaser: Boolean, companyDetails: Option[CompanyDetails]): Boolean =
    mandatoryFieldsDefined(purchaser, isMainPurchaser, companyDetails).forall(identity)

  def purchasers(fullReturn: FullReturn): Seq[Purchaser] =
    fullReturn.purchaser.getOrElse(Seq.empty)

  private def mainPurchaserId(fullReturn: FullReturn): Option[String] =
    fullReturn.returnInfo.flatMap(_.mainPurchaserID)

  private def isMainPurchaser(purchaser: Purchaser, fullReturn: FullReturn): Boolean =
    mainPurchaserId(fullReturn).exists(id => purchaser.purchaserID.contains(id))

  def incompletePurchasers(fullReturn: FullReturn): Seq[Purchaser] =
    purchasers(fullReturn).filterNot(purchaser =>
      isPurchaserComplete(purchaser, isMainPurchaser(purchaser, fullReturn), fullReturn.companyDetails))

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val all = purchasers(fullReturn)
    if (all.isEmpty) Seq(false)
    else all.flatMap(purchaser =>
      mandatoryFieldsDefined(purchaser, isMainPurchaser(purchaser, fullReturn), fullReturn.companyDetails))
  }

  def isPurchaserComplete(fullReturn: FullReturn): Boolean = {
    val all = purchasers(fullReturn)
    all.nonEmpty && all.forall(purchaser =>
      isPurchaserComplete(purchaser, isMainPurchaser(purchaser, fullReturn), fullReturn.companyDetails))
  }

  def purchaserRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url =
      if (isPurchaserComplete(fullReturn))
        controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      else if (isPrelimPurchaser(fullReturn))
        controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
      else if (incompletePurchasers(fullReturn).nonEmpty)
        controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url
      else
        controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _           => true
      },
      messageKey = _ => "tasklist.purchaserQuestion.details",
      url = _ => _ => url,
      tagId = "purchaserQuestionDetailRow",
      checks = scheme => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => Seq()
    )
  }

  def buildPurchaserRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    purchaserRowBuilder(fullReturn).build(fullReturn)
}