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
import pages.lease._
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage, TransactionTypePage}
import pages.purchaser.*
import pages.purchaserAgent.*
import pages.taxCalculation.freeholdSelfAssessed.*
import pages.taxCalculation.freeholdTaxCalculated.*
import pages.taxCalculation.leaseholdSelfAssessed.*
import pages.taxCalculation.leaseholdTaxCalculated.*
import pages.transaction.*
import pages.ukResidency.{CloseCompanyPage, CrownEmploymentReliefPage, NonUkResidentPurchaserPage}
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

        "go from NameOfPurchaserPage to ConfirmPurchaserAddress page" in {
          navigator.nextPage(NameOfPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.ConfirmPurchaserAddressController.onPageLoad(NormalMode)
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

        "go from PurchaserFormOfIdIndividualPage to IsPurchaserActingAsTrustee" in {
          navigator.nextPage(PurchaserFormOfIdIndividualPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
        }

        "go from addPhoneNumberPage to enterPhoneNumberPage" in {
          navigator.nextPage(AddPurchaserPhoneNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(NormalMode)
        }

        "go from EnterPurchaserPhoneNumberPage to DoesPurchaserHaveNI page" in {
          navigator.nextPage(EnterPurchaserPhoneNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode)
        }

        "go from PurchaserCorporationTaxUTRPage to PurchaserType Page" in {
          navigator.nextPage(PurchaserUTRPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(NormalMode)
        }

        "go from PurchaserTypePage to IsPurchaserActingAsTrustee" in {
          navigator.nextPage(PurchaserTypeOfCompanyPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode)
        }

        "go from CompanyFormOfIdPage to PurchaserType page" in {
          navigator.nextPage(CompanyFormOfIdPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(NormalMode)
        }

        "go from PurchaserAndVendorConnectedPage to PurchaserCheckYourAnswers" in {
          navigator.nextPage(PurchaserAndVendorConnectedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        }

        "go from IsPurchaserActingAsTrusteePage to PurchaserAndVendorConnected" in {
          navigator.nextPage(IsPurchaserActingAsTrusteePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(NormalMode)
        }

        "go from ConfirmNameOfThePurchaserPage to ConfirmPurchaserAddress page" in {
          navigator.nextPage(ConfirmNameOfThePurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.ConfirmPurchaserAddressController.onPageLoad(NormalMode)
        }

        "go from ConfirmPurchaserAddress to AddPurchaserPhoneNumber page" in {
          navigator.nextPage(ConfirmPurchaserAddressPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode)
        }

        "go from RegistrationNumberPage to PurchaserTypeOfCompany page" in {
          navigator.nextPage(RegistrationNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(NormalMode)
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

        "go from ChangePurchaserOnePage to ConfirmChangeOfMainPurchaser page" in {
          navigator.nextPage(ChangePurchaserOnePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.ConfirmChangeOfMainPurchaserController.onPageLoad()
        }

        "go from ConfirmChangeOfMainPurchaser to PurchaserAgentCheckYourAnswers page" in {
          navigator.nextPage(ConfirmChangeOfMainPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        }

        "go from PurchaserCompanyTypeKnownPage to PurchaserTypeOfCompanyController page" in {
          navigator.nextPage(PurchaserCompanyTypeKnownPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode)
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

        "go from LandMineralsOrMineralRightsPage to AgriculturalOrDevelopmentalLand page" in {
          navigator.nextPage(LandMineralsOrMineralRightsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(NormalMode)
        }

        "go from ConfirmLandOrPropertyAddressPage to Local Authority Code page" in {
          navigator.nextPage(ConfirmLandOrPropertyAddressPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LocalAuthorityCodeController.onPageLoad(NormalMode)
        }

        "go from LocalAuthorityCodePage to LandRegisteredHmRegistry page" in {
          navigator.nextPage(LocalAuthorityCodePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandRegisteredHmRegistryController.onPageLoad(NormalMode)
        }

        "go from LandSelectMeasurementUnitPage to what is the area of land page" in {
          navigator.nextPage(LandSelectMeasurementUnitPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.AreaOfLandController.onPageLoad(NormalMode)
        }

        "go from AreaOfLandPage to Land check your answers page" in {
          navigator.nextPage(AreaOfLandPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        }

        "go from LandSendingPlanByPostPage to LandMineralsOrMineralRights page" in {
          navigator.nextPage(LandSendingPlanByPostPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandMineralsOrMineralRightsController.onPageLoad(NormalMode)
        }

        "go from AgriculturalOrDevelopmentalLandPage to DoYouKnowTheAreaOfLand page" in {
          navigator.nextPage(AgriculturalOrDevelopmentalLandPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.DoYouKnowTheAreaOfLandController.onPageLoad(NormalMode)
        }

        "go from DoYouKnowTheAreaOfLandPage to LandSelectMeasurementUnit page" in {
          navigator.nextPage(DoYouKnowTheAreaOfLandPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(NormalMode)
        }
      }

      "residency routes" - {

        "go from NonUkResidentPurchaserPage to CloseCompany page" in {
          navigator.nextPage(NonUkResidentPurchaserPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.CloseCompanyController.onPageLoad(NormalMode)
        }

        "go from CloseCompanyPage to CrownEmploymentRelief" in {
          navigator.nextPage(CloseCompanyPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.CrownEmploymentReliefController.onPageLoad(NormalMode)
        }
        "go from CrownEmploymentReliefPage to UkResidencyCheckYourAnswers page" in {
          navigator.nextPage(CrownEmploymentReliefPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad()
        }
      }

      "transaction routes" - {
        "go from ConfirmTypeOfTransactionPage to TransactionEffectiveDatePage" in {
          navigator.nextPage(ConfirmTypeOfTransactionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(NormalMode)
        }

        "go from TypeOfTransactionPage to Are you sure you want to change the transaction type page" in {
          navigator.nextPage(TypeOfTransactionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.ChangeTypeOfTransactionController.onPageLoad(NormalMode)
        }

        "go from TransactionEffectiveDatePage to contract or conclusion of missives page" in {
          navigator.nextPage(TransactionEffectiveDatePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionAddDateOfContractController.onPageLoad(NormalMode)
        }

        "go from TransactionAddContractDatePage to TransactionDateOfContractPage page" in {
          navigator.nextPage(TransactionAddDateOfContractPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(NormalMode)
        }

        "go from TransactionDateOfContractPage to total consideration of transaction page" in {
          navigator.nextPage(TransactionDateOfContractPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TotalConsiderationOfTransactionController.onPageLoad(NormalMode)
        }

        "go from ChangeTypeTransactionTypePage to TransactionEffectiveDatePage" in {
          navigator.nextPage(ChangeTypeOfTransactionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(NormalMode)
        }

        "go from TransactionVatIncludedPage to tr-5a Amount of VAT page" in {
          navigator.nextPage(TransactionVatIncludedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionVatAmountController.onPageLoad(NormalMode)
        }

        "go from TransactionVatAmountPage to Forms of consideration page" in {
          navigator.nextPage(TransactionVatAmountPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionFormsOfConsiderationController.onPageLoad(NormalMode)
        }

        "go from TransactionFormsOfConsiderationPage to Is this transaction linked to another page (tr-7)" in {
          navigator.nextPage(TransactionFormsOfConsiderationPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionLinkedTransactionsController.onPageLoad(NormalMode)
        }

        "go from ReasonForReliefPage to TransactionPartialReliefPage" in {
          navigator.nextPage(ReasonForReliefPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(NormalMode)
        }

        "go from AddRegisteredCharityNumberPage to tr-8c to capture charity register number" in {
          navigator.nextPage(AddRegisteredCharityNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.CharityRegisteredNumberController.onPageLoad(NormalMode)
        }

        "go from TransactionLinkedTransactionsPage to tr-7a total consideration of all linked transactions" in {
          navigator.nextPage(TransactionLinkedTransactionsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TotalConsiderationOfLinkedTransactionController.onPageLoad(NormalMode)
        }

        "go from TotalConsiderationOfLinkedTransactionPage to PurchaserEligibleToClaimReliefPage" in {
          navigator.nextPage(TotalConsiderationOfLinkedTransactionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.PurchaserEligibleToClaimReliefController.onPageLoad(NormalMode)
        }

        "go from TotalConsiderationOfTransactionPage to TransactionVatIncludedController" in {
          navigator.nextPage(TotalConsiderationOfTransactionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionVatIncludedController.onPageLoad(NormalMode)
        }

        "go from PurchaserEligibleToClaimReliefPage to ReasonForReliefPage" in {
          navigator.nextPage(PurchaserEligibleToClaimReliefPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.ReasonForReliefController.onPageLoad(NormalMode)
        }

        "go from TransactionPartialReliefPage to ClaimingPartialReliefAmountPage" in {
          navigator.nextPage(TransactionPartialReliefPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.ClaimingPartialReliefAmountController.onPageLoad(NormalMode)
        }

        "go from ClaimingPartialReliefAmountPage to ConsiderationsAffectedUncertainPage" in {
          navigator.nextPage(ClaimingPartialReliefAmountPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.ConsiderationsAffectedUncertainController.onPageLoad(NormalMode)
        }

        "go from CharityRegisteredNumberPage to purchaser claiming relief part of the land page" in {
          navigator.nextPage(CharityRegisteredNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(NormalMode)
        }
        
        "go from TransactionCisNumberPage to TransactionPartialReliefPage" in {
          navigator.nextPage(TransactionCisNumberPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(NormalMode)
        }

        "go from TransactionDeferringPaymentPage to transaction sale of a business page" in {
          navigator.nextPage(TransactionDeferringPaymentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.SaleOfBusinessController.onPageLoad(NormalMode)
        }

        "go from TransactionUseOfLandOrPropertyPage to transaction part of the sale of a business page" in {
          navigator.nextPage(TransactionUseOfLandOrPropertyPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.SaleOfBusinessController.onPageLoad(NormalMode)
        }

        "go from ConsiderationsAffectedUncertainPage to TransactionDeferringPaymentPage" in {
          navigator.nextPage(ConsiderationsAffectedUncertainPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionDeferringPaymentController.onPageLoad(NormalMode)
        }

        "go from TransactionRulingFollowedPage to restrictions covenants or conditions page" in {
          navigator.nextPage(TransactionRulingFollowedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionRestrictionsCovenantsAndConditionsController.onPageLoad(NormalMode)
        }

        "go from SaleOfBusinessPage to What is included in the sale page" in {
          navigator.nextPage(SaleOfBusinessPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionSaleOfBusinessAssetsController.onPageLoad(NormalMode)
        }

        "go from TransactionSaleOfBusinessAssetsPage to TotalAssetsConsiderationPage" in {
          navigator.nextPage(TransactionSaleOfBusinessAssetsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TotalAssetsConsiderationController.onPageLoad(NormalMode)
        }

        "go from TransactionRestrictionsCovenantsAndConditionsPage to DescriptionOfRestrictionsCovenantsAndConditionsPage" in {
          navigator.nextPage(TransactionRestrictionsCovenantsAndConditionsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.DescriptionOfRestrictionsController.onPageLoad(NormalMode)
        }

        "go from Cap1OrNsbcPage to TransactionRulingFollowedPage" in {
          navigator.nextPage(Cap1OrNsbcPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionRulingFollowedController.onPageLoad(NormalMode)
        }

        "go from IsLandOrPropertyExchangedPage to exchange or part exchange Address Lookup integration" in {
          navigator.nextPage(IsLandOrPropertyExchangedPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionAddressController.redirectToAddressLookupTransaction()
        }

        "go from TotalAssetsConsiderationPage to CAP1-or-NSBC page" in {
          navigator.nextPage(TotalAssetsConsiderationPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.Cap1OrNsbcController.onPageLoad(NormalMode)
        }

        "go from IsPurchaserRegisteredWithCISPage to TransactionCisNumberPage" in {
          navigator.nextPage(IsPurchaserRegisteredWithCISPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCisNumberController.onPageLoad(NormalMode)
        }

        "go from DescriptionOfRestrictionsPage to IsLandOrPropertyExchangedPage" in {
          navigator.nextPage(DescriptionOfRestrictionsPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.IsLandOrPropertyExchangedController.onPageLoad(NormalMode)
        }

        "go from TransactionExercisingAnOptionPage to CYA page" in {
          navigator.nextPage(TransactionExercisingAnOptionPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        }
      }

      "lease routes" - {

        "go from TypeOfLeasePage to ls-2" in { //TODO - DTR-3506 - SPRINT 15 - update to what is the start date ls-2
          navigator.nextPage(TypeOfLeasePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.lease.routes.TypeOfLeaseController.onPageLoad(NormalMode)
        }

        "go from LeaseEnterRentFreePeriodPage to AnnualStartingRent page" in { //TODO: - DTR-3518 - SPRINT 15 - update to What is the annual starting rent including VAT? - ls-5
          navigator.nextPage(LeaseEnterRentFreePeriodPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.lease.routes.LeaseEnterRentFreePeriodController.onPageLoad(NormalMode)
        }

        "go from LaterRentPage to 1000PoundThresholdPage" in { // TODO DTR-3524: Redirect to is the annual rent 1000 pounds or more ls-8
          navigator.nextPage(LaterRentPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.lease.routes.LaterRentController.onPageLoad(NormalMode)
        }
      }

      "freehold tax calculated routes" - {

        "go from FreeholdTaxCalculatedSdltPage to Index page" in {
          navigator.nextPage(FreeholdTaxCalculatedSdltPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdTaxCalculatedSelfAssessedAmountPage to Index page" in {
          navigator.nextPage(FreeholdTaxCalculatedSelfAssessedAmountPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdTaxCalculatedTotalAmountDuePage to Index page" in {
          navigator.nextPage(FreeholdTaxCalculatedTotalAmountDuePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdTaxCalculatedPenaltiesAndInterestPage to Index page" in {
          navigator.nextPage(FreeholdTaxCalculatedPenaltiesAndInterestPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }
      }

      "freehold self-assessed routes" - {

        "go from FreeholdSelfAssessedCannotCalculateTaxPage to Index page" in {
          navigator.nextPage(FreeholdSelfAssessedCannotCalculateTaxPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdSelfAssessedAmountPage to Index page" in {
          navigator.nextPage(FreeholdSelfAssessedAmountPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdSelfAssessedTotalAmountDuePage to Index page" in {
          navigator.nextPage(FreeholdSelfAssessedTotalAmountDuePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from FreeholdSelfAssessedPenaltiesAndInterestPage to Index page" in {
          navigator.nextPage(FreeholdSelfAssessedPenaltiesAndInterestPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }
      }

      "leasehold tax calculated routes" - {

        "go from LeaseholdTaxCalculatedSdltPage to Index page" in {
          navigator.nextPage(LeaseholdTaxCalculatedSdltPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from LeaseholdTaxCalculatedSelfAssessedAmountPage to Index page" in {
          navigator.nextPage(LeaseholdTaxCalculatedSelfAssessedAmountPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from LeaseholdTaxCalculatedTotalAmountDuePage to Index page" in {
          navigator.nextPage(LeaseholdTaxCalculatedTotalAmountDuePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from LeaseholdTaxCalculatedPenaltiesAndInterestPage to Index page" in {
          navigator.nextPage(LeaseholdTaxCalculatedPenaltiesAndInterestPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }
      }

      "leasehold self-assessed routes" - {

        "go from LeaseholdSelfAssessedPremiumPayableTaxPage to LeaseholdSelfAssessedNpvTaxPage page" in {
          navigator.nextPage(LeaseholdSelfAssessedPremiumPayableTaxPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe
            controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedTaxDueOnNpvController.onPageLoad(NormalMode)
        }

        "go from LeaseholdSelfAssessedNpvTaxPage to LeaseholdSelfAssessedTotalAmountDuePage page" in {
          navigator.nextPage(LeaseholdSelfAssessedNpvTaxPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe
            controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedTotalAmountDueController.onPageLoad(NormalMode)
        }

        "go from LeaseholdSelfAssessedTotalAmountDuePage to Index page" in {
          navigator.nextPage(LeaseholdSelfAssessedTotalAmountDuePage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }

        "go from LeaseholdSelfAssessedPenaltiesAndInterestPage to Index page" in {
          navigator.nextPage(LeaseholdSelfAssessedPenaltiesAndInterestPage, NormalMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to ReturnTaskList" in {
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.routes.ReturnTaskListController.onPageLoad()
      }

      "must go from any purchaser page to PurchaserCheckYourAnswers" in {
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
        navigator.nextPage(PurchaserCompanyTypeKnownPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
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

      "must go from any land page to LandCheckYourAnswers" in {
        navigator.nextPage(LandTypeOfPropertyPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandInterestTransferredOrCreatedPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandRegisteredHmRegistryPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandTitleNumberPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandAddNlpgUprnPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandNlpgUprnPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(ConfirmLandOrPropertyAddressPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LocalAuthorityCodePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(DoYouKnowTheAreaOfLandPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AgriculturalOrDevelopmentalLandPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AreaOfLandPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandSendingPlanByPostPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandMineralsOrMineralRightsPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.LandCheckYourAnswersController.onPageLoad()
        navigator.nextPage(LandSelectMeasurementUnitPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.land.routes.AreaOfLandController.onPageLoad(CheckMode)
      }

      "must go from transaction pages to TransactionCheckYourAnswers in CheckMode" in {
        navigator.nextPage(TypeOfTransactionPage,                     CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(ConfirmTypeOfTransactionPage,              CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionEffectiveDatePage,              CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionAddDateOfContractPage,          CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionDateOfContractPage,             CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TotalConsiderationOfTransactionPage,       CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionVatIncludedPage,                CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionVatAmountPage,                  CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionFormsOfConsiderationPage,       CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionLinkedTransactionsPage,         CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TotalConsiderationOfLinkedTransactionPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserEligibleToClaimReliefPage,        CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(ReasonForReliefPage,                       CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(AddRegisteredCharityNumberPage,            CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(CharityRegisteredNumberPage,               CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionPartialReliefPage,              CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
        navigator.nextPage(ClaimingPartialReliefAmountPage,           CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad()
      }

      "must go from NonUkResidentPurchaserPage to UkResidencyCheckYourAnswers in CheckMode" in {
        navigator.nextPage(NonUkResidentPurchaserPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad()
      }

      "must go from CloseCompanyPage to UkResidencyCheckYourAnswers in CheckMode" in {
        navigator.nextPage(CloseCompanyPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad()
      }

      "must go from CrownEmploymentReliefPage to UkResidencyCheckYourAnswers in CheckMode" in {
        navigator.nextPage(CrownEmploymentReliefPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad()
      }

      "must go from any preliminary page to CheckYourAnswers" in {
        navigator.nextPage(PurchaserIsIndividualPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(PurchaserSurnameOrCompanyNamePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
        navigator.nextPage(TransactionTypePage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe controllers.preliminary.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from FreeholdTaxCalculatedPenaltiesAndInterestPage to Index in CheckMode" in {
        navigator.nextPage(FreeholdTaxCalculatedPenaltiesAndInterestPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from FreeholdSelfAssessedPenaltiesAndInterestPage to Index in CheckMode" in {
        navigator.nextPage(FreeholdSelfAssessedPenaltiesAndInterestPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from LeaseholdSelfAssessedPenaltiesAndInterestPage to Index in CheckMode" in {
        navigator.nextPage(LeaseholdSelfAssessedPenaltiesAndInterestPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from LeaseholdTaxCalculatedPenaltiesAndInterestPage to Index in CheckMode" in {
        navigator.nextPage(LeaseholdTaxCalculatedPenaltiesAndInterestPage, CheckMode, UserAnswers("id", storn = "TESTSTORN")) mustBe routes.IndexController.onPageLoad()
      }
    }

    "isPurchaserSection helper" - {
      "must return true for purchaser section pages" in {
        val userAnswers = UserAnswers("id", storn = "TESTSTORN")

        navigator.nextPage(WhoIsMakingThePurchasePage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)

        navigator.nextPage(NameOfPurchaserPage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.ConfirmPurchaserAddressController.onPageLoad(NormalMode)

        navigator.nextPage(RegistrationNumberPage, NormalMode, userAnswers) mustBe
          controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(NormalMode)
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