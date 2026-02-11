/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

object FormatUtils {

  val keyCssClass = "govuk-!-width-one-half"
  val valueCssClass = "govuk-!-text-align-right"

  def bigDecimalFormat(value: BigDecimal, currencySymbol: String = "£"): String =
    currencySymbol + f"$value%1.2f".replace(".00", "")
      .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")

}
