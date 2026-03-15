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

package models.taxCalculation

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

import java.time.LocalDate

class SdltCalculationRequestSpec extends AnyFreeSpec with Matchers {

  private val effectiveDateDay   = 1
  private val effectiveDateMonth = 2
  private val effectiveDateYear  = 2015
  private val effectiveDate      = LocalDate.of(effectiveDateYear, effectiveDateMonth, effectiveDateDay)

  private val premium     = 100000
  private val highestRent = 0

  private val leaseStartDay   = 1
  private val leaseStartMonth = 3
  private val leaseStartYear  = 2015
  private val leaseEndDay     = 1
  private val leaseEndMonth   = 3
  private val leaseEndYear    = 2025
  private val leaseStart      = LocalDate.of(leaseStartYear, leaseStartMonth, leaseStartDay)
  private val leaseEnd        = LocalDate.of(leaseEndYear, leaseEndMonth, leaseEndDay)

  private val leaseTermYears             = 10
  private val leaseTermDays              = 5
  private val leaseTermDaysInPartialYear = 3

  private val year1Rent     = 500
  private val year2Rent     = 600
  private val year3Rent     = 700
  private val year4Rent     = 800
  private val year5Rent     = 900
  private val relevantRent  = 1000
  private val taxReliefCode = 22

  private val minimalRequest = SdltCalculationRequest(
    holdingType         = HoldingTypes.leasehold,
    propertyType        = PropertyTypes.residential,
    effectiveDate       = effectiveDate,
    nonUKResident       = None,
    premium             = premium,
    highestRent         = highestRent,
    propertyDetails     = None,
    leaseDetails        = None,
    relevantRentDetails = None,
    firstTimeBuyer      = None,
    isLinked            = None,
    interestTransferred = None,
    taxReliefDetails    = None,
    isMultipleLand      = None
  )

  private val minimalJson = Json.obj(
    "holdingType"        -> "leasehold",
    "propertyType"       -> "Residential",
    "effectiveDateDay"   -> effectiveDateDay,
    "effectiveDateMonth" -> effectiveDateMonth,
    "effectiveDateYear"  -> effectiveDateYear,
    "premium"            -> premium,
    "highestRent"        -> highestRent
  )

