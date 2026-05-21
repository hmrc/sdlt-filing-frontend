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

package utils

import base.SpecBase
import models.{FullReturn, Land, Transaction}
import utils.PropertyTypeHelper.isResidentialProperty

class PropertyTypeHelperSpec extends SpecBase {

  private val baseFullReturn = FullReturn(
    stornId           = "TESTSTORN",
    returnResourceRef = "REF001"
  )

  private def landWithType(propertyType: String): Land =
    Land(propertyType = Some(propertyType))

  private def withLand(types: String*): FullReturn =
    baseFullReturn.copy(land = Some(types.map(landWithType)))

  private def withTransaction(date: String): Option[Transaction] =
    Some(Transaction(effectiveDate = Some(date)))

  "PropertyTypeHelper.isResidentialProperty" - {

    "land property type check" - {

      "must return true when a single land entry is residential ('01')" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when a single land entry is additional residential ('04')" in {
        val fullReturn = withLand("04").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when at least one of multiple land entries is residential" in {
        val fullReturn = withLand("03", "01", "02").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return false when no land entry is residential" in {
        val fullReturn = withLand("02", "03").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when the land sequence is empty" in {
        val fullReturn = baseFullReturn.copy(
          land        = Some(Seq.empty),
          transaction = withTransaction("2024-06-12")
        )
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when land is None" in {
        val fullReturn = baseFullReturn.copy(
          land        = None,
          transaction = withTransaction("2024-06-12")
        )
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when a land entry has no property type" in {
        val fullReturn = baseFullReturn.copy(
          land        = Some(Seq(Land(propertyType = None))),
          transaction = withTransaction("2024-06-12")
        )
        isResidentialProperty(fullReturn) mustBe false
      }

      "must ignore unrecognised property type codes" in {
        val fullReturn = withLand("99", "XX").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe false
      }
    }

    "effective date check" - {

      "must return true when the effective date is after the cutoff (ISO format)" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the effective date is after the cutoff (UK format)" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("12/06/2024"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the effective date is exactly on the cutoff (1 April 2021)" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2021-04-01"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the effective date is on the cutoff in UK format" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("01/04/2021"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return false when the effective date is before the cutoff" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2020-12-15"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when the effective date is the day before the cutoff" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2021-03-31"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return true when the effective date is missing (no transaction)" in {
        val fullReturn = withLand("01").copy(transaction = None)
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the transaction has no effective date" in {
        val fullReturn = withLand("01").copy(transaction = Some(Transaction(effectiveDate = None)))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the effective date is an empty string" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction(""))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return true when the effective date is whitespace only" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("   "))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return false when the effective date string is unparseable" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("not-a-date"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must trim whitespace before parsing the effective date" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("  2024-06-12  "))
        isResidentialProperty(fullReturn) mustBe true
      }
    }

    "combined land and date logic" - {

      "must return true when land is residential and date is after cutoff" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe true
      }

      "must return false when land is residential but date is before cutoff" in {
        val fullReturn = withLand("01").copy(transaction = withTransaction("2020-01-01"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when land is non-residential even if date is after cutoff" in {
        val fullReturn = withLand("02").copy(transaction = withTransaction("2024-06-12"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must return false when both land is non-residential and date is before cutoff" in {
        val fullReturn = withLand("02").copy(transaction = withTransaction("2020-01-01"))
        isResidentialProperty(fullReturn) mustBe false
      }

      "must reflect the live data scenario: land '01' with effective date '12/06/2024'" in {
        val realLand = Seq(
          Land(landID = Some("382925780"), propertyType = Some("01"), postcode = Some("M43 6UR")),
          Land(landID = Some("382925699"), propertyType = Some("03"), postcode = Some("M43 6UR"))
        )
        val fullReturn = baseFullReturn.copy(
          land        = Some(realLand),
          transaction = withTransaction("12/06/2024")
        )
        isResidentialProperty(fullReturn) mustBe true
      }
    }
  }
}