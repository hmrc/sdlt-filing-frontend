package calculation.utils

object StringUtils {

  def intToMonetaryString(amount: Int): String = {
    amount.toString.replaceAll("""\B(?=(\d{3})+(?!\d))""", ",")
  }

}
