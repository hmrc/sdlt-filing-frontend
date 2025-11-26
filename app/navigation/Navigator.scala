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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*
import models.vendor.*
import pages.preliminary.*
import pages.purchaser.*
import pages.vendor.*

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case PurchaserIsIndividualPage =>
      _ => controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode)
    case PurchaserSurnameOrCompanyNamePage =>
      _ => controllers.preliminary.routes.PrelimAddressController.redirectToAddressLookup()
    case VendorRepresentedByAgentPage =>
      _ => controllers.vendor.routes.AgentNameController.onPageLoad(NormalMode)
    case TransactionTypePage =>
      _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case AgentNamePage =>
      _ => controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
    case WhoIsTheVendorPage =>
      _ => controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode)
    case VendorOrCompanyNamePage =>
      _ => controllers.vendor.routes.ConfirmVendorAddressController.onPageLoad(NormalMode)
    case ConfirmVendorAddressPage =>
      _ => controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode)
    case AddVendorAgentContactDetailsPage =>
      _ => controllers.routes.ReturnTaskListController.onPageLoad() //TODO DTR-1019 Redirect to Vendor Agent contact details page
    case DoYouKnowYourAgentReferencePage =>
      userAnswers =>
        userAnswers.get(DoYouKnowYourAgentReferencePage) match {
          case Some(DoYouKnowYourAgentReference.Yes) =>
            // TODO: This will need to redirect to CYA page
            controllers.routes.ReturnTaskListController.onPageLoad()

          case Some(DoYouKnowYourAgentReference.No) =>
            // TODO: This will need to redirect to CYA page
            controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
          // TODO: This will need to redirect to CYA page
          case _ =>
            routes.IndexController.onPageLoad()
        }

    case page if isPurchaserSection(page) => purchaserRoutes(page)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def isPurchaserSection(page: Page): Boolean = page match {
    case WhoIsMakingThePurchasePage | NameOfPurchaserPage => true
    case _ => false
  }

  private def purchaserRoutes(page: Page): UserAnswers => Call = page match {
    case WhoIsMakingThePurchasePage =>
      _ => controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)
    case NameOfPurchaserPage =>
      _ => controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
    case _ => _ => routes.IndexController.onPageLoad()
  }


  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