  "SdltCalculationRequest" - {

    ".writes" - {

      "must serialize a minimal request" in {
        Json.toJson(minimalRequest) mustBe minimalJson
      }

      "must serialize nonUKResident true as 'Yes'" in {
        Json.toJson(minimalRequest.copy(nonUKResident = Some(true))) mustBe
          minimalJson ++ Json.obj("nonUKResident" -> "Yes")
      }

      "must serialize nonUKResident false as 'No'" in {
        Json.toJson(minimalRequest.copy(nonUKResident = Some(false))) mustBe
          minimalJson ++ Json.obj("nonUKResident" -> "No")
      }

      "must serialize firstTimeBuyer true as 'Yes'" in {
        Json.toJson(minimalRequest.copy(firstTimeBuyer = Some(true))) mustBe
          minimalJson ++ Json.obj("firstTimeBuyer" -> "Yes")
      }

      "must serialize firstTimeBuyer false as 'No'" in {
        Json.toJson(minimalRequest.copy(firstTimeBuyer = Some(false))) mustBe
          minimalJson ++ Json.obj("firstTimeBuyer" -> "No")
      }

      "must serialize freehold holding type" in {
        Json.toJson(minimalRequest.copy(holdingType = HoldingTypes.freehold)) mustBe
          minimalJson ++ Json.obj("holdingType" -> "freehold")
      }

      "must serialize NonResidential property type" in {
        Json.toJson(minimalRequest.copy(propertyType = PropertyTypes.nonResidential)) mustBe
          minimalJson ++ Json.obj("propertyType" -> "NonResidential")
      }

      "must serialize Mixed property type" in {
        Json.toJson(minimalRequest.copy(propertyType = PropertyTypes.mixed)) mustBe
          minimalJson ++ Json.obj("propertyType" -> "Mixed")
      }

      "must serialize propertyDetails when present" in {
        val details = PropertyDetails(
          individual           = false,
          twoOrMoreProperties  = Some(true),
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        )
        Json.toJson(minimalRequest.copy(propertyDetails = Some(details))) mustBe
          minimalJson ++ Json.obj(
            "propertyDetails" -> Json.obj(
              "individual"          -> "No",
              "twoOrMoreProperties" -> "Yes"
            )
          )
      }

      "must serialize a fully populated request" in {

        val request = minimalRequest.copy(
          nonUKResident       = Some(false),
          firstTimeBuyer      = Some(true),
          isLinked            = Some(false),
          interestTransferred = Some("FG"),
          isMultipleLand      = Some(true),
          propertyDetails     = Some(PropertyDetails(
            individual           = true,
            twoOrMoreProperties  = Some(false),
            replaceMainResidence = Some(true),
            sharedOwnership      = Some(false),
            currentValue         = Some(true)
          )),
          leaseDetails = Some(LeaseDetails(
            startDate = leaseStart,
            endDate   = leaseEnd,
            leaseTerm = LeaseTerm(years = leaseTermYears, days = leaseTermDays, daysInPartialYear = leaseTermDaysInPartialYear),
            year1Rent = year1Rent,
            year2Rent = Some(year2Rent),
            year3Rent = Some(year3Rent),
            year4Rent = Some(year4Rent),
            year5Rent = Some(year5Rent)
          )),
          relevantRentDetails = Some(RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16     = Some(false),
            relevantRent                  = Some(relevantRent)
          )),
          taxReliefDetails = Some(TaxReliefDetails(
            taxReliefCode   = taxReliefCode,
            isPartialRelief = Some(true)
          ))
        )

        Json.toJson(request) mustBe Json.obj(
          "holdingType"        -> "leasehold",
          "propertyType"       -> "Residential",
          "effectiveDateDay"   -> effectiveDateDay,
          "effectiveDateMonth" -> effectiveDateMonth,
          "effectiveDateYear"  -> effectiveDateYear,
          "nonUKResident"      -> "No",
          "premium"            -> premium,
          "highestRent"        -> highestRent,
          "propertyDetails"    -> Json.obj(
            "individual"           -> "Yes",
            "twoOrMoreProperties"  -> "No",
            "replaceMainResidence" -> "Yes",
            "sharedOwnership"      -> "No",
            "currentValue"         -> "Yes"
          ),
          "leaseDetails" -> Json.obj(
            "startDateDay"   -> leaseStartDay,
            "startDateMonth" -> leaseStartMonth,
            "startDateYear"  -> leaseStartYear,
            "endDateDay"     -> leaseEndDay,
            "endDateMonth"   -> leaseEndMonth,
            "endDateYear"    -> leaseEndYear,
            "leaseTerm" -> Json.obj(
              "years"             -> leaseTermYears,
              "days"              -> leaseTermDays,
              "daysInPartialYear" -> leaseTermDaysInPartialYear
            ),
            "year1Rent" -> year1Rent,
            "year2Rent" -> year2Rent,
            "year3Rent" -> year3Rent,
            "year4Rent" -> year4Rent,
            "year5Rent" -> year5Rent
          ),
          "relevantRentDetails" -> Json.obj(
            "contractPre201603"        -> "Yes",
            "contractVariedPost201603" -> "No",
            "relevantRent"             -> relevantRent
          ),
          "firstTimeBuyer"      -> "Yes",
          "isLinked"            -> false,
          "interestTransferred" -> "FG",
          "taxReliefDetails" -> Json.obj(
            "taxReliefCode"   -> taxReliefCode,
            "isPartialRelief" -> true
          ),
          "isMultipleLand" -> true
        )
      }
    }
  }
}