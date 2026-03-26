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

  // Relief claimed (left column, above box 9)
  val CALCULATION_CLAIMING_RELIEF_YES           = "calculation_claimingRelief_yes"
  val CALCULATION_CLAIMING_RELIEF_NO            = "calculation_claimingRelief_no"
  val CALCULATION_CLAIMING_RELIEF_REASON        = "calculation_claimingReliefReason"
  val CALCULATION_CLAIMING_RELIEF_SCHEME_NUMBER = "calculation_claimingReliefSchemeNumber"
  val CALCULATION_CLAIMING_RELIEF_AMOUNT        = "calculation_claimingReliefAmount"

  // Box 9: Total consideration (single wide field — full amount as string)
  val CALCULATION_TOTAL_CONSIDERATION = "calculation_totalConsideration"

  // Box 9 also has split digit-group fields (_1 leftmost, _4 rightmost)
  // These appear to be for manual entry boxes — populate both for compatibility
  val CALCULATION_TOTAL_CONSIDERATION_1 = "calculation_totalConsideration_1"
  val CALCULATION_TOTAL_CONSIDERATION_2 = "calculation_totalConsideration_2"
  val CALCULATION_TOTAL_CONSIDERATION_3 = "calculation_totalConsideration_3"
  val CALCULATION_TOTAL_CONSIDERATION_4 = "calculation_totalConsideration_4"

  // Box 10: VAT in consideration
  val CALCULATION_TOTAL_CONSIDERATION_VAT = "calculation_totalConsiderationVatAmount"

  // Box 12: Linked transactions
  val CALCULATION_LINKED_TRANSACTION_YES   = "calculation_linkedTransaction_yes"
  val CALCULATION_LINKED_TRANSACTION_NO    = "calculation_linkedTransaction_no"

  // Box 13: Total linked consideration
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

  // Box 21: End date for starting rent (split)
  val LEASE_STARTING_RENT_END_DATE_DAY   = "lease_startingRentEndDate_day"
  val LEASE_STARTING_RENT_END_DATE_MONTH = "lease_startingRentEndDate_month"
  val LEASE_STARTING_RENT_END_DATE_YEAR  = "lease_startingRentEndDate_year"

  // Box 22: Later rent known
  val LEASE_STARTING_RENT_LATER_KNOWN_YES = "lease_startingRentLaterKnown_yes"
  val LEASE_STARTING_RENT_LATER_KNOWN_NO  = "lease_startingRentLaterKnown_no"

  // Box 23: VAT on lease
  val LEASE_VAT_AMOUNT = "lease_vatAmount"

  // Box 24: Total premium
  val LEASE_PREMIUM_PAID = "lease_premiumPaid"

  // Box 25: Net present value
  val LEASE_NET_PRESENT_VALUE = "lease_netPresentValue"

  // Box 25a: Tax due — premium
  val LEASE_TOTAL_PREMIUM_TAX = "lease_totalPremiumTax"

  // Box 25b: Tax due — NPV
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

  // Box 29: Address on SDLT3
  val REST_OF_ADDRESS_ON_SUPPLEMENTARY_RETURN = "restOfAddressOnSupplementaryReturn"
  // Note: the "?" named field is the yes checkbox for box 29 (unnamed in template)
  val LAND_ADDRESS_ON_SDLT3_YES = "?"
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

  // Box 36: Vendor surname/company name
  val VENDOR_NAME = "vendor_name"

  // Box 37: Vendor first name(s)
  val VENDOR_FORENAME_1 = "vendor_forename1"
  val VENDOR_FORENAME_2 = "vendor_forename2"

  // Box 38: Vendor address
  val VENDOR_POSTCODE_1    = "vendor_postcode_1"
  val VENDOR_POSTCODE_2    = "vendor_postcode_2"
  val VENDOR_HOUSE_NUMBER  = "vendor_houseNumber"
  val VENDOR_ADDRESS_LINE1 = "vendor_addressLine1"
  val VENDOR_ADDRESS_LINE2 = "vendor_addressLine2"
  val VENDOR_ADDRESS_LINE3 = "vendor_addressLine3"
  val VENDOR_ADDRESS_LINE4 = "vendor_addressLine4"

  // Box 39: Agent name
  val VENDOR_AGENT_NAME = "vendor_agentName"

  // =========================================================================
  // Checkbox state constants
  // =========================================================================
  val CHECKBOX_ON  = "Yes"
  val CHECKBOX_OFF = "Off"
}