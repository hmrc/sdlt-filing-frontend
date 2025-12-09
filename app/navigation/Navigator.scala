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

import controllers.routes
import models.*
import pages.*
import pages.preliminary.*
import pages.purchaser.*
import pages.vendor.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case PurchaserIsIndividualPage =>
      _ => controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode)
    case PurchaserSurnameOrCompanyNamePage =>
      _ => controllers.preliminary.routes.PrelimAddressController.redirectToAddressLookup()
    case TransactionTypePage =>
      _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case WhoIsTheVendorPage =>
      _ => controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode)
    case VendorOrCompanyNamePage =>
      _ => controllers.vendor.routes.ConfirmVendorAddressController.onPageLoad(NormalMode)
    case ConfirmVendorAddressPage =>
      _ => controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode)
    case VendorRepresentedByAgentPage =>
      _ => controllers.vendor.routes.AgentNameController.onPageLoad(NormalMode)
    case AgentNamePage =>
      _ => controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
    case AddVendorAgentContactDetailsPage =>
      _ => controllers.vendor.routes.VendorAgentsContactDetailsController.onPageLoad(NormalMode)
    case VendorAgentsContactDetailsPage =>
      _ => controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(NormalMode)
    case DoYouKnowYourAgentReferencePage =>
      _ => controllers.vendor.routes.VendorAgentsReferenceController.onPageLoad(NormalMode)
    case VendorAgentsReferencePage => // TODO: Should navigate to Agent Check Your Answers Page
      _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()


    case page if isPurchaserSection(page) => purchaserRoutes(page)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def isPurchaserSection(page: Page): Boolean = page match {
    case WhoIsMakingThePurchasePage | NameOfPurchaserPage | DoesPurchaserHaveNIPage | PurchaserNationalInsurancePage | PurchaserFormOfIdIndividualPage | AddPurchaserPhoneNumberPage | PurchaserDateOfBirthPage => true
    case _ => false
  }

  private def purchaserRoutes(page: Page): UserAnswers => Call = page match {
    case WhoIsMakingThePurchasePage =>
      _ => controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)
    case NameOfPurchaserPage =>
      _ => controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
    case AddPurchaserPhoneNumberPage =>
      //TODO - update to DTR-1591 route when completed
      _ => controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode)
    case DoesPurchaserHaveNIPage =>
      _ => controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(NormalMode)
    case PurchaserNationalInsurancePage =>
      _ => controllers.purchaser.routes.PurchaserDateOfBirthController.onPageLoad(NormalMode)
    case PurchaserDateOfBirthPage => // TODO: Should redirect to pr-6 is the purchaser acting as a trustee
      _ => controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)
    case PurchaserFormOfIdIndividualPage =>
      _ => routes.ReturnTaskListController.onPageLoad() // TODO: Update to pr-6 'Is the Purchaser a trustee?' in DTR-1682
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    //TODO Change to correct CYA when created
    case WhoIsMakingThePurchasePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case NameOfPurchaserPage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()

    case VendorOrCompanyNamePage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case AgentNamePage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case WhoIsTheVendorPage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case VendorRepresentedByAgentPage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()

    case PurchaserIsIndividualPage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case PurchaserSurnameOrCompanyNamePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case PurchaserFormOfIdIndividualPage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case TransactionTypePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case _ => _ => controllers.routes.ReturnTaskListController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
