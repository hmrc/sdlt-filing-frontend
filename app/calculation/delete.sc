def calcTax(amount: BigDecimal, rate: BigDecimal): BigDecimal = {
  Math.floor(Math.floor(amount.toDouble) * rate.toDouble / 100)
}

val x = calcTax(2000000,5)
println("test"+x)