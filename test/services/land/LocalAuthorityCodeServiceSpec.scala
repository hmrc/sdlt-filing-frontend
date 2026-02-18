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

package services.land

import base.SpecBase
import constants.FullReturnConstants.*
import models.UserAnswers
import models.address.{Address, Country}
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.land.LandAddressPage

import java.time.LocalDate

class LocalAuthorityCodeServiceSpec extends SpecBase {

  val testAddress: Address = Address(
    "16 Coniston Court",
    Some("Holland road"),
    None,
    None,
    None,
    Some("RG1 7NQ"),
    Some(Country(Some("UK"), Some("United Kingdom"))),
    false
  )

  val userAnswersWithDatesAndAddress: UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(transaction = Some(completeTransaction))))
      .set(LandAddressPage, testAddress).success.value

  val userAnswersWithoutDatesAndAddress: UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(transaction = None)))

  val mockLocalAuthorityCodeService = new LocalAuthorityCodeService()

  "LocalAuthorityCodeService" - {
    ".prepareFormContext" - {
      "must return LocalAuthorityFormContext with all fields populated when dates exist in full return and address exist in session" in {
        mockLocalAuthorityCodeService.prepareFormContext(userAnswersWithDatesAndAddress) shouldBe
          LocalAuthorityFormContext(
            effectiveTransactionDate = Some(LocalDate.parse("2024-10-01")) ,
            contractEffectiveDate = Some(LocalDate.parse("2024-09-15")),
            landPostcode = Some("RG1 7NQ")
          )
      }

      "must return LocalAuthorityFormContext with no fields populated when dates in full return do not exist and address do not exist in session" in {
        mockLocalAuthorityCodeService.prepareFormContext(userAnswersWithoutDatesAndAddress) shouldBe
          LocalAuthorityFormContext(
            effectiveTransactionDate = None ,
            contractEffectiveDate = None,
            landPostcode = None
          )
      }
    }
  }
}
