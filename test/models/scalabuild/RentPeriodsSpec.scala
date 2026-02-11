/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild
import models.LeaseTerm
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class RentPeriodsSpec extends PlaySpec{
  "Rent Period" must {
    "be readable from the database" when {
      "all fields are defined" in {
        val testJson = Json.parse("""{
                                    |    "year1Rent": 10000,
                                    |    "year2Rent": 20000,
                                    |    "year3Rent": 30000,
                                    |    "year4Rent": 40000,
                                    |    "year5Rent": 50000
                                    |}""".stripMargin)
        val model = RentPeriods(
          year1Rent = 10000,
          year2Rent = Some(20000),
          year3Rent = Some(30000),
          year4Rent = Some(40000),
          year5Rent = Some(50000)
        )
        Json.fromJson[RentPeriods](testJson) shouldBe JsSuccess(model)
      }
      "one fields is defined" in {
        val testJson = Json.parse("""{
                                    |    "year1Rent": 10000
                                    |}""".stripMargin)
        val model = RentPeriods(
          year1Rent = 10000
        )
        Json.fromJson[RentPeriods](testJson) shouldBe JsSuccess(model)
      }
      "some fields are defined" in {
        val testJson = Json.parse("""{
                                    |    "year1Rent": 10000,
                                    |    "year2Rent": 20000,
                                    |    "year3Rent": 30000
                                    |}""".stripMargin)

        val model = RentPeriods(
          year1Rent = 10000,
          year2Rent = Some(20000),
          year3Rent = Some(30000)
        )

        Json.fromJson[RentPeriods](testJson) shouldBe JsSuccess(model)

      }
    }
  }
  "Lease Context" must {
    "be readable from the database" in {
      val testJson = Json.parse("""{
                                  |    "years": 33,
                                  |    "days": 0,
                                  |    "daysInPartialYear": 0
                                  |}""".stripMargin)

      val model = LeaseTerm(
        years = 33,
        days = 0,
        daysInPartialYear = 0
      )
      Json.fromJson[LeaseTerm](testJson) shouldBe JsSuccess(model)
    }
  }
}
