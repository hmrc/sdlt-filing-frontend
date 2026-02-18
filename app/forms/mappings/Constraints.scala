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

package forms.mappings

import config.CurrencyFormatter
import play.api.data.validation
import play.api.data.validation.Constraints.minLength
import play.api.data.validation.{Constraint, Invalid, Valid}
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.{Failure, Success, Try}

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def optionalRegexp(regex: String, errorKey: String): Constraint[Option[String]] =
    Constraint {
      case Some(str) if str.matches(regex) =>
        Valid
      case None => Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def optionalMaxLength(maximum: Int, errorKey: String): Constraint[Option[String]] =
    Constraint {
      case Some(str) if str.length > maximum => Invalid(errorKey, maximum)
      case _ => Valid
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def minimumCurrency(minimum: BigDecimal, errorKey: String)(implicit ev: Ordering[BigDecimal]): Constraint[BigDecimal] =
    Constraint {
      input =>
        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, CurrencyFormatter.currencyFormat(minimum))
        }
    }

  protected def maximumCurrency(maximum: BigDecimal, errorKey: String)(implicit ev: Ordering[BigDecimal]): Constraint[BigDecimal] = {
    Constraint {
      input =>
        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, CurrencyFormatter.currencyFormat(maximum))
        }
    }
  }

  protected def validUtr(errorKey: String): Constraint[String] = {

    def checkSum(errorKey: String): Constraint[String] = {
      val weights: Seq[Int] = Seq(6, 7, 8, 9, 10, 5, 4, 3, 2)

      def validateCheckSum(utr: String) = Try {
        val utrInts: Seq[Int] = utr.map(_.asDigit)
        val utrSum: Int = utrInts.slice(1, 10).zip(weights).map { case (x, y) => x * y }.sum
        val utrCalc = 11 - (utrSum % 11)
        val checkSum = if (utrCalc > 9) utrCalc - 9 else utrCalc
        checkSum == utrInts.head
      } match {
        case Success(s) => s
        case Failure(_) => false
      }

      Constraint { str =>
        if (validateCheckSum(str)) Valid else Invalid(errorKey)
      }
    }

    firstError(
      regexp("^[0-9]*$", s"${errorKey}.regex.invalid"),
      minLength(10, s"${errorKey}.length"),
      maxLength(10, s"${errorKey}.length"),
      checkSum(s"${errorKey}.invalid")
    )
  }

  protected def vatCheckF16Validation(errorKey: String): Constraint[String] = {

    def vatF16Check(errorKey: String): Constraint[String] = {

      def validateVAT(raw: String): Boolean = Try {
        val normalized = {
          val up = Option(raw).getOrElse("").trim.toUpperCase
          val noGb = if (up.startsWith("GB")) up.drop(2) else up
          noGb.replaceAll("\\s+", "")
        }
        val bodyDigits = normalized.substring(0, 7)
        val checkDigitsValue = normalized.substring(7, 9).toInt

        val weights = Array(8, 7, 6, 5, 4, 3, 2)
        val sum = bodyDigits
          .map(_.asDigit)
          .zip(weights)
          .map { case (digit, weight) => digit * weight }
          .sum

        val check1 = 97 - Math.floorMod(sum, 97)
        val check2 = 97 - Math.floorMod(sum + 55, 97)

        checkDigitsValue >= 0 &&
          checkDigitsValue <= 97 &&
          (checkDigitsValue == check1 || checkDigitsValue == check2)
      } match {
        case Success(isValid) => isValid
        case Failure(_) => false
      }

      Constraint[String] { str =>
        if (validateVAT(str)) Valid else Invalid(errorKey)
      }
    }

    firstError(
      regexp("^[0-9]*$", s"${errorKey}.regex.invalid"),
      minLength(9, s"${errorKey}.length"),
      maxLength(9, s"${errorKey}.length"),
      vatF16Check(s"${errorKey}.invalid")
    )
  }

  protected def maxCheckboxes(maximumSelection: Int, errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.size <= maximumSelection =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def localAuthorityCodeConstraints(effectiveTransactionDate: Option[LocalDate], contractEffDate: Option[LocalDate], postcode: Option[String], errorKey: String): Constraint[String] = Constraint {

    code =>
      val codeTrim = code
      val authorityCodeRegex = "^((0114)|(0116)|(0119)|(0121)|(0205)|(0215)|(0220)|(0230)|(0335)|(0340)|(0345)|(0350)|(0355)|(0360)|(0405)|(0410)|(0415)|(0425)|(0435)|(0505)|(0510)|(0515)|(0520)|(0530)|(0540)|(0605)|(0610)|(0615)|(0620)|(0630)|(0635)|(0650)|(0655)|(0724)|(0728)|(0734)|(0738)|(0805)|(0810)|(0815)|(0820)|(0825)|(0830)|(0835)|(0905)|(0910)|(0915)|(0920)|(0925)|(0930)|(1005)|(1010)|(1015)|(1025)|(1030)|(1035)|(1040)|(1045)|(1055)|(1105)|(1110)|(1115)|(1125)|(1130)|(1135)|(1145)|(1150)|(1160)|(1165)|(1210)|(1215)|(1225)|(1230)|(1235)|(1240)|(1250)|(1255)|(1305)|(1315)|(1320)|(1325)|(1330)|(1335)|(1340)|(1350)|(1410)|(1415)|(1425)|(1430)|(1435)|(1445)|(1505)|(1510)|(1515)|(1520)|(1525)|(1530)|(1535)|(1540)|(1545)|(1550)|(1560)|(1570)|(1590)|(1595)|(1605)|(1610)|(1615)|(1620)|(1625)|(1630)|(1705)|(1710)|(1715)|(1720)|(1725)|(1730)|(1735)|(1740)|(1750)|(1760)|(1765)|(1775)|(1780)|(1805)|(1825)|(1835)|(1840)|(1845)|(1850)|(1860)|(1905)|(1910)|(1915)|(1920)|(1925)|(1930)|(1935)|(1940)|(1945)|(1950)|(2001)|(2002)|(2003)|(2004)|(2100)|(2205)|(2210)|(2215)|(2220)|(2230)|(2235)|(2245)|(2250)|(2255)|(2260)|(2265)|(2270)|(2280)|(2315)|(2320)|(2325)|(2330)|(2335)|(2340)|(2345)|(2350)|(2355)|(2360)|(2365)|(2370)|(2372)|(2373)|(2405)|(2410)|(2415)|(2420)|(2430)|(2435)|(2440)|(2465)|(2470)|(2505)|(2510)|(2515)|(2520)|(2525)|(2530)|(2535)|(2605)|(2610)|(2615)|(2620)|(2625)|(2630)|(2635)|(2705)|(2710)|(2715)|(2720)|(2725)|(2730)|(2735)|(2741)|(2805)|(2810)|(2815)|(2820)|(2825)|(2830)|(2835)|(2905)|(2910)|(2915)|(2920)|(2925)|(2930)|(3005)|(3010)|(3015)|(3020)|(3025)|(3030)|(3040)|(3060)|(3105)|(3110)|(3115)|(3120)|(3125)|(3205)|(3210)|(3215)|(3220)|(3225)|(3240)|(3305)|(3310)|(3315)|(3320)|(3325)|(3405)|(3410)|(3415)|(3420)|(3425)|(3430)|(3435)|(3445)|(3455)|(3505)|(3510)|(3515)|(3520)|(3525)|(3530)|(3535)|(3605)|(3610)|(3615)|(3620)|(3625)|(3630)|(3635)|(3640)|(3645)|(3650)|(3655)|(3705)|(3710)|(3715)|(3720)|(3725)|(3805)|(3810)|(3815)|(3820)|(3825)|(3830)|(3835)|(3905)|(3910)|(3915)|(3925)|(3935)|(4205)|(4210)|(4215)|(4220)|(4225)|(4230)|(4235)|(4240)|(4245)|(4250)|(4305)|(4310)|(4315)|(4320)|(4325)|(4405)|(4410)|(4415)|(4420)|(4505)|(4510)|(4515)|(4520)|(4525)|(4605)|(4610)|(4615)|(4620)|(4625)|(4630)|(4635)|(4705)|(4710)|(4715)|(4720)|(4725)|(5030)|(5060)|(5090)|(5120)|(5150)|(5180)|(5210)|(5240)|(5270)|(5300)|(5330)|(5360)|(5390)|(5420)|(5450)|(5480)|(5510)|(5540)|(5570)|(5600)|(5630)|(5660)|(5690)|(5720)|(5750)|(5780)|(5810)|(5840)|(5870)|(5900)|(5930)|(5960)|(5990)|(6805)|(6810)|(6815)|(6820)|(6825)|(6828)|(6829)|(6830)|(6835)|(6840)|(6845)|(6850)|(6853)|(6854)|(6855)|(6905)|(6910)|(6915)|(6920)|(6925)|(6930)|(6935)|(6940)|(6945)|(6950)|(6955)|(6996)|(6997)|(6998)|(6999)|(7005)|(7010)|(7015)|(7020)|(7025)|(7030)|(7035)|(7040)|(7045)|(7050)|(7055)|(7060)|(7065)|(7070)|(7075)|(7080)|(7085)|(7090)|(7095)|(7100)|(7105)|(7110)|(7115)|(7120)|(7125)|(7130)|(8998)|(8999)|(9000)|(9010)|(9020)|(9051)|(9052)|(9053)|(9054)|(9055)|(9056)|(9057)|(9058)|(9059)|(9060)|(9061)|(9062)|(9063)|(9064)|(9065)|(9066)|(9067)|(9068)|(9069)|(9070)|(9071)|(9072)|(9073)|(9074)|(9075)|(9076)|(9077)|(9078)|(9079)){0,4}$"
      val welshLocalAuthNumbers = "^((6805)|(6810)|(6815)|(6820)|(6825)|(6828)|(6829)|(6830)|(6835)|(6840)|(6845)|(6850)|(6853)|(6854)|(6855)|(6905)|(6910)|(6915)|(6920)|(6925)|(6930)|(6935)|(6940)|(6945)|(6950)|(6955)|(6996)|(6997)|(6998)|(6999)){0,4}$"

      if (codeTrim.matches(authorityCodeRegex)) {

        if (checkScotlandRegex(effectiveTransactionDate, contractEffDate, code, postcode) && !codeTrim.matches(welshLocalAuthNumbers)) {
          Valid
        } else if (checkWelshRegex(effectiveTransactionDate, code, contractEffDate, welshLocalAuthNumbers) && !isScottishPostcode(postcode) && !code.matches("^9[0-9]{3}$") && !code.matches("^899[89]$")) {
          Valid
        } else {
          Invalid(errorKey)
        }
      }
      else {
        Invalid(errorKey)
      }
  }

  private def checkWelshRegex(
                               effectiveTransactionDate: Option[LocalDate],
                               code: String,
                               contractEffDate: Option[LocalDate],
                               welshLocalAuthNumbers: String
                             ): Boolean = {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val walesEffectiveActDate = LocalDate.parse("2018-04-01", formatter)
    val walesActDate = LocalDate.parse("2014-12-17", formatter)
    val isInvalid: Boolean = effectiveTransactionDate.exists { effectiveTransactiondt =>

      val isWelsh = code.matches(welshLocalAuthNumbers)
      val walesEffectiveDtBeforeAct = effectiveTransactiondt.isBefore(walesEffectiveActDate)

      isWelsh match {

        case true if (!Set("6996", "6997", "6998", "6999").contains(code)) && walesEffectiveDtBeforeAct => false

        case true if Set("6996", "6997").contains(code) && !walesEffectiveDtBeforeAct => false

        case true if (code == "6998") && contractEffDate.exists(contractEffDate => !contractEffDate.isBefore(walesEffectiveActDate)) => false

        case true if (code == "6999") && contractEffDate.exists(contractEffDate => !contractEffDate.isBefore(walesActDate)) => false

        case _ => true

      }
    }
    !isInvalid
  }

  private def checkScotlandRegex(effectiveTransactionDate: Option[LocalDate], contractEffDate: Option[LocalDate], code: String, postcode: Option[String]): Boolean = {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val cr223EffectiveFrom = LocalDate.parse("2015-04-01", formatter)
    val currentDay = LocalDate.now()
    val theScotlandActDateTime = LocalDate.parse("2012-05-01", formatter)
    code.matches("^899[89]$") match {
      case true =>
        true match {
          case _ if currentDay.isBefore(cr223EffectiveFrom) ||
            effectiveTransactionDate.exists(_.isBefore(cr223EffectiveFrom)) =>
            false

          case _ if contractEffDate.isEmpty =>
            false
          case _ if code == "8999" &&
            !contractEffDate.exists(_.isBefore(theScotlandActDateTime)) =>
            false
          case _ if code == "8998" && !contractEffDate.exists(_.isBefore(cr223EffectiveFrom)) =>
            false
          case _ => true
        }

      case false if code.matches("^9[0-9]{3}$") && !currentDay.isBefore(cr223EffectiveFrom) &&
        !effectiveTransactionDate.exists(_.isBefore(cr223EffectiveFrom)) => false

      case false if !currentDay.isBefore(cr223EffectiveFrom) &&
        !effectiveTransactionDate.exists(_.isBefore(cr223EffectiveFrom)) => !isScottishPostcode(postcode)

      case _ => false
    }

  }

  private def isScottishPostcode(postCode: Option[String]) = {

    postCode match {
      case Some(postcode) =>
        val postcodeOutcode = postcode.substring(0, postcode.indexOf(" "));

        postcodeOutcode.matches("^AB([1-3]|23|3[0-8]|4[1-5]|5[1-6])$")
          || postcodeOutcode.matches("^DD([1-9]|1[0-1])$")
          || postcodeOutcode.matches("^PH([1-9]|1[0-9]|2[0-6]|3[0-9]|4[0-4])$")
          || postcodeOutcode.matches("^FK([1-9]|1[0-9]|2[0-1])$")
          || postcodeOutcode.matches("^G([1-2]|1[1-2]|14|15|20|21|32|33|41|43|45|46|51|53|6[0-9]|7[1-8]|8[1-4])$")
          || postcodeOutcode.matches("^PA(1|[3-9]|1[0-4]|1[6-9]|[2-4][0-9]|6[0-8]|7[0-8])$")
          || postcodeOutcode.matches("^DG([1-9]|10|11|13)$")
          || postcodeOutcode.matches("^KA([1-9]|1[0-9]|2[0-9]|30)$")
          || postcodeOutcode.matches("^ML([1-9]|1[0-2])$")
          || postcodeOutcode.matches("^EH([1-2]|[4-9]|10|1[2-9]|2[0-9]|3[0-9]|4[0-9]|5[2-5])$")
          || postcodeOutcode.matches("^TD([1-4]|[6-7]|10|11|13|14)$")
          || postcodeOutcode.matches("^KY([1-9]|1[0-6])$")
          || postcodeOutcode.matches("^IV([1-9]|1[0-9]|2[0-8]|3[0-2]|36|4[0-9]|5[1-6])$")
          || postcodeOutcode.matches("^KW([1-3]|[5-9]|1[0-7])$")
          || postcodeOutcode.matches("^ZE([1-3])$")
          || postcodeOutcode.matches("^HS([1-9])$")

      case None => false
    }
  }
}
