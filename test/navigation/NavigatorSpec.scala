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
import pages.*
import models.*
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage, TransactionTypePage}
import pages.purchaser.{ConfirmNameOfThePurchaserPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import pages.vendor.{AddVendorAgentContactDetailsPage, AgentNamePage, ConfirmVendorAddressPage, VendorOrCompanyNamePage, VendorRepresentedByAgentPage, WhoIsTheVendorPage}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }

      "vendor routes" - {

        "go from agent name page to agent address lookup" in {
          navigator.nextPage(AgentNamePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent()
        }

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
      }
      "go from VendorRepresentedByAgentPage to Agent Name page" in {
        navigator.nextPage(VendorRepresentedByAgentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.vendor.routes.AgentNameController.onPageLoad(mode = NormalMode)
      }

      "go from AddVendorAgentContactDetailsPage to Vendor Agents Contact Detail page" in {
        navigator.nextPage(AddVendorAgentContactDetailsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.routes.ReturnTaskListController.onPageLoad()
      }

    }

    "purchaser routes" - {

      "go from WhoIsMakingThePurchasePage to NameOfPurchaser page" in {
        navigator.nextPage(WhoIsMakingThePurchasePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode = NormalMode)
      }

      "go from NameOfPurchaserPage to address lookup" in {
        navigator.nextPage(NameOfPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()
      }
    }
  }

  "in Check mode" - {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from any purchaser page to CheckYourAnswers" in {
      navigator.nextPage(WhoIsMakingThePurchasePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      navigator.nextPage(NameOfPurchaserPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
    }

    "must go from any vendor page to CheckYourAnswers" in {
      navigator.nextPage(WhoIsTheVendorPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      navigator.nextPage(VendorOrCompanyNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      navigator.nextPage(VendorRepresentedByAgentPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      navigator.nextPage(AgentNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
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