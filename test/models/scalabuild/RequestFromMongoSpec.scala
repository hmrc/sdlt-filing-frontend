/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild
import enums.{HoldingTypes, PropertyTypes}
import models.{LeaseTerm, PropertyDetails}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class RequestFromMongoSpec extends PlaySpec {
  "RequestFromMongo" must {
    "read a full Request in the leasehold journey" in {
      val testJson = Json.parse("""
                                  |{
                                  |  "holdingType": "Leasehold",
                                  |  "propertyType": "Residential",
                                  |  "effectiveDate": "2025-01-01",
                                  |  "mongoLeaseDetails": {
                                  |      "leaseDates": {
                                  |        "startDate": "2025-01-01",
                                  |        "endDate": "2125-01-01"
                                  |      },
                                  |      "rentDetails": {
                                  |        "year1Rent": 10000,
                                  |        "year2Rent": 20000,
                                  |        "year3Rent": 30000,
                                  |        "year4Rent": 40000,
                                  |        "year5Rent": 50000
                                  |      },
                                  |      "leaseTerm": {
                                  |        "years": 100,
                                  |        "days": 1,
                                  |        "daysInPartialYear": 365
                                  |      }
                                  |  },
                                  |  "premium": 550000,
                                  |  "highestRent": 99000,
                                  |  "nonUKResident": false,
                                  |  "propertyDetails": {
                                  |     "individual": false
                                  |   }
                                  |}""".stripMargin)

      val model = RequestFromMongo(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2025, 1, 1),
        nonUKResident = Some(false),
        premium = 550000,
        mongoLeaseDetails = Some(
          MongoLeaseDetails(
            leaseDates = LeaseDates(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2125, 1, 1)),
            leaseTerm = LeaseTerm(
              years = 100,
              days = 1,
              daysInPartialYear = 365
            ),
            rentDetails = RentPeriods(
              year1Rent = 10000,
              year2Rent = Some(20000),
              year3Rent = Some(30000),
              year4Rent = Some(40000),
              year5Rent = Some(50000)
            )
          )
        ),
        propertyDetails = Some(
          PropertyDetails(
            individual = false,
            twoOrMoreProperties = None,
            replaceMainResidence = None,
            sharedOwnership = None,
            currentValue = None
          )
        ),
        relevantRentDetails = None,
        firstTimeBuyer = None
      )

      Json.fromJson[RequestFromMongo](testJson) shouldBe JsSuccess(model)

    }
    "read minimal Request in the Non-Residential journey" in {
      val testJson = Json.parse("""
                                  |{
                                  |  "holdingType": "Leasehold",
                                  |  "propertyType": "NonResidential",
                                  |  "effectiveDate": "2025-01-01",
                                  |  "premium": 550000
                                  |}""".stripMargin)

      val model = RequestFromMongo(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.nonResidential,
        effectiveDate = LocalDate.of(2025, 1, 1),
        nonUKResident = None,
        premium = 550000,
        mongoLeaseDetails = None,
        propertyDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = None
      )

      Json.fromJson[RequestFromMongo](testJson) shouldBe JsSuccess(model)

    }
    "read Lease Term from database" in {
      val testJson = Json.parse("""{
          |  "years": 48,
          |  "days": 11,
          |  "daysInPartialYear": 5
          |}""".stripMargin)

      val model = LeaseTerm(years = 48, days = 11, daysInPartialYear = 5)

      Json.fromJson[LeaseTerm](testJson) shouldBe JsSuccess(model)

    }
  }
  "LeaseDetails" must {
    "read from MongoLeaseDetails from database" when {
      "all fields are defined" in {
        val testJson = Json.parse("""
            |{
            |  "leaseDates":  {
            |    "startDate": "2025-01-01",
            |    "endDate": "2125-01-01"
            |  },  
            |  "leaseTerm":  {
            |    "years": 33,
            |    "days": 0,
            |    "daysInPartialYear": 0
            |   },
            |  "rentDetails":  {
            |    "year1Rent": 10000,
            |    "year2Rent": 20000,
            |    "year3Rent": 30000,
            |    "year4Rent": 40000,
            |    "year5Rent": 50000
            |  }  
            |}
          """.stripMargin)

        val model = MongoLeaseDetails(
          leaseDates = LeaseDates(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2125, 1, 1)),
          leaseTerm = LeaseTerm(
            years = 33,
            days = 0,
            daysInPartialYear = 0
          ),
          rentDetails = RentPeriods(
            year1Rent = 10000,
            year2Rent = Some(20000),
            year3Rent = Some(30000),
            year4Rent = Some(40000),
            year5Rent = Some(50000)
          )
        )

        Json.fromJson[MongoLeaseDetails](testJson) shouldBe JsSuccess(model)
      }

      "only one rent field is defined" in {
        val testJson = Json.parse("""
            |{
            |  "leaseDates":  {
            |    "startDate": "2025-01-01",
            |    "endDate": "2125-01-01"
            |  },
            |  "leaseTerm":  {
            |    "years": 33,
            |    "days": 0,
            |    "daysInPartialYear": 0
            |   },
            |  "rentDetails":  {
            |    "year1Rent": 10000
            |  }
            |}
          """.stripMargin)

        val model = MongoLeaseDetails(
          leaseDates = LeaseDates(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2125, 1, 1)),
          leaseTerm = LeaseTerm(
            years = 33,
            days = 0,
            daysInPartialYear = 0
          ),
          rentDetails = RentPeriods(
            year1Rent = 10000
          )
        )
        Json.fromJson[MongoLeaseDetails](testJson) shouldBe JsSuccess(model)
      }
    }
  }

}
