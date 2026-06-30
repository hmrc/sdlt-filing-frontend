/*
 * Copyright 2026 HM Revenue & Customs
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

package services.pdf

/**
 * All AcroForm field names present in the SDLT1 PDF template.
 *
 * Field names were extracted directly from the template PDF using PyMuPDF.
 * Grouped by page and logical section to match the form layout.
 *
 * Naming convention in the template:
 *   - Text fields:    plain string values
 *   - CheckBox:       "Yes" to check, "Off" to uncheck
 *   - Date fields:    split into _day / _month / _year suffixes
 *   - Money fields:   some are single wide fields, some are split digit groups (_1/_2/_3/_4)
 */
object SdltPdfFields {

  // =========================================================================
  // SHARED (appears on every page)
  // =========================================================================
  val UTRN         = "UTRN"
  val PRINT_STATUS = "PRINT_STATUS"
  val IR_MARK      = "irMark"

  // =========================================================================
  // PAGE 1 — About the Transaction (Boxes 1-8)
  // =========================================================================

  // Box 1: Type of property
  val LAND_TYPE_PROPERTY = "land_typeProperty"

  // Box 2: Description of transaction
  val TRANSACTION_DESCRIPTION = "transaction_description"

  // Box 3: Interest/estate transferred
  val LAND_ESTATE_OR_INTEREST_TRANSFERRED = "land_estateOrInterestTransfered"

  // Box 4: Effective date (split)
  val TRANSACTION_EFFECTIVE_DATE_DAY   = "transaction_effectiveDate_day"
  val TRANSACTION_EFFECTIVE_DATE_MONTH = "transaction_effectiveDate_month"
  val TRANSACTION_EFFECTIVE_DATE_YEAR  = "transaction_effectiveDate_year"

  // Box 5: Restrictions/covenants
  val TRANSACTION_RESTRICTIONS_YES     = "transaction_restrictionsAffecting_yes"
  val TRANSACTION_RESTRICTIONS_NO      = "transaction_restrictionsAffecting_no"
  val TRANSACTION_RESTRICTIONS_DETAILS_1 = "transaction_restrictionsAffectingDetails_1"
  val TRANSACTION_RESTRICTIONS_DETAILS_2 = "transaction_restrictionsAffectingDetails_2"

  // Box 6: Date of contract (split)
  val TRANSACTION_CONTRACT_DATE_DAY    = "transaction_contractDate_day"
  val TRANSACTION_CONTRACT_DATE_MONTH  = "transaction_contractDate_month"
  val TRANSACTION_CONTRACT_DATE_YEAR   = "transaction_contractDate_year"

  // Box 7: Land exchanged
  val TRANSACTION_LAND_EXCHANGED_YES          = "transaction_landExchanged_yes"
  val TRANSACTION_LAND_EXCHANGED_NO           = "transaction_landExchanged_no"
  val TRANSACTION_LAND_EXCHANGED_POSTCODE_1   = "transaction_landExchangedPostcode_1"
  val TRANSACTION_LAND_EXCHANGED_POSTCODE_2   = "transaction_landExchangedPostcode_2"
  val TRANSACTION_LAND_EXCHANGED_HOUSE_NUMBER = "transaction_landExchangedHouseNumber"
  val TRANSACTION_LAND_EXCHANGED_ADDRESS_1    = "transaction_landExchangedAddressLine1"
  val TRANSACTION_LAND_EXCHANGED_ADDRESS_2    = "transaction_landExchangedAddressLine2"
  val TRANSACTION_LAND_EXCHANGED_ADDRESS_3    = "transaction_landExchangedAddressLine3"
  val TRANSACTION_LAND_EXCHANGED_ADDRESS_4    = "transaction_landExchangedAddressLine4"

  // Box 8: Pursuant to option
  val TRANSACTION_PURSUANT_TO_OPTION_YES = "transaction_pursuantToOption_yes"
  val TRANSACTION_PURSUANT_TO_OPTION_NO  = "transaction_pursuantToOption_no"

