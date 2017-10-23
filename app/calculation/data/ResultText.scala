package calculation.data

object ResultText {

  val _BASED_ON_THE_RULES_FROM_ = " based on the rules from "

  private val DATE_17_03_2016 = "17 March 2016"

  val DETAIL_HEADING_TOTAL_SDLT        = "This is a breakdown of how the total amount of SDLT was calculated"
  val DETAIL_COL_HEADER_PURCHASE_PRICE = "Purchase price bands (£)"
  val DETAIL_FOOTER_TOTAL              = "Total SDLT due"

  val DETAIL_HEADING_TOTAL_SDLT_FROM_MAR_2016 = s"$DETAIL_HEADING_TOTAL_SDLT${_BASED_ON_THE_RULES_FROM_}$DATE_17_03_2016"

  private val RESULT_HEADING_BEFORE_ = "Results based on SDLT rules before "
  private val RESULT_HEADING_FROM_   = "Results based on SDLT rules from "

  private val RESULT_HINT_EXCHANGE_BEFORE_ = "You may be entitled to pay SDLT using the old rules if you exchanged contracts before "

  val RESULT_HEADING_BEFORE_MAR_2016       = s"$RESULT_HEADING_BEFORE_$DATE_17_03_2016"
  val RESULT_HINT_EXCHANGE_BEFORE_MAR_2016 = s"$RESULT_HINT_EXCHANGE_BEFORE_$DATE_17_03_2016."

  val RESULT_HEADING_FROM_MAR_2016 = s"$RESULT_HEADING_FROM_$DATE_17_03_2016"
}
