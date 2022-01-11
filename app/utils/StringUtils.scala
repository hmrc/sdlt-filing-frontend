/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package utils

object StringUtils {

  private val locale = new java.util.Locale("en", "EN")
  private val formatter = java.text.NumberFormat.getIntegerInstance(locale)

  def intToMonetaryString(amount: Int): String = {
    formatter.format(amount)
  }

}