  // =========================================================================
  // PAGE 2 — About the Tax Calculation (Boxes 9-15) + New Leases (16-25)
  // =========================================================================

  // Box 9: Relief claimed
  val CALCULATION_CLAIMING_RELIEF_YES           = "calculation_claimingRelief_yes"
  val CALCULATION_CLAIMING_RELIEF_NO            = "calculation_claimingRelief_no"
  val CALCULATION_CLAIMING_RELIEF_REASON        = "calculation_claimingReliefReason"
  val CALCULATION_CLAIMING_RELIEF_SCHEME_NUMBER = "calculation_claimingReliefSchemeNumber"
  val CALCULATION_CLAIMING_RELIEF_AMOUNT        = "calculation_claimingReliefAmount"

  // Box 10: Total consideration including VAT
  val CALCULATION_TOTAL_CONSIDERATION = "calculation_totalConsideration"

  // Box 11: VAT in consideration
  val CALCULATION_TOTAL_CONSIDERATION_VAT = "calculation_totalConsiderationVatAmount"

  // Box 12: Forms of consideration
  val CALCULATION_FORM_OF_CONSIDERATION_1 = "calculation_totalConsideration_1"
  val CALCULATION_FORM_OF_CONSIDERATION_2 = "calculation_totalConsideration_2"
  val CALCULATION_FORM_OF_CONSIDERATION_3 = "calculation_totalConsideration_3"
  val CALCULATION_FORM_OF_CONSIDERATION_4 = "calculation_totalConsideration_4"

  // Box 13: Linked transactions
  val CALCULATION_LINKED_TRANSACTION_YES   = "calculation_linkedTransaction_yes"
  val CALCULATION_LINKED_TRANSACTION_NO    = "calculation_linkedTransaction_no"
  val CALCULATION_LINKED_TRANSACTION_TOTAL = "calculation_linkedTransactionTotalConsideration"

  // Box 14: Tax due
  val CALCULATION_TAX_DUE = "calculation_taxDueUserEntered"

  // Box 15: Amount paid
  val CALCULATION_AMOUNT_PAID = "calculation_amountPaid"

  // Does amount include penalties?
  val CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_YES = "calculation_amountPaidIncludesPenalties_yes"
  val CALCULATION_AMOUNT_PAID_INCLUDES_PENALTIES_NO  = "calculation_amountPaidIncludesPenalties_no"

  // Box 16: Lease type
  val LEASE_LEASE_TYPE = "lease_leaseType"

  // Box 17: Lease start date (split)
  val LEASE_CONTRACT_START_DATE_DAY   = "lease_contractStartDate_day"
  val LEASE_CONTRACT_START_DATE_MONTH = "lease_contractStartDate_month"
  val LEASE_CONTRACT_START_DATE_YEAR  = "lease_contractStartDate_year"

  // Box 18: Lease end date (split)
  val LEASE_CONTRACT_END_DATE_DAY   = "lease_contractEndDate_day"
  val LEASE_CONTRACT_END_DATE_MONTH = "lease_contractEndDate_month"
  val LEASE_CONTRACT_END_DATE_YEAR  = "lease_contractEndDate_year"

  // Box 19: Rent-free period
  val LEASE_RENT_FREE_PERIOD = "lease_rentFreePeriod"

  // Box 20: Annual starting rent
  val LEASE_STARTING_RENT = "lease_startingRent"

  val LEASE_STARTING_RENT_END_DATE_DAY   = "lease_startingRentEndDate_day"
  val LEASE_STARTING_RENT_END_DATE_MONTH = "lease_startingRentEndDate_month"
  val LEASE_STARTING_RENT_END_DATE_YEAR  = "lease_startingRentEndDate_year"

  val LEASE_STARTING_RENT_LATER_KNOWN_YES = "lease_startingRentLaterKnown_yes"
  val LEASE_STARTING_RENT_LATER_KNOWN_NO  = "lease_startingRentLaterKnown_no"

  // Box 21: VAT on lease
  val LEASE_VAT_AMOUNT = "lease_vatAmount"

