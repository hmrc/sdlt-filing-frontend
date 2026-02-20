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

import base.SpecBase
import controllers.routes
import models.*
import pages.*
import pages.land.*
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage, TransactionTypePage}
import pages.purchaser.*
import pages.purchaserAgent.*
import pages.vendor.*
import pages.vendorAgent.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val userAnswers = UserAnswers("id", storn = "TESTSTORN")

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {
      "must go from a page that doesn't exist in the route map to Index" in {
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }

      "prelim routes" - {

        "go from purchaser is individual page to purchaser surname or company name" in {
          navigator.nextPage(PurchaserIsIndividualPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(mode = NormalMode)
        }

        "go from purchaser surname or company name page to address look up" in {
          navigator.nextPage(PurchaserSurnameOrCompanyNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.PrelimAddressController.redirectToAddressLookup()
        }

        "go from transaction type page to check your answers" in {
          navigator.nextPage(TransactionTypePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "vendor routes" - {

        "go from WhoIsTheVendor page to Vendor or Company name" in {
          navigator.nextPage(WhoIsTheVendorPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(mode = NormalMode)
        }

        "go from Vendor or Company name page to Confirm Vendor Address Page" in {
          navigator.nextPage(VendorOrCompanyNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.ConfirmVendorAddressController.onPageLoad(mode = NormalMode)
        }

        "go from ConfirmVendorAddressPage to VendorCheckYourAnswers page" in {
          navigator.nextPage(ConfirmVendorAddressPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
        }

        "go from Vendor Agent Before You Start page to agent name page" in {
          navigator.nextPage(VendorAgentBeforeYouStartPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.AgentNameController.onPageLoad(mode = NormalMode)
        }

        "go from Agent name page to agent address lookup" in {
          navigator.nextPage(AgentNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
        }

        "go from VendorAgentsContactDetailsPage to return task list controller" in {
          navigator.nextPage(VendorAgentsContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onPageLoad(mode = NormalMode)
        }

        "go from AddVendorAgentContactDetailsPage to Vendor Agents Contact Detail page" in {
          navigator.nextPage(AddVendorAgentContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(mode = NormalMode)
        }

        "go from VendorAgentsAddReference Page to Agent Reference page" in {
          navigator.nextPage(VendorAgentsAddReferencePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(NormalMode)
        }

        "go from Agent Reference page to CYA page" in {
          navigator.nextPage(VendorAgentsReferencePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        }
      }

      "purchaser routes" - {
        "go from WhoIsMakingThePurchasePage to NameOfPurchaser page" in {
          navigator.nextPage(WhoIsMakingThePurchasePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode = NormalMode)
        }

        "go from NameOfPurchaserPage to address lookup" in {
          navigator.nextPage(NameOfPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
        }

        "go from DoesPurchaserHaveNIPage to PurchaserNationalInsurancePage" in {
          navigator.nextPage(DoesPurchaserHaveNIPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(mode = NormalMode)
        }

        "go from PurchaserNationalInsurancePage to PurchaserDateOfBirthPage" in {
          navigator.nextPage(PurchaserNationalInsurancePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserDateOfBirthController.onPageLoad(mode = NormalMode)
        }

        "go from PurchaserDateOfBirthPage to IsPurchaserActingAsTrusteeController" in {
          navigator.nextPage(PurchaserDateOfBirthPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(mode = NormalMode)
        }

        "go from PurchaserFormOfIdIndividualPage to ReturnTaskList" in {
          navigator.nextPage(PurchaserFormOfIdIndividualPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
        }

        "go from addPhoneNumberPage to enterPhoneNumberPage" in {
          navigator.nextPage(NameOfPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
        }

        "go from PurchaserCorporationTaxUTRPage to PurchaserType Page" in {
          navigator.nextPage(PurchaserUTRPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
        }

        "go from PurchaserTypePage to ReturnTaskList" in {
          navigator.nextPage(PurchaserTypeOfCompanyPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
        }

        "go from CompanyFormOfIdPage to PurchaserType page" in {
          navigator.nextPage(CompanyFormOfIdPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
        }

        "go from PurchaserCorporationTaxUTRPage to PurchaserCheckYourAnswers" in {
          navigator.nextPage(PurchaserAndVendorConnectedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        }

        "go from IsPurchaserActingAsTrusteePage to Return Task List Controller" in {
          navigator.nextPage(IsPurchaserActingAsTrusteePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(NormalMode)
        }

        "go from ConfirmNameOfThePurchaserPage to PurchaserAddress page" in {
          navigator.nextPage(ConfirmNameOfThePurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
        }

        "go from PurchaserAgentBeforeYouStartPage to SelectPurchaserAgentPage" in {
          navigator.nextPage(PurchaserAgentBeforeYouStartPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.SelectPurchaserAgentController.onPageLoad(NormalMode)
        }

        "go from SelectPurchaserAgent page to AddPurchaserAgentReferenceNumber page" in {
          navigator.nextPage(SelectPurchaserAgentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
        }

        "go from PurchaserAgentNamePage to PurchaserAgentAddress page" in {
          navigator.nextPage(PurchaserAgentNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentAddressController.redirectToAddressLookupPurchaserAgent()
        }

        "go from AddContactDetailsForPurchaserAgentPage to PurchaserAgentsContactDetailsPage page" in {
          navigator.nextPage(AddContactDetailsForPurchaserAgentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController.onPageLoad(NormalMode)
        }

        "go from PurchaserAgentsContactDetailsPage to AddPurchaserAgentReferenceNumber page" in {
          navigator.nextPage(PurchaserAgentsContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
        }

        "go from AddPurchaserAgentReferenceNumberPage to PurchaserAgentReference page" in {
          navigator.nextPage(AddPurchaserAgentReferenceNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(NormalMode)
        }

        "go from PurchaserAgentReferencePage to PurchaserAgentAuthorised page" in {
          navigator.nextPage(PurchaserAgentReferencePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(NormalMode)
        }

        "go from PurchaserAgentAuthorisedPage to PurchaserAgentCheckYourAnswers page" in {
          navigator.nextPage(PurchaserAgentAuthorisedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        }

      }

      "land routes" - {

        "go from LandTypeOfPropertyPage to InterestTransferredOrCreated page" in {
          navigator.nextPage(LandTypeOfPropertyPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandInterestTransferredOrCreatedController.onPageLoad(NormalMode)
        }

        "go from LandInterestTransferredOrCreatedPage to ConfirmAddressOfLandOrProperty page" in {
          navigator.nextPage(LandInterestTransferredOrCreatedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(NormalMode)
        }

        "go from LandRegisteredHmRegistryPage to Title number for the land or property page" in {
          navigator.nextPage(LandRegisteredHmRegistryPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandTitleNumberController.onPageLoad(NormalMode)
        }

        "go from LandTitleNumberPage to Do you have an NLPG UPRN for the land or property page" in {
          navigator.nextPage(LandTitleNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandAddNlpgUprnController.onPageLoad(NormalMode)
        }

        "go from LandAddNlpgUprnPage to LandNlpgUprn page" in {
          navigator.nextPage(LandAddNlpgUprnPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandNlpgUprnController.onPageLoad(NormalMode)
        }

        "go from LandNlpgUprnPage to Will You Be Sending Plan by Post page" in {
          navigator.nextPage(LandNlpgUprnPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandSendingPlanByPostController.onPageLoad(NormalMode)
        }

        // TODO DTR-2468: Redirect to Does the transaction involve agricultural or developmental land? (lr-9) page
        "go from LandMineralsOrMineralRightsPage to lr-9 page" in {
          navigator.nextPage(LandMineralsOrMineralRightsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandMineralsOrMineralRightsController.onPageLoad(NormalMode)
        }

        "go from ConfirmLandOrPropertyAddressPage to Local Authority Code page" in {
          navigator.nextPage(ConfirmLandOrPropertyAddressPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LocalAuthorityCodeController.onPageLoad(NormalMode)
        }

        "go from LocalAuthorityCodePage to LandRegisteredHmRegistry page page" in {
          navigator.nextPage(LocalAuthorityCodePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandRegisteredHmRegistryController.onPageLoad(NormalMode)
        }

        "go from LandSelectMeasurementUnitPage to what is the are of land page" in {
          navigator.nextPage(LandSelectMeasurementUnitPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.AreaOfLandController.onPageLoad(NormalMode)
        }

        "go from AreaOfLandPage to Land check your answers page" in { // TODO - DTR-2495 - SPRINT-10 Redirect to Land check your answers
          navigator.nextPage(AreaOfLandPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad()
        }

        "go from LandSendingPlanByPostPage to lr-8 page" in {
          navigator.nextPage(LandSendingPlanByPostPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandMineralsOrMineralRightsController.onPageLoad(NormalMode)
        }

      }
    }

    "in Check mode" - {
      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.routes.ReturnTaskListController.onPageLoad()
      }

      "must go from any purchaser page to CheckYourAnswers" in {
        navigator.nextPage(WhoIsMakingThePurchasePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(NameOfPurchaserPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AddPurchaserPhoneNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(EnterPurchaserPhoneNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAndVendorConnectedPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(IsPurchaserActingAsTrusteePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(CompanyFormOfIdPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserFormOfIdIndividualPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(ConfirmNameOfThePurchaserPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(DoesPurchaserHaveNIPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserConfirmIdentityPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserUTRPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserDateOfBirthPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserNationalInsurancePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserTypeOfCompanyPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        navigator.nextPage(RegistrationNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
      }

      "must go from any vendor page to VendorCheckYourAnswers" in {
        navigator.nextPage(WhoIsTheVendorPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorOrCompanyNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
      }

      "must go from any vendorAgent page to VendorAgentCheckYourAnswers" in {
        navigator.nextPage(AgentNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorAgentAddressPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AddVendorAgentContactDetailsPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorAgentsContactDetailsPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorAgentsAddReferencePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorAgentsReferencePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad()
      }

      "must go from any purchaserAgent page to PurchaserAgentCheckYourAnswers" in {
        navigator.nextPage(SelectPurchaserAgentPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAgentNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAgentAddressPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AddContactDetailsForPurchaserAgentPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAgentsContactDetailsPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AddPurchaserAgentReferenceNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAgentReferencePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserAgentAuthorisedPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad()
      }

      //TODO - DTR-2495 - SPRINT-10 - mopup implement all check routes for Land CYA
      "must go from any land page to LandCheckYourAnswers" in {
        navigator.nextPage(LandTypeOfPropertyPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LandInterestTransferredOrCreatedPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LandRegisteredHmRegistryPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LandTitleNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LandAddNlpgUprnPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LandNlpgUprnPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(ConfirmLandOrPropertyAddressPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
        navigator.nextPage(LocalAuthorityCodePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
      }

      "must go from any preliminary page to CheckYourAnswers" in {
        navigator.nextPage(PurchaserIsIndividualPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserSurnameOrCompanyNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionTypePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "isPurchaserSection helper" - {
      "must return true for purchaser section pages" in {
        val userAnswers = UserAnswers("id", storn = "TESTSTORN")

        navigator.nextPage(WhoIsMakingThePurchasePage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)

        navigator.nextPage(NameOfPurchaserPage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()

        navigator.nextPage(RegistrationNumberPage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
      }

      "must return false for non-purchaser section pages" in {
        val userAnswers = UserAnswers("id", storn = "TESTSTORN")

        navigator.nextPage(WhoIsTheVendorPage, NormalMode, userAnswers) mustBe
          controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode)

        navigator.nextPage(AgentNamePage, NormalMode, userAnswers) mustBe
          controllers.vendorAgent.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
      }
    }
  }
}