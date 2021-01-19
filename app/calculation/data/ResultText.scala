/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.data

object ResultText {

  private val BASED_ON_THE_RULES_FROM   = "based on the rules from"
  private val BASED_ON_THE_RULES_BEFORE = "based on the rules before"

  private val DATE_17_03_2016 = "17 March 2016"
  private val DATE_01_04_2016 = "1 April 2016"
  private val DATE_08_07_2020 = "8 July 2020 to 31 March 2021"
  private val DATE_26_11_2015 = "26 November 2015"

  private val RESULT_HEADING_BEFORE = "Results based on SDLT rules before"
  private val RESULT_HEADING_FROM   = "Results based on SDLT rules from"

  private val RESULT_HINT_EXCHANGE_BEFORE = "You may be entitled to pay SDLT using the old rules if you exchanged contracts before"
  private val RESULT_HINT_DISPOSE_BEFORE  = "If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
                                            " You must apply for any repayment within 12 months of disposing of your old main residence"

  val DETAIL_HEADING_TOTAL_SDLT        = "This is a breakdown of how the total amount of SDLT was calculated"
  val DETAIL_COL_HEADER_PURCHASE_PRICE = "Purchase price bands (£)"
  val DETAIL_FOOTER_TOTAL              = "Total SDLT due"
  val DETAIL_ADDITIONAL_DWELLINGS      = "Result if you become eligible for a repayment of the higher rate on additional dwellings"

  val DETAIL_HEADING_SDLT_ON_RENT = "This is a breakdown of how the amount of SDLT on the rent was calculated"
  val DETAIL_COL_HEADER_RENT      = "Rent bands (£)"
  val DETAIL_FOOTER_RENT          = "SDLT due on the rent"

  val DETAIL_HEADING_SDLT_ON_PREM = "This is a breakdown of how the amount of SDLT on the premium was calculated"
  val DETAIL_COL_HEADER_PREM      = "Premium bands (£)"
  val DETAIL_FOOTER_PREM          = "SDLT due on the premium"

  val DETAIL_HEADING_SDLT_ON_RENT_FROM_MAR_2016   = s"$DETAIL_HEADING_SDLT_ON_RENT $BASED_ON_THE_RULES_FROM $DATE_17_03_2016"
  val DETAIL_HEADING_SDLT_ON_PREM_FROM_MAR_2016   = s"$DETAIL_HEADING_SDLT_ON_PREM $BASED_ON_THE_RULES_FROM $DATE_17_03_2016"
  val DETAIL_HEADING_SDLT_ON_RENT_BEFORE_MAR_2016 = s"$DETAIL_HEADING_SDLT_ON_RENT $BASED_ON_THE_RULES_BEFORE $DATE_17_03_2016"

  val DETAIL_HEADING_TOTAL_SDLT_FROM_MAR_2016 = s"$DETAIL_HEADING_TOTAL_SDLT $BASED_ON_THE_RULES_FROM $DATE_17_03_2016"
  val DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016 = s"$DETAIL_HEADING_TOTAL_SDLT $BASED_ON_THE_RULES_BEFORE $DATE_01_04_2016"
  val DETAIL_HEADING_TOTAL_SDLT_FROM_APR_2016 = s"$DETAIL_HEADING_TOTAL_SDLT $BASED_ON_THE_RULES_FROM $DATE_01_04_2016"

  val DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020 = s"$DETAIL_HEADING_TOTAL_SDLT $BASED_ON_THE_RULES_FROM $DATE_08_07_2020"
  val DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020_AND_BEFORE_MARCH_2021 = s"$RESULT_HEADING_FROM $DATE_08_07_2020"

  val DETAIL_HEADING_SDLT_ON_RENT_FROM_APR_2016 = s"$DETAIL_HEADING_SDLT_ON_RENT $BASED_ON_THE_RULES_FROM $DATE_01_04_2016"
  val DETAIL_HEADING_SDLT_ON_RENT_BEFORE_APR_2016 = s"$DETAIL_HEADING_SDLT_ON_RENT $BASED_ON_THE_RULES_BEFORE $DATE_01_04_2016"
  val DETAIL_HEADING_SDLT_ON_PREM_BEFORE_APR_2016 = s"$DETAIL_HEADING_SDLT_ON_PREM $BASED_ON_THE_RULES_BEFORE $DATE_01_04_2016"
  val DETAIL_HEADING_SDLT_ON_PREM_FROM_APR_2016 = s"$DETAIL_HEADING_SDLT_ON_PREM $BASED_ON_THE_RULES_FROM $DATE_01_04_2016"

  val RESULT_HEADING_BEFORE_MAR_2016       = s"$RESULT_HEADING_BEFORE $DATE_17_03_2016"
  val RESULT_HINT_EXCHANGE_BEFORE_MAR_2016 = s"$RESULT_HINT_EXCHANGE_BEFORE $DATE_17_03_2016."
  val RESULT_HEADING_BEFORE_APR_2016       = s"$RESULT_HEADING_BEFORE $DATE_01_04_2016"
  val RESULT_HEADING_BEFORE_JULY_2020       = s"$DETAIL_ADDITIONAL_DWELLINGS"
  val RESULT_HEADING_PREVIOUS_AFTER_MARCH_2021 = s"$DETAIL_ADDITIONAL_DWELLINGS"
  val RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021 = "Result of calculation based on SDLT rates for transactions dated 8 July 2020 to 31 March 2021"
  val RESULT_HEADING_AFTER_MARCH_2021 = "Results of calculation based on SDLT rules for the effective date entered"
  val RESULT_HINT_EXCHANGE_BEFORE_NOV_2015 = s"$RESULT_HINT_EXCHANGE_BEFORE $DATE_26_11_2015."
  val RESULT_HINT_EXCHANGE_JULY_20 = s"$RESULT_HINT_DISPOSE_BEFORE."
  val RESULT_HINT_EXCHANGE_AFTER_MARCH_21 = s"$RESULT_HINT_DISPOSE_BEFORE."

  val RESULT_HEADING_FROM_MAR_2016 = s"$RESULT_HEADING_FROM $DATE_17_03_2016"
  val RESULT_HEADING_FROM_APR_2016 = s"$RESULT_HEADING_FROM $DATE_01_04_2016"
  val RESULT_HEADING_FROM_JULY_2020 = s"$RESULT_HEADING_FROM $DATE_08_07_2020"
  val RESULT_HINT_ADDNL_PROP_REFUND = "If you dispose of your previous main residence within 3 years you may be eligible for a refund of £"
  val RESULT_HINT_ADDNL_PROP_2020 = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. "
  val RESULT_HINT_ADDNL_PROP_AFTER_MARCH_2021 = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. "
}
