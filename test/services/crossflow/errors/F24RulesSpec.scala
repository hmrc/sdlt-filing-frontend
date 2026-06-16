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
import pages.transaction.TransactionEffectiveDatePage
import services.crossflow.*

import java.time.LocalDate

class F24RulesSpec extends SpecBase with Matchers {

  private val floor = LocalDate.of(2016, 4, 1)

  private def answersWith(
                           effectiveDate: Option[LocalDate] = None
                         ): UserAnswers = {
    val committedTransaction = Transaction(
      effectiveDate = effectiveDate.map(_.toString)
    )

    val base = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(committedTransaction)
    )))

    effectiveDate.fold(base)(d => base.set(TransactionEffectiveDatePage, d).success.value)
  }

  private def landWithType(propertyType: String): Land =
    Land(landID = Some("LND001"), propertyType = Some(propertyType))


  "F24AdditionalResidentialEffDate" - {

    "must fire when property type is 04 and effective date is the day before the floor" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-3")
    }

    "must fire when property type is 04 and effective date is well before the floor" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2010, 1, 1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-3")
    }

    "must pass when property type is 04 and effective date is on the floor" in {
      val ua   = answersWith(effectiveDate = Some(floor))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must pass when property type is 04 and effective date is after the floor" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must pass when property type is 04 and effective date is missing (None is valid)" in {
      val ua   = answersWith(effectiveDate = None)
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must not apply when property type is Residential (01)" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("01")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must not apply when property type is Mixed (02)" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("02")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must not apply when property type is Non-residential (03)" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("03")

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must not apply when propertyType is missing" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = Land(landID = Some("LND001"), propertyType = None)

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must fire regardless of whether the user is claiming relief (F24 is independent of relief claim)" in {
      // The baseline answersWith doesn't set any relief flags — the rule still fires
      // because F24 is a property-type + date validity rule, not a relief eligibility rule.
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-3")
    }

    "must return a failure with the correct rule metadata" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      val failure = F24AdditionalResidentialEffDate.validate(land, ua).value

      failure.ruleId     mustBe "Cf-3"
      failure.affects    mustBe ReturnSection.Land
      failure.messageKey mustBe "crossflow.land.Cf-3.body"
    }

    "must surface the override heading key on the failure" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua).value.headingKey mustBe "crossflow.land.Cf-3.heading"
    }

    "must surface the inline error key on the failure" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      F24AdditionalResidentialEffDate.validate(land, ua).value.inlineErrorKey mustBe "crossflow.land.Cf-3.inline"
    }

    "must target the property type page" in {
      val ua   = answersWith(effectiveDate = Some(floor.minusDays(1)))
      val land = landWithType("04")

      val failure = F24AdditionalResidentialEffDate.validate(land, ua).value

      failure.targets.map(_.page) must contain (Pages.LandPropertyType)
    }
  }


  "F24Rules.all" - {

    "must contain Cf-3" in {
      F24Rules.all.map(_.id) mustBe Set("Cf-3")
    }

    "must produce no failures for a baseline (English residential land, post-2016)" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2024, 6, 1)))
      val land = Land(landID = Some("LND001"), propertyType = Some("01"))

      F24Rules.all.flatMap(_.validate(land, ua)) mustBe empty
    }

    "must produce a Cf-3 failure for the canonical broken case (property type 04 + pre-2016 effective date)" in {
      val ua   = answersWith(effectiveDate = Some(LocalDate.of(2014, 9, 15)))
      val land = landWithType("04")

      F24Rules.all.flatMap(_.validate(land, ua)).map(_.ruleId) must contain ("Cf-3")
    }
  }
}