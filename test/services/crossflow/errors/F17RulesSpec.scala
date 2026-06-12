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

package services.crossflow.errors

import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.{Land, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.transaction.{TransactionDateOfContractPage, TransactionEffectiveDatePage}
import services.crossflow.*

import java.time.LocalDate

class F17RulesSpec extends SpecBase with Matchers {

  private val welshActEffective = LocalDate.of(2018, 4, 1)
  private val welshActDate      = LocalDate.of(2014, 12, 17)

  private def answersWith(
                           effectiveDate: Option[LocalDate] = None,
                           contractDate:  Option[LocalDate] = None
                         ): UserAnswers = {
    val committedTransaction = Transaction(
      effectiveDate = effectiveDate.map(_.toString),
      contractDate  = contractDate.map(_.toString)
    )

    val base = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(committedTransaction)
    )))

    val withEff      = effectiveDate.fold(base)(d => base.set(TransactionEffectiveDatePage, d).success.value)
    val withContract = contractDate.fold(withEff)(d => withEff.set(TransactionDateOfContractPage, d).success.value)
    withContract
  }

  private def landWithCode(code: String): Land =
    Land(landID = Some("LND001"), localAuthorityNumber = Some(code))


  "Cf8_RegularWelshCodes" - {

    "must fire when code is a regular Welsh code (6810) and effective date is on the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6810")

      Cf8_RegularWelshCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-8")
    }

    "must fire when code is a regular Welsh code (6905) and effective date is after the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("6905")

      Cf8_RegularWelshCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-8")
    }

    "must pass when code is a regular Welsh code (6810) and effective date is before the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6810")

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must not apply when code is a Welsh special (6996)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6996")

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must not apply when code is a Welsh special (6998)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6998")

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must not apply when the code is English (5900)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("5900")

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must not apply when localAuthorityNumber is missing" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = Land(landID = Some("LND001"), localAuthorityNumber = None)

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must pass when code is a regular Welsh code (6810) and effective date is missing (None is valid)" in {
      val ua = answersWith(effectiveDate = None)
      val land = landWithCode("6810")

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }
  }


  "Cf9a_Welsh6996_6997EffDate" - {

    "must fire when code is 6996 and effective date is before the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6996")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9a")
    }

    "must fire when code is 6997 and effective date is before the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6997")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9a")
    }

    "must pass when code is 6996 and effective date is missing (None is valid)" in {
      val ua = answersWith(effectiveDate = None)
      val land = landWithCode("6996")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6997 and effective date is missing (None is valid)" in {
      val ua = answersWith(effectiveDate = None)
      val land = landWithCode("6997")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6996 and effective date is on the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6996")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6997 and effective date is on the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6997")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6996 and effective date is after the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("6996")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6997 and effective date is well after the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2030, 1, 1)))
      val land = landWithCode("6997")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a different Welsh special (6998)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6998")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a different Welsh special (6999)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6999")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a regular Welsh code (6810)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6810")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is an English/NI code (5900)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("5900")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Scottish-style code (9001)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("9001")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a dummy code (8999)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("8999")

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply when localAuthorityNumber is missing" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = Land(landID = Some("LND001"), localAuthorityNumber = None)

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must return a failure with the correct rule metadata" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6996")

      val failure = Cf9a_Welsh6996_6997EffDate.validate(land, ua).value

      failure.ruleId     mustBe "Cf-9a"
      failure.affects    mustBe ReturnSection.Land
      failure.messageKey mustBe "crossflow.land.Cf-9.welsh6996_6997.body"
    }

    "must target both the land authority code page and the effective date page" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6996")

      val failure = Cf9a_Welsh6996_6997EffDate.validate(land, ua).value

      failure.targets.map(_.page) must contain allOf (Pages.LandAuthorityCode, Pages.EffectiveDate)
    }
  }


  "Cf9b_Welsh6998EffDate" - {

    "must fire when code is 6998 and effective date is before the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6998")

      Cf9b_Welsh6998EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9b")
    }

    "must pass when code is 6998 and effective date is missing (None is valid)" in {
      val ua   = answersWith(effectiveDate = None)
      val land = landWithCode("6998")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6998 and effective date is on the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6998")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6998 and effective date is after the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("6998")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6996" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6996")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6999" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6999")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a regular Welsh code (6810)" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6810")

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }
  }


  "Cf10_Welsh6998ContractDate" - {

    "must fire when code is 6998 and contract date is on the Wales Act effective date" in {
      val ua   = answersWith(contractDate = Some(welshActEffective))
      val land = landWithCode("6998")

      Cf10_Welsh6998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-10")
    }

    "must fire when code is 6998 and contract date is after the Wales Act effective date" in {
      val ua   = answersWith(contractDate = Some(welshActEffective.plusDays(1)))
      val land = landWithCode("6998")

      Cf10_Welsh6998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-10")
    }

    "must fire when code is 6998 and contract date is missing" in {
      val ua   = answersWith(contractDate = None)
      val land = landWithCode("6998")

      Cf10_Welsh6998ContractDate.validate(land, ua).map(_.ruleId) mustBe None
    }

    "must pass when code is 6998 and contract date is before the Wales Act effective date" in {
      val ua   = answersWith(contractDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6998")

      Cf10_Welsh6998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6999" in {
      val ua   = answersWith(contractDate = Some(welshActEffective.plusDays(1)))
      val land = landWithCode("6999")

      Cf10_Welsh6998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a regular Welsh code (6810)" in {
      val ua   = answersWith(contractDate = Some(welshActEffective.plusDays(1)))
      val land = landWithCode("6810")

      Cf10_Welsh6998ContractDate.validate(land, ua) mustBe None
    }
  }


  "Cf9c_Welsh6999EffDate" - {

    "must fire when code is 6999 and effective date is before the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6999")

      Cf9c_Welsh6999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9c")
    }

    "must pass when code is 6999 and effective date is missing (None is valid)" in {
      val ua   = answersWith(effectiveDate = None)
      val land = landWithCode("6999")

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6999 and effective date is on the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective))
      val land = landWithCode("6999")

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6999 and effective date is after the Wales Act effective date" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("6999")

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6996" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6996")

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6998" in {
      val ua   = answersWith(effectiveDate = Some(welshActEffective.minusDays(1)))
      val land = landWithCode("6998")

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }
  }


  "Cf11_Welsh6999ContractDate" - {

    "must fire when code is 6999 and contract date is after the Wales Act date (17/12/2014)" in {
      val ua   = answersWith(contractDate = Some(welshActDate.plusDays(1)))
      val land = landWithCode("6999")

      Cf11_Welsh6999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-11")
    }

    "must fire when code is 6999 and contract date is missing" in {
      val ua   = answersWith(contractDate = None)
      val land = landWithCode("6999")

      Cf11_Welsh6999ContractDate.validate(land, ua).map(_.ruleId) mustBe None
    }

    "must pass when code is 6999 and contract date is on the Wales Act date (17/12/2014)" in {
      val ua   = answersWith(contractDate = Some(welshActDate))
      val land = landWithCode("6999")

      Cf11_Welsh6999ContractDate.validate(land, ua) mustBe None
    }

    "must pass when code is 6999 and contract date is before the Wales Act date" in {
      val ua   = answersWith(contractDate = Some(welshActDate.minusDays(1)))
      val land = landWithCode("6999")

      Cf11_Welsh6999ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6996" in {
      val ua   = answersWith(contractDate = Some(welshActDate.plusDays(1)))
      val land = landWithCode("6996")

      Cf11_Welsh6999ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 6998" in {
      val ua   = answersWith(contractDate = Some(welshActDate.plusDays(1)))
      val land = landWithCode("6998")

      Cf11_Welsh6999ContractDate.validate(land, ua) mustBe None
    }
  }


  "F17Rules.all" - {

    "must contain all six F17 rules" in {
      F17Rules.all.map(_.id) must contain allOf (
        "Cf-8",
        "Cf-9a",
        "Cf-9b",
        "Cf-9c",
        "Cf-10",
        "Cf-11"
      )
    }

    "must produce no failures for a baseline (English code post-2018)" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)), contractDate = Some(LocalDate.of(2024, 1, 1)))
      val land = landWithCode("5900")

      F17Rules.all.flatMap(_.validate(land, ua)) mustBe empty
    }

    "must produce a failure when the land has 6996 and effective date is pre-2018" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2017, 6, 1)))
      val land = landWithCode("6996")

      F17Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId) must contain ("Cf-9a")
    }

    "must produce a failure when the land has a regular Welsh code (6810) and effective date is post-2018" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("6810")

      F17Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId) must contain ("Cf-8")
    }

    "must produce both eff-date and contract-date failures for 6998 when both dates are wrong" in {
      val ua = answersWith(
        effectiveDate = Some(welshActEffective.minusDays(1)),
        contractDate  = Some(welshActEffective.plusDays(1))
      )
      val land = landWithCode("6998")

      val ids = F17Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId)
      ids must contain allOf ("Cf-9b", "Cf-10")
    }

    "must produce both eff-date and contract-date failures for 6999 when both dates are wrong" in {
      val ua = answersWith(
        effectiveDate = Some(welshActEffective.minusDays(1)),
        contractDate  = Some(welshActDate.plusDays(1))
      )
      val land = landWithCode("6999")

      val ids = F17Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId)
      ids must contain allOf ("Cf-9c", "Cf-11")
    }
  }
}