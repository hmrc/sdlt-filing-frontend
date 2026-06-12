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

class F18RulesSpec extends SpecBase with Matchers {

  private val cr223Effective  = LocalDate.of(2015, 4, 1)
  private val scotlandActDate = LocalDate.of(2012, 5, 1)

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

  private def landWithPostcode(postcode: String): Land =
    Land(landID = Some("LND001"), postcode = Some(postcode))

  "Cf12_Dummy8998_8999EffDate" - {

    "must fire when code is 8998 and effective date is before CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("8998")

      Cf12_Dummy8998_8999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-12")
    }

    "must fire when code is 8999 and effective date is before CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("8999")

      Cf12_Dummy8998_8999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-12")
    }

    "must pass when code is 8998 and effective date is missing (None is valid)" in {
      val ua   = answersWith(effectiveDate = None)
      val land = landWithCode("8998")

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 8998 and effective date is on CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective))
      val land = landWithCode("8998")

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must pass when code is 8999 and effective date is after CR223" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("8999")

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Welsh code (6810)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("6810")

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Scottish-pattern code (9001)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("9001")

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must not apply when localAuthorityNumber is missing" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = Land(landID = Some("LND001"), localAuthorityNumber = None)

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }
  }

  "Cf13_Dummy8999ContractDate" - {

    "must fire when code is 8999 and contract date is on the Scotland Act date" in {
      val ua   = answersWith(contractDate = Some(scotlandActDate))
      val land = landWithCode("8999")

      Cf13_Dummy8999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-13")
    }

    "must fire when code is 8999 and contract date is after the Scotland Act date" in {
      val ua   = answersWith(contractDate = Some(scotlandActDate.plusDays(1)))
      val land = landWithCode("8999")

      Cf13_Dummy8999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-13")
    }

    "must pass when code is 8999 and contract date is missing (None is valid)" in {
      val ua   = answersWith(contractDate = None)
      val land = landWithCode("8999")

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }

    "must pass when code is 8999 and contract date is before the Scotland Act date" in {
      val ua   = answersWith(contractDate = Some(scotlandActDate.minusDays(1)))
      val land = landWithCode("8999")

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 8998" in {
      val ua   = answersWith(contractDate = Some(scotlandActDate.plusDays(1)))
      val land = landWithCode("8998")

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Welsh code" in {
      val ua   = answersWith(contractDate = Some(scotlandActDate.plusDays(1)))
      val land = landWithCode("6810")

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf14_Dummy8998ContractDate" - {

    "must fire when code is 8998 and contract date is on CR223" in {
      val ua   = answersWith(contractDate = Some(cr223Effective))
      val land = landWithCode("8998")

      Cf14_Dummy8998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-14")
    }

    "must fire when code is 8998 and contract date is after CR223" in {
      val ua   = answersWith(contractDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("8998")

      Cf14_Dummy8998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-14")
    }

    "must pass when code is 8998 and contract date is missing (None is valid)" in {
      val ua   = answersWith(contractDate = None)
      val land = landWithCode("8998")

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }

    "must pass when code is 8998 and contract date is before CR223" in {
      val ua   = answersWith(contractDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("8998")

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is 8999" in {
      val ua   = answersWith(contractDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("8999")

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Welsh code" in {
      val ua   = answersWith(contractDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("6810")

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf15_ScottishCodes" - {

    "must fire when code matches Scottish pattern (9001) and effective date is on CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective))
      val land = landWithCode("9001")

      Cf15_ScottishCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-15")
    }

    "must fire when code matches Scottish pattern (9500) and effective date is after CR223" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("9500")

      Cf15_ScottishCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-15")
    }

    "must pass when code matches Scottish pattern (9999) and effective date is missing (None is valid)" in {
      val ua = answersWith(effectiveDate = None)
      val land = landWithCode("9999")

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must pass when code matches Scottish pattern and effective date is before CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithCode("9001")

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must not apply when the code is a dummy code (8999)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("8999")

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must not apply when the code is a Welsh code (6810)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("6810")

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must not apply when the code is English (5900)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithCode("5900")

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must not apply when localAuthorityNumber is missing" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = Land(landID = Some("LND001"), localAuthorityNumber = None)

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }
  }

  "Cf16_ScottishPostcode" - {

    "must fire when postcode is in Edinburgh (EH1 1AA) and effective date is on CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective))
      val land = landWithPostcode("EH1 1AA")

      Cf16_ScottishPostcode.validate(land, ua).map(_.ruleId) mustBe Some("Cf-16")
    }

    "must fire when postcode is in Glasgow (G1 1AA) and effective date is on CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective))
      val land = landWithPostcode("G1 1AA")

      Cf16_ScottishPostcode.validate(land, ua).map(_.ruleId) mustBe Some("Cf-16")
    }

    "must pass when postcode is Scottish and effective date is missing (None is valid)" in {
      val ua = answersWith(effectiveDate = None)
      val land = landWithPostcode("EH1 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must pass when postcode is Scottish but effective date is before CR223" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.minusDays(1)))
      val land = landWithPostcode("EH1 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when postcode is in London (SW1A 1AA)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithPostcode("SW1A 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when postcode is in Manchester (M1 1AA)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithPostcode("M1 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when postcode is in Cardiff (CF10 1AA)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithPostcode("CF10 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when postcode is in Belfast (BT1 1AA)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithPostcode("BT1 1AA")

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when postcode is missing" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = Land(landID = Some("LND001"), postcode = None)

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must handle lowercase postcodes by treating them as Scottish (EH1 1AA → eh1 1aa)" in {
      val ua   = answersWith(effectiveDate = Some(cr223Effective.plusDays(1)))
      val land = landWithPostcode("eh1 1aa")

      Cf16_ScottishPostcode.validate(land, ua).map(_.ruleId) mustBe Some("Cf-16")
    }
  }

  "F18Rules.all" - {

    "must contain all five F18 rules" in {
      F18Rules.all.map(_.id) must contain allOf (
        "Cf-12",
        "Cf-13",
        "Cf-14",
        "Cf-15",
        "Cf-16"
      )
    }

    "must produce no failures for a baseline (English land, post-CR223)" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)), contractDate = Some(LocalDate.of(2024, 1, 1)))
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("5900"), postcode = Some("SW1A 1AA"))

      F18Rules.all.flatMap(_.validate(land, ua)) mustBe empty
    }

    "must produce a failure when the land has a Scottish code and effective date is on/after CR223" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithCode("9001")

      F18Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId) must contain ("Cf-15")
    }

    "must produce failures from both code-based and postcode-based rules when both conditions match" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = Land(
        landID = Some("LND001"),
        localAuthorityNumber = Some("9001"),
        postcode = Some("EH1 1AA")
      )

      val ids = F18Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId)
      ids must contain allOf ("Cf-15", "Cf-16")
    }
  }
}