  // Box 22: Total premium payable
  val LEASE_PREMIUM_PAID = "lease_premiumPaid"

  // Box 23: Net present value
  val LEASE_NET_PRESENT_VALUE = "lease_netPresentValue"

  // Box 24: Tax due — premium
  val LEASE_TOTAL_PREMIUM_TAX = "lease_totalPremiumTax"

  // Box 25: Tax due — NPV
  val LEASE_TOTAL_NPV_TAX = "lease_totalNpvTax"

  // =========================================================================
  // PAGE 3 — About the Land (26-33) + About the Vendor (34-39)
  // =========================================================================

  // Box 26: Number of properties
  val LAND_NUMBER_PROPERTIES = "land_numberProperties"

  // Box 27: Certificate for each property
  val LAND_CERTIFICATE_FOR_EACH_YES = "land_certificateForEach_yes"
  val LAND_CERTIFICATE_FOR_EACH_NO  = "land_certificateForEach_no"

  // Box 28: Land address
  val LAND_POSTCODE_1    = "land_postcode_1"
  val LAND_POSTCODE_2    = "land_postcode_2"
  val LAND_HOUSE_NUMBER  = "land_houseNumber"
  val LAND_ADDRESS_LINE1 = "land_addressLine1"
  val LAND_ADDRESS_LINE2 = "land_addressLine2"
  val LAND_ADDRESS_LINE3 = "land_addressLine3"
  val LAND_ADDRESS_LINE4 = "land_addressLine4"

  // Box 29: Is Address continued on SDLT3
  val LAND_ADDRESS_ON_SDLT3_NO  = "restOfAddressOnSupplementaryReturn"

  // Box 30: Local authority number
  val LAND_LOCAL_AUTHORITY_NUMBER = "land_localAuthorityNumber"

  // Box 31: Title number
  val LAND_TITLE_NUMBER = "land_titleNumber"

  // Box 32: NLPG UPRN
  val LAND_NLPG_UPRN = "land_nlpgUprn"

  // Box 33: Land area
  val LAND_AREA_TYPE_HECTARES      = "land_landAreaType_Hectares"
  val LAND_AREA_TYPE_SQUARE_METRES = "land_landAreaType_Square_Metres"
  val LAND_AREA                    = "land_landArea"
  val LAND_PLAN_ATTACHED_YES       = "land_planAttached_yes"
  val LAND_PLAN_ATTACHED_NO        = "land_planAttached_no"

  // Box 34: Number of vendors
  val VENDOR_NUMBER_VENDORS = "vendor_numberVendors"

  // Box 35: Vendor title
  val VENDOR_TITLE = "vendor_title"

  // Box 36: Vendor surname/company name & Box 46: Additional vendor surname/company name
  val VENDOR_NAME = "vendor_name"

  // Box 37: Vendor first name(s) & Box 47: Additional vendor first name(s)
  val VENDOR_FORENAME_1 = "vendor_forename1"
  val VENDOR_FORENAME_2 = "vendor_forename2"

  // Box 38: Vendor address & Box 48: Vendor address
  val VENDOR_POSTCODE_1    = "vendor_postcode_1"
  val VENDOR_POSTCODE_2    = "vendor_postcode_2"
  val VENDOR_HOUSE_NUMBER  = "vendor_houseNumber"
  val VENDOR_ADDRESS_LINE1 = "vendor_addressLine1"
  val VENDOR_ADDRESS_LINE2 = "vendor_addressLine2"
  val VENDOR_ADDRESS_LINE3 = "vendor_addressLine3"
  val VENDOR_ADDRESS_LINE4 = "vendor_addressLine4"

  // Box 39: Vendor agent name
  val VENDOR_AGENT_NAME = "vendor_agentName"

  // =========================================================================
  // PAGE 4 — About the Vendor (continued) (40-44) + Additional Vendor (45-48)
  // =========================================================================

