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

package services.lease

import base.SpecBase
import models.{FullReturn, Land, UserAnswers}
import models.lease.TypeOfLease
import org.scalatest.matchers.must.Matchers
import services.lease.LeaseService.{InvalidMixedRule, InvalidNonResidentialRule, InvalidResidentialRule, Valid}

class LeaseServiceSpec extends SpecBase with Matchers {
  private val service = new LeaseService()

  def userAnswersWithPropertyTypes(types: Seq[String]): UserAnswers = {
    val lands = types.map(t => Land(propertyType = Some(t)))

    val fullReturn = FullReturn(
      stornId = "1",
      returnResourceRef = "ref",
      land = Some(lands)
    )

    emptyUserAnswers.copy(fullReturn = Some(fullReturn))
  }

  "leasePropertyLandPropertyValidation" - {

    "must return Valid when no land present in full return" in {
      val result = service.leasePropertyLandPropertyValidation(emptyUserAnswers, TypeOfLease.R)
      result mustBe Valid
    }

    "must return Valid when lease type is Residential and property type is '01 - Residential'" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("01"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.R)
      result mustBe Valid
    }

    "must return Valid when lease type is Residential and property type is '04 - Additional Residential'" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("04"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.R)
      result mustBe Valid
    }

    "must return Valid when lease type is Non-Residential and property type is '03 - Non-Residential'" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("03"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.N)
      result mustBe Valid
    }

    "must return Valid when lease type is Mixed and property type is '02 - Mixed'" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("02"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.M)
      result mustBe Valid
    }

    "must return InvalidResidentialRule when Residential lease does not match property type" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("02"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.R)
      result mustBe InvalidResidentialRule
    }

    "must return InvalidNonResidentialRule when Non-Residential lease does not match property type" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("01"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.N)
      result mustBe InvalidNonResidentialRule
    }

    "must return InvalidMixedRule when Mixed lease does not match property type" in {
      val userAnswers = userAnswersWithPropertyTypes(Seq("01"))
      val result = service.leasePropertyLandPropertyValidation(userAnswers, TypeOfLease.M)
      result mustBe InvalidMixedRule
    }
  }
}
