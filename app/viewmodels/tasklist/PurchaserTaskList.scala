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
import models.FullReturn
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

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val mainPurchaserID: Option[String] = fullReturn.returnInfo.flatMap(_.mainPurchaserID)
    val mainPurchaser = fullReturn.purchaser.flatMap(_.find(purchaser => mainPurchaserID.equals(purchaser.purchaserID)))

    val isPurchaserCompany = mainPurchaser.exists(_.isCompany.exists(_.equalsIgnoreCase("yes")))
    val isCompanyDetailsDefined = fullReturn.companyDetails.isDefined

    // common purchaser fields
    val commonFieldsDefined = Seq(
      mainPurchaser.exists(_.isCompany.isDefined),
      mainPurchaser.exists(_.address1.isDefined),
      mainPurchaser.exists(_.isTrustee.isDefined),
      mainPurchaser.exists(_.isConnectedToVendor.isDefined)
    )

    // purchaser company fields
    val companyFieldsDefined = Seq(
      mainPurchaser.exists(_.companyName.isDefined),
      fullReturn.companyDetails.exists(x => x.VATReference.isDefined || x.UTR.isDefined) ||
        mainPurchaser.exists(x => x.registrationNumber.isDefined && x.placeOfRegistration.isDefined)
    )

    // purchaser individual fields
    val isNinoDefined = mainPurchaser.exists(_.nino.isDefined)
    val isDOBDefined = mainPurchaser.exists(_.dateOfBirth.isDefined)
    val isRegDefined = mainPurchaser.exists(x => x.registrationNumber.isDefined && x.placeOfRegistration.isDefined)

    val individualFieldsDefined = Seq(
      mainPurchaser.exists(_.surname.isDefined)
    ) ++ (
      if (isNinoDefined) {
        Seq(isNinoDefined, isDOBDefined)
      } else {
        Seq(isRegDefined)
      })

    (isPurchaserCompany, isCompanyDetailsDefined) match {
      case (true, true) => commonFieldsDefined ++ companyFieldsDefined
      case (true, false) => commonFieldsDefined ++ Seq(isCompanyDetailsDefined)
      case (false, _) => commonFieldsDefined ++ individualFieldsDefined
    }
  }
    
  def isPurchaserComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  def purchaserRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val mainPurchaserID = fullReturn.returnInfo.flatMap(_.mainPurchaserID)

    val url = fullReturn.purchaser match {
      case Some(list) if list.length >1 => controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      case Some(list) if list.exists( x => x.purchaserID == mainPurchaserID && x.address1.isEmpty)
      => controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
      case Some(list) if list.nonEmpty => controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      case _ => controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
    }
    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.purchaserQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "purchaserQuestionDetailRow",
      checks = scheme => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => Seq()
    )
  }

  def buildPurchaserRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    purchaserRowBuilder(fullReturn).build(fullReturn)

}