  // Box 40: Vendor agent address
  val VENDOR_AGENT_POSTCODE_1 = "vendor_agentPostcode_1"
  val VENDOR_AGENT_POSTCODE_2 = "vendor_agentPostcode_2"
  val VENDOR_AGENT_HOUSE_NUMBER = "vendor_agentHouseNumber"
  val VENDOR_AGENT_ADDRESS_LINE1 = "vendor_agentAddressLine1"
  val VENDOR_AGENT_ADDRESS_LINE2 = "vendor_agentAddressLine2"
  val VENDOR_AGENT_ADDRESS_LINE3 = "vendor_agentAddressLine3"
  val VENDOR_AGENT_ADDRESS_LINE4 = "vendor_agentAddressLine4"

  // Box 41: Vendor agent DX number and exchange - TO BE LEFT EMPTY

  // Box 42: Vendor agent email address
  val VENDOR_AGENT_EMAIL_ADDRESS = "vendor_agentEmailAddress"

  // Box 43: Vendor agent email address
  val VENDOR_AGENT_REFERENCE = "vendor_agentReference"

  // Box 44: Vendor agent phone number
  val VENDOR_AGENT_PHONE_NUMBER = "vendor_agentPhoneNumber"

  // =========================================================================
  // PAGE 5 — About the Purchaser (Boxes 49-62)
  // =========================================================================

  // Box 49: National Insurance number (split across five boxes)
  val PURCHASER_NINO_1 = "purchaser_nino_1"
  val PURCHASER_NINO_2 = "purchaser_nino_2"
  val PURCHASER_NINO_3 = "purchaser_nino_3"
  val PURCHASER_NINO_4 = "purchaser_nino_4"
  val PURCHASER_NINO_5 = "purchaser_nino_5"

  // Box 49 sub-field: Date of birth
  val PURCHASER_DOB_DAY   = "purchaser_dob_day"
  val PURCHASER_DOB_MONTH = "purchaser_dob_month"
  val PURCHASER_DOB_YEAR  = "purchaser_dob_year"

  // Box 50: VAT Registration Number
  val PURCHASER_VAT_REFERENCE = "purchaser_vatReference"

  // Box 51: UK company/partnership UTR / foreign tax reference / country of registration
  val PURCHASER_COMPANY_UTR       = "purchaser_companyUtr"
  val PURCHASER_REGISTERED_NUMBER = "purchaser_registeredNumber"
  val PURCHASER_REGISTERED_PLACE  = "purchaser_registeredPlace"

  // Box 52: Number of purchasers included
  val PURCHASER_NUMBER_PURCHASERS = "purchaser_numberPurchasers"

  // Box 53: Title          — reuse PURCHASER_TITLE
  // Box 54: Surname or company name — reuse PURCHASER_COMPANY_NAME (single field for both)
  // Box 55: First name(s)  — reuse PURCHASER_FORENAME_1/2
  // Box 56: Address        — reuse PURCHASER_HOUSE_NUMBER, PURCHASER_ADDRESS_LINE_1..4, PURCHASER_POSTCODE_1/2
  // Box 57: Acting trustee — reuse PURCHASER_ACTING_TRUSTEE_YES/NO
  // Box 59: Connected to vendor — reuse PURCHASER_CONNECTED_TO_VENDOR_YES/NO

  // Box 58: Daytime phone number
  val PURCHASER_DAYTIME_PHONE_NUMBER = "purchaser_daytimePhoneNumber"

  // Box 60: Where to send the certificate
  val LAND_CERTIFICATE_ADDRESS_PROPERTY  = "land_certificateAddress_property"
  val LAND_CERTIFICATE_ADDRESS_PURCHASER = "land_certificateAddress_purchaser"
  val LAND_CERTIFICATE_ADDRESS_AGENT     = "land_certificateAddress_agent"

  // Box 61: Agent authorised to handle correspondence
  val PURCHASER_AGENT_AUTHORISED_YES = "purchaser_agentAuthorised_yes"
  val PURCHASER_AGENT_AUTHORISED_NO  = "purchaser_agentAuthorised_no"

