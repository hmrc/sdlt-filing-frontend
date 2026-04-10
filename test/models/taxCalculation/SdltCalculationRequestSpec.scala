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

class SdltCalculationRequestSpec extends AnyFreeSpec with Matchers {

  private val minimalRequest = SdltCalculationRequest(
    holdingType         = HoldingTypes.leasehold,
    propertyType        = PropertyTypes.residential,
    effectiveDateDay    = 1,
    effectiveDateMonth  = 2,
    effectiveDateYear   = 2015,
    nonUKResident       = None,
    premium             = BigDecimal(100000),
    highestRent         = BigDecimal(0),
    propertyDetails     = None,
    leaseDetails        = None,
    relevantRentDetails = None,
    firstTimeBuyer      = None,
    isLinked            = None,
    interestTransferred = None,
    taxReliefDetails    = None,
    isMultipleLand      = None,
    declaredNpv         = None
  )

  private val minimalJson = Json.obj(
    "holdingType"        -> "leasehold",
    "propertyType"       -> "Residential",
    "effectiveDateDay"   -> 1,
    "effectiveDateMonth" -> 2,
    "effectiveDateYear"  -> 2015,
    "premium"            -> 100000,
    "highestRent"        -> 0
  )

  "SdltCalculationRequest" - {

    ".writes" - {

      "must serialize a minimal request" in {
        Json.toJson(minimalRequest) mustBe minimalJson
      }

      "must include nonUKResident in JSON when present" in {
        Json.toJson(minimalRequest.copy(nonUKResident = Some("Yes"))) mustBe
          minimalJson ++ Json.obj("nonUKResident" -> "Yes")
      }

      "must include firstTimeBuyer in JSON when present" in {
        Json.toJson(minimalRequest.copy(firstTimeBuyer = Some("Yes"))) mustBe
          minimalJson ++ Json.obj("firstTimeBuyer" -> "Yes")
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
          individual           = "No",
          twoOrMoreProperties  = Some("Yes"),
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
          nonUKResident       = Some("No"),
          firstTimeBuyer      = Some("Yes"),
          isLinked            = Some(false),
          interestTransferred = Some("FG"),
          isMultipleLand      = Some(true),
          propertyDetails     = Some(PropertyDetails(
            individual           = "Yes",
            twoOrMoreProperties  = Some("No"),
            replaceMainResidence = Some("Yes"),
            sharedOwnership      = Some("No"),
            currentValue         = Some("Yes")
          )),
          leaseDetails = Some(LeaseDetails(
            startDateDay   = 1,
            startDateMonth = 3,
            startDateYear  = 2015,
            endDateDay     = 1,
            endDateMonth   = 3,
            endDateYear    = 2025,
            leaseTerm      = LeaseTerm(years = 10, days = 5, daysInPartialYear = 3),
            year1Rent      = BigDecimal(500),
            year2Rent      = Some(BigDecimal(600)),
            year3Rent      = Some(BigDecimal(700)),
            year4Rent      = Some(BigDecimal(800)),
            year5Rent      = Some(BigDecimal(900))
          )),
          relevantRentDetails = Some(RelevantRentDetails(
            contractPre201603        = Some("Yes"),
            contractVariedPost201603 = Some("No"),
            relevantRent             = Some(BigDecimal(1000))
          )),
          taxReliefDetails = Some(TaxReliefDetails(
            taxReliefCode   = 22,
            isPartialRelief = Some(true)
          ))
        )

        val expectedPropertyDetails: JsObject = Json.obj(
          "individual"           -> "Yes",
          "twoOrMoreProperties"  -> "No",
          "replaceMainResidence" -> "Yes",
          "sharedOwnership"      -> "No",
          "currentValue"         -> "Yes"
        )
        val expectedLeaseDetails: JsObject = Json.obj(
          "startDateDay"   -> 1,
          "startDateMonth" -> 3,
          "startDateYear"  -> 2015,
          "endDateDay"     -> 1,
          "endDateMonth"   -> 3,
          "endDateYear"    -> 2025,
          "leaseTerm"      -> Json.obj("years" -> 10, "days" -> 5, "daysInPartialYear" -> 3),
          "year1Rent"      -> 500,
          "year2Rent"      -> 600,
          "year3Rent"      -> 700,
          "year4Rent"      -> 800,
          "year5Rent"      -> 900
        )
        val expectedRelevantRentDetails: JsObject = Json.obj(
          "contractPre201603"        -> "Yes",
          "contractVariedPost201603" -> "No",
          "relevantRent"             -> 1000
        )
        val expectedTaxReliefDetails: JsObject = Json.obj(
          "taxReliefCode"   -> 22,
          "isPartialRelief" -> true
        )

        Json.toJson(request) mustBe Json.obj(
          "holdingType"         -> "leasehold",
          "propertyType"        -> "Residential",
          "effectiveDateDay"    -> 1,
          "effectiveDateMonth"  -> 2,
          "effectiveDateYear"   -> 2015,
          "nonUKResident"       -> "No",
          "premium"             -> 100000,
          "highestRent"         -> 0,
          "propertyDetails"     -> expectedPropertyDetails,
          "leaseDetails"        -> expectedLeaseDetails,
          "relevantRentDetails" -> expectedRelevantRentDetails,
          "firstTimeBuyer"      -> "Yes",
          "isLinked"            -> false,
          "interestTransferred" -> "FG",
          "taxReliefDetails"    -> expectedTaxReliefDetails,
          "isMultipleLand"      -> true
        )
      }
    }
  }
}
