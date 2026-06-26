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

object Sdlt2PurchaserFields {

  // =========================================================================
  // SHARED (appears on every page)
  // =========================================================================
  
  val UTRN = SdltPdfFields.UTRN
  val PRINT_STATUS = SdltPdfFields.PRINT_STATUS
  val IR_MARK = SdltPdfFields.IR_MARK

  // =========================================================================
  // PAGE 1 — About the Purchaser (Boxes 1-5)
  // =========================================================================

  // Box 1: Additional Vendor or Purchaser
  val IS_PURCHASER = "is_purchaser"

  // Box 2: Title
  val PURCHASER_TITLE = "purchaser_title"

  // BOX 3: Surname or Company name
  val PURCHASER_SURNAME = "purchaser_surname"
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
}