  // Box 62: Agent name
  val PURCHASER_AGENT_NAME = "purchaser_agentName"

  // =========================================================================
  // SDLT2 Purchaser
  // =========================================================================

  // =========================================================================
  // PAGE 1 — About the Purchaser (Boxes 1-5)
  // =========================================================================

  // Box 1: Additional Vendor or Purchaser
  val IS_PURCHASER = "is_purchaser"

  // Box 2: Title
  val PURCHASER_TITLE = "purchaser_title"

  // BOX 3: Surname or Company name
  //val PURCHASER_SURNAME = "purchaser_surname"
  val PURCHASER_COMPANY_NAME = "purchaser_companyName"

  // Box 4: Forename 1 and 2
  val PURCHASER_FORENAME_1 = "purchaser_forename1"
  val PURCHASER_FORENAME_2 = "purchaser_forename2"

  // Box 5: Address
  val PURCHASER_HOUSE_NUMBER = "purchaser_houseNumber"
  val PURCHASER_ADDRESS_LINE_1 = "purchaser_addressLine1"
  val PURCHASER_ADDRESS_LINE_2 = "purchaser_addressLine2"
  val PURCHASER_ADDRESS_LINE_3 = "purchaser_addressLine3"
  val PURCHASER_ADDRESS_LINE_4 = "purchaser_addressLine4"
  val PURCHASER_POSTCODE_1 = "purchaser_postcode_1"
  val PURCHASER_POSTCODE_2 = "purchaser_postcode_2"

  // =========================================================================
  // PAGE 2 — Additional purchaser details (Boxes 6-7)
  // =========================================================================

  // Box 6: Purchaser and vendor connected
  val PURCHASER_CONNECTED_TO_VENDOR_YES = "purchaser_connectedToVendor_yes"
  val PURCHASER_CONNECTED_TO_VENDOR_NO = "purchaser_connectedToVendor_no"

  // Box 7: Purchaser acting as a trustee
  val PURCHASER_ACTING_TRUSTEE_YES = "purchaser_actingTrustee_yes"
  val PURCHASER_ACTING_TRUSTEE_NO = "purchaser_actingTrustee_no"
  
  // =========================================================================
  // SDLT3
  // =========================================================================

  val LAND_AREA_DECIMAL = "land_landArea_decimal"
  val LAND_MINERAL_RIGHTS = "land_mineralRights"

  // =========================================================================
  // SDLT4
  // =========================================================================

  // Box 1: Business sale
  val TRANSACTION_BUSINESS_SALE_STOCK = "transaction_businessSaleStock"
  val TRANSACTION_BUSINESS_SALE_GOODWILL = "transaction_businessSaleGoodwill"
  val TRANSACTION_BUSINESS_SALE_CHATTEL = "transaction_businessSaleChattel"
  val TRANSACTION_BUSINESS_SALE_OTHER = "transaction_businessSaleOther"
  val TRANSACTION_TOTAL_CONSIDERATION = "transaction_totalConsideration"

  // Box 2: Commercial use
  val TRANSACTION_COMMERCIAL_USE_OFFICE = "transaction_commercialUseOffice"
  val TRANSACTION_COMMERCIAL_USE_SHOP = "transaction_commercialUseShop"
  val TRANSACTION_COMMERCIAL_USE_FACTORY = "transaction_commercialUseFactory"
  val TRANSACTION_COMMERCIAL_USE_HOTEL = "transaction_commercialUseHotel"
  val TRANSACTION_COMMERCIAL_USE_WAREHOUSE = "transaction_commercialUseWarehouse"
  val TRANSACTION_COMMERCIAL_USE_INDUSTRIAL = "transaction_commercialUseIndustrial"
  val TRANSACTION_COMMERCIAL_USE_OTHER = "transaction_commercialUseOther"

