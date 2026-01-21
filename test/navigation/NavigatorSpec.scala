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
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage, TransactionTypePage}
import pages.purchaser.*
import pages.purchaserAgent.*
import pages.vendor.*

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

        "go from ConfirmVendorAddressPage to VendorRepresentedByAgent page" in {
          navigator.nextPage(ConfirmVendorAddressPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(mode = NormalMode)
        }

        "go from VendorRepresentedByAgentPage to Agent Name page" in {
          navigator.nextPage(VendorRepresentedByAgentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.AgentNameController.onPageLoad(mode = NormalMode)
        }

        "go from Agent name page to agent address lookup" in {
          navigator.nextPage(AgentNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
        }

        "go from VendorAgentsContactDetailsPage to return task list controller" in {
          navigator.nextPage(VendorAgentsContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(mode = NormalMode)
        }

        "go from AddVendorAgentContactDetailsPage to Vendor Agents Contact Detail page" in {
          navigator.nextPage(AddVendorAgentContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorAgentsContactDetailsController.onPageLoad(mode = NormalMode)
        }

        "go from DoYouKnowYourAgentReference Page to Agent Reference page" in {
          navigator.nextPage(DoYouKnowYourAgentReferencePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorAgentsReferenceController.onPageLoad(NormalMode)
        }

        // TODO: Redirect should change to AgentCYA when created - DTR-2057
        "go from Agent Reference page to CYA page" in {
          navigator.nextPage(VendorAgentsReferencePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
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

        "go from PurchaserCorporationTaxUTRPage to purchaserandvendorconnected" in { //TODO: update to Purchaser CYA - DTR-1788
          navigator.nextPage(PurchaserAndVendorConnectedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.routes.ReturnTaskListController.onPageLoad()
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

        "go from PurchaserAgentsContactDetailsPage to AddPurchaserReferenceNumber page" in {
          navigator.nextPage(PurchaserAgentsContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
        }

        //TODO DTR-1829 - link up route
        "go from AddPurchaserReferenceNumber to EnterPurchaserReferenceNumber page" in {
          navigator.nextPage(AddPurchaserAgentReferenceNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
          //          navigator.nextPage(AddPurchaserAgentReferenceNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaserAgent.routes.EnterPurchaserAgentReferenceNumberController.onPageLoad(NormalMode)
        }

        "go from PurchaserAgentAuthorisedPage to ReturnTakList page" in {//TODO: Update link to PA-CYA DTR-1851
          navigator.nextPage(AddPurchaserAgentReferenceNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.ReturnTaskListController.onPageLoad()
         }

      }
    }

    "in Check mode" - {
      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.routes.ReturnTaskListController.onPageLoad()
      }

      "must go from any purchaser page to CheckYourAnswers" in {
        navigator.nextPage(WhoIsMakingThePurchasePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(NameOfPurchaserPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserFormOfIdIndividualPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from any vendor page to VendorCheckYourAnswers" in {
        navigator.nextPage(WhoIsTheVendorPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorOrCompanyNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
        navigator.nextPage(VendorRepresentedByAgentPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AgentNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()
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
          controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
      }
    }
  }
}