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
import pages.vendorAgent.*
import pages.land.*
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
      _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
    case AgentNamePage =>
      _ => controllers.vendorAgent.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
    case AddVendorAgentContactDetailsPage =>
      _ => controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(NormalMode)
    case VendorAgentsContactDetailsPage =>
      _ => controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onPageLoad(NormalMode)
    case VendorAgentsAddReferencePage =>
      _ => controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(NormalMode)
    case VendorAgentsReferencePage =>
      _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case VendorAgentBeforeYouStartPage =>
      _ => controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode)

    case purchaserPage if isPurchaserSection(purchaserPage) => purchaserRoutes(purchaserPage)
    case landPage if isLandSection(landPage) => landRoutes(landPage)
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
    case PurchaserAgentAuthorisedPage =>
      _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def isLandSection(page: Page): Boolean = page match {

    case LandTypeOfPropertyPage | LandInterestTransferredOrCreatedPage | LandRegisteredHmRegistryPage
         | LandAddNlpgUprnPage | LandTitleNumberPage | ConfirmLandOrPropertyAddressPage | LocalAuthorityCodePage | LandNlpgUprnPage
         | LandMineralsOrMineralRightsPage => true

    case _ => false
  }

  private def landRoutes(page: Page): UserAnswers => Call = page match {
    case LandTypeOfPropertyPage =>
      _ => controllers.land.routes.LandInterestTransferredOrCreatedController.onPageLoad(NormalMode)
    case LandInterestTransferredOrCreatedPage =>
      _ => controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(NormalMode)
    case LandRegisteredHmRegistryPage =>
      _ => controllers.land.routes.LandTitleNumberController.onPageLoad(NormalMode)
    case LandTitleNumberPage =>
      _ => controllers.land.routes.LandAddNlpgUprnController.onPageLoad(NormalMode)
    case LandAddNlpgUprnPage =>
      _ => controllers.land.routes.LandNlpgUprnController.onPageLoad(NormalMode)
    case LandNlpgUprnPage =>
      _ => controllers.land.routes.LandBeforeYouStartController.onPageLoad() // TODO - DTR-2462 - SPRINT-9 Redirect to Will you be sending plan by post page
    case ConfirmLandOrPropertyAddressPage =>
      _ => controllers.land.routes.LocalAuthorityCodeController.onPageLoad(NormalMode)
    case LocalAuthorityCodePage =>
      _ => controllers.land.routes.LandRegisteredHmRegistryController.onPageLoad(NormalMode)
    case LandMineralsOrMineralRightsPage =>
      _ => controllers.land.routes.LandMineralsOrMineralRightsController.onPageLoad(NormalMode) // TODO DTR-2468: Redirect to Does the transaction involve agricultural or developmental land? (lr-9) page

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
    case WhoIsTheVendorPage => _ => controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()

    case AgentNamePage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case VendorAgentAddressPage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case AddVendorAgentContactDetailsPage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case VendorAgentsContactDetailsPage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case VendorAgentsAddReferencePage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
    case VendorAgentsReferencePage => _ => controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()

    case PurchaserIsIndividualPage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case PurchaserSurnameOrCompanyNamePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    case TransactionTypePage => _ => controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()

    case SelectPurchaserAgentPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case PurchaserAgentNamePage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case PurchaserAgentAddressPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case AddContactDetailsForPurchaserAgentPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case PurchaserAgentsContactDetailsPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case AddPurchaserAgentReferenceNumberPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case PurchaserAgentReferencePage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
    case PurchaserAgentAuthorisedPage => _ => controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()

    //TODO - DTR-2495 - SPRINT-19 - Add land check route here to be uncommented in CYA task
    //    case LandTypeOfPropertyPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LandInterestTransferredOrCreatedPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LandRegisteredHmRegistryPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LandTitleNumberPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LandAddNlpgUprnPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LandNlpgUprnPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case ConfirmLandOrPropertyAddressPage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
    //    case LocalAuthorityCodePage => _ => controllers.land.routes.LandCheckYourAnswersController.onPageLoad()

    case _ => _ => controllers.routes.ReturnTaskListController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