  // Box 3: Transaction ruling
  val TRANSACTION_POST_TRANSACTION_RULING_YES = "transaction_postTransactionRuling_yes"
  val TRANSACTION_POST_TRANSACTION_RULING_NO = "transaction_postTransactionRuling_no"
  val TRANSACTION_RULING_FOLLOWED_YES = "transaction_rulingFollowed_yes"
  val TRANSACTION_RULING_FOLLOWED_NO = "transaction_rulingFollowed_no"
  val TRANSACTION_RULING_FOLLOWED_RULING_NOT_RECEIVED = "transaction_rulingFollowed_ruling"

  // Box 4: Depends on future event
  val TRANSACTION_DEPENDS_FUTURE_EVENT_YES = "transaction_dependsFutureEvent_yes"
  val TRANSACTION_DEPENDS_FUTURE_EVENT_NO = "transaction_dependsFutureEvent_no"

  // Box 5: Pay deferred
  val TRANSACTION_PAY_DEFERRED_YES = "transaction_payDeferred_yes"
  val TRANSACTION_PAY_DEFERRED_NO = "transaction_payDeferred_no"

  // Box 7: Purchaser Description
  val PURCHASER_DESCRIPTION_1 = "purchaser_description_1"
  val PURCHASER_DESCRIPTION_2 = "purchaser_description_2"
  val PURCHASER_DESCRIPTION_3 = "purchaser_description_3"
  val PURCHASER_DESCRIPTION_4 = "purchaser_description_4"

  // Box 26: Terms surrendered
  val LEASE_TERMS_SURRENDERED = "lease_termsSurrendered"

  // Box 27: Break clause type
  val LEASE_BREAK_CLAUSE_LANDLORD = "lease_breakClause_landlord"
  val LEASE_BREAK_CLAUSE_TENANT = "lease_breakClause_tenant"
  val LEASE_BREAK_CLAUSE_EITHER = "lease_breakClause_either"

  // Box 28: Break clause date
  val LEASE_BREAK_CLAUSE_DAY = "lease_breakClauseDate_day"
  val LEASE_BREAK_CLAUSE_MONTH = "lease_breakClauseDate_month"
  val LEASE_BREAK_CLAUSE_YEAR = "lease_breakClauseDate_year"

  // Box 29: Relate to the lease
  val LEASE_OPTION_TO_RENEW = "lease_optionToRenew"
  val LEASE_MARKET_RENT = "lease_marketRent"
  val LEASE_TURNOVER_RENT = "lease_turnoverRent"
  val LEASE_UNASCERTAINABLE_RENT = "lease_unascertainableRent"
  val LEASE_CONTINGENT_RESERVED_RENT = "lease_contingentReservedRent"

  // Box 30: Rent review frequency
  val LEASE_REVIEW_FREQUENCY = "lease_reviewFrequency"

  // Box 31: First review date
  val LEASE_FIRST_REVIEW_DATE_DAY = "lease_firstReviewDate_day"
  val LEASE_FIRST_REVIEW_DATE_MONTH = "lease_firstReviewDate_month"
  val LEASE_FIRST_REVIEW_DATE_YEAR = "lease_firstReviewDate_year"

  // Box 32: Rent review clause
  val LEASE_REVIEW_CLAUSE_OPEN = "lease_reviewClause_open"
  val LEASE_REVIEW_CLAUSE_RPI = "lease_reviewClause_rpi"
  val LEASE_REVIEW_CLAUSE_OTHER = "lease_reviewClause_other"

  // Box 33: Rent charge date
  val LEASE_RENT_CHARGE_DATE_DAY = "lease_rentChargeDate_day"
  val LEASE_RENT_CHARGE_DATE_MONTH = "lease_rentChargeDate_month"
  val LEASE_RENT_CHARGE_DATE_YEAR = "lease_rentChargeDate_year"

  // Box 34: Service charge amount
  val LEASE_SERVICE_CHARGE = "lease_serviceCharge"

  // Box 35: Service charge frequency
  val LEASE_SERVICE_CHARGE_FREQUENCY_MONTHLY = "lease_serviceChargeFrequency_monthly"
  val LEASE_SERVICE_CHARGE_FREQUENCY_QUARTERLY = "lease_serviceChargeFrequency_quarterly"
  val LEASE_SERVICE_CHARGE_ANNUALLY = "lease_serviceChargeFrequency_annually"
  val LEASE_SERVICE_CHARGE_OTHER = "lease_serviceChargeFrequency_other"

