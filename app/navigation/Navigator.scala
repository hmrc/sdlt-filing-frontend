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
import pages.purchaserAgent.*
import pages.vendor.*
import pages.vendorAgent.AgentNamePage
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
    case AgentNamePage =>
      _ => controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
    case AddVendorAgentContactDetailsPage =>
      _ => controllers.vendor.routes.VendorAgentsContactDetailsController.onPageLoad(NormalMode)
    case VendorAgentsContactDetailsPage =>
      _ => controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(NormalMode)
    case DoYouKnowYourAgentReferencePage =>
      _ => controllers.vendor.routes.VendorAgentsReferenceController.onPageLoad(NormalMode)
    case VendorAgentsReferencePage =>
      _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()

    case page if isPurchaserSection(page) => purchaserRoutes(page)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def isPurchaserSection(page: Page): Boolean = page match {

    case WhoIsMakingThePurchasePage | NameOfPurchaserPage | DoesPurchaserHaveNIPage
         | PurchaserNationalInsurancePage | PurchaserFormOfIdIndividualPage | AddPurchaserPhoneNumberPage
         | PurchaserDateOfBirthPage | EnterPurchaserPhoneNumberPage | PurchaserUTRPage
         | RegistrationNumberPage | PurchaserTypeOfCompanyPage | CompanyFormOfIdPage
         | PurchaserAndVendorConnectedPage | IsPurchaserActingAsTrusteePage | ConfirmNameOfThePurchaserPage
         | PurchaserAgentsContactDetailsPage | PurchaserAgentNamePage | AddPurchaserAgentReferenceNumberPage
         | PurchaserAgentReferencePage | AddContactDetailsForPurchaserAgentPage | SelectPurchaserAgentPage
         | PurchaserAgentBeforeYouStartPage | PurchaserAgentAuthorisedPage => true

    case _ => false
  }

  private def purchaserRoutes(page: Page): UserAnswers => Call = page match {
    case WhoIsMakingThePurchasePage =>
      _ => controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)
    case NameOfPurchaserPage =>
      _ => controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
    case AddPurchaserPhoneNumberPage =>
      _ => controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(NormalMode)
    case EnterPurchaserPhoneNumberPage =>
      _ => controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode)
    case DoesPurchaserHaveNIPage =>
      _ => controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(NormalMode)
    case PurchaserNationalInsurancePage =>
      _ => controllers.purchaser.routes.PurchaserDateOfBirthController.onPageLoad(NormalMode)
    case PurchaserDateOfBirthPage =>
      _ => controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
    case PurchaserFormOfIdIndividualPage =>
      _ => controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
    case PurchaserUTRPage =>
      _ => controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
    case RegistrationNumberPage =>
      _ => controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
    case IsPurchaserActingAsTrusteePage =>
      _ => controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(NormalMode)
    case PurchaserTypeOfCompanyPage =>
      _ => controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
    case CompanyFormOfIdPage =>
      _ => controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
    case PurchaserAndVendorConnectedPage =>
      _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case ConfirmNameOfThePurchaserPage =>
      _ => controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()

    case PurchaserAgentBeforeYouStartPage =>
      _ => controllers.purchaserAgent.routes.SelectPurchaserAgentController.onPageLoad(NormalMode)
    case SelectPurchaserAgentPage =>
      _ => controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
    case PurchaserAgentNamePage =>
      _ => controllers.purchaserAgent.routes.PurchaserAgentAddressController.redirectToAddressLookupPurchaserAgent()
    case PurchaserAgentsContactDetailsPage =>
      _ => controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
    case AddPurchaserAgentReferenceNumberPage =>
      _ => controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(NormalMode)
    case PurchaserAgentReferencePage =>
      _ => controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(NormalMode)
    case AddContactDetailsForPurchaserAgentPage =>
      _ => controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController.onPageLoad(NormalMode)
    case PurchaserAgentAuthorisedPage =>//TODO: Update link to PA-CYA DTR-1851
      _ => controllers.routes.ReturnTaskListController.onPageLoad()
    case _ => _ => routes.IndexController.onPageLoad()
  }
  
  private val checkRouteMap: Page => UserAnswers => Call = {
    case WhoIsMakingThePurchasePage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case NameOfPurchaserPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case AddPurchaserPhoneNumberPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case EnterPurchaserPhoneNumberPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserAndVendorConnectedPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case IsPurchaserActingAsTrusteePage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case CompanyFormOfIdPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserFormOfIdIndividualPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case ConfirmNameOfThePurchaserPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case DoesPurchaserHaveNIPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserConfirmIdentityPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserUTRPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserDateOfBirthPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserNationalInsurancePage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case PurchaserTypeOfCompanyPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
    case RegistrationNumberPage => _ => controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()

    case VendorOrCompanyNamePage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case AgentNamePage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case WhoIsTheVendorPage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case VendorRepresentedByAgentPage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()

    case PurchaserIsIndividualPage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case PurchaserSurnameOrCompanyNamePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()


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