  // Box 36: Other considerations - tenant to landlord
  val LEASE_TO_LANDLORD_1 = "lease_toLandlord_1"
  val LEASE_TO_LANDLORD_2 = "lease_toLandlord_2"
  val LEASE_TO_LANDLORD_3 = "lease_toLandlord_3"
  val LEASE_TO_LANDLORD_4 = "lease_toLandlord_4"

  // Box 37: Other considerations - landlord to tenant
  val LEASE_TO_TENANT_1 = "lease_toTenant_1"
  val LEASE_TO_TENANT_2 = "lease_toTenant_2"
  val LEASE_TO_TENANT_3 = "lease_toTenant_3"
  val LEASE_TO_TENANT_4 = "lease_toTenant_4"

  // =========================================================================
  // Checkbox state constants
  // =========================================================================
  val CHECKBOX_ON  = "Yes"
  val CHECKBOX_OFF = "Off"

  // =========================================================================
  // Page number 6 & 7 additional purchaser PAGE 6 & 7 — About the Additional Purchaser (63-73)
  // =========================================================================
  
  // val PURCHASER_TITLE = "purchaser_title"
   val PURCHASER_SURNAME = "purchaser_companyName"
  // val PURCHASER_COMPANY_NAME = "purchaser_companyName"
  // val PURCHASER_FORENAME_1 = "purchaser_forename1"
  // val PURCHASER_FORENAME_2 = "purchaser_forename2"
  // val PURCHASER_HOUSE_NUMBER = "purchaser_houseNumber"
  val PURCHASER_ADDRESS_LINE1 = "purchaser_addressLine1"
  val PURCHASER_ADDRESS_LINE2 = "purchaser_addressLine2"
  val PURCHASER_ADDRESS_LINE3 = "purchaser_addressLine3"
  val PURCHASER_ADDRESS_LINE4 = "purchaser_addressLine4"
  // val PURCHASER_POSTCODE_1 = "purchaser_postcode_1"
  // val PURCHASER_POSTCODE_2 = "purchaser_postcode_2"
  // val PURCHASER_ACTING_TRUSTEE_YES = "purchaser_actingTrustee_yes"
  // val PURCHASER_ACTING_TRUSTEE_NO = "purchaser_actingTrustee_no"

  val PURCHASER2_SAMEADDRESS = "purchaser_sameAddress"

  val PURCHASER_AGENT_NAME = "purchaser_agentName"
  val PURCHASER_AGENT_POSTCODE_1 = "purchaser_agentPostcode_1"
  val PURCHASER_AGENT_POSTCODE_2 = "purchaser_agentPostcode_2"
  val PURCHASER_AGENT_HOUSE_NUMBER = "purchaser_agentHouseNumber"
  val PURCHASER_AGENT_ADDRESS_LINE1 = "purchaser_agentAddressLine1"
  val PURCHASER_AGENT_ADDRESS_LINE2 = "purchaser_agentAddressLine2"
  val PURCHASER_AGENT_ADDRESS_LINE3 = "purchaser_agentAddressLine3"
  val PURCHASER_AGENT_ADDRESS_LINE4 = "purchaser_agentAddressLine4"
  val PURCHASER_AGENT_DX_NUMBER = "purchaser_agentDxAddress"
  val PURCHASER_AGENT_REFERENCE = "purchaser_agentReference"
  val PURCHASER_AGENT_PHONE_NUMBER = "purchaser_agentPhoneNumber"

  val PURCHASER_ADDITIONALDETAILS_SDLT2 = "return_additionalDetailsSdlt2"
  val LAND_ADDITIONALDETAILS_SDLT3 = "return_additionalDetailsSdlt3"
  val LEASE_ADDITIONALDETAILS_SDLT4 = "return_additionalDetailsSdlt4"


}
