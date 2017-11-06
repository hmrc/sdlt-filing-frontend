package calculation.models

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class RequestSpec extends UnitSpec {
  "LeaseTerm" should {
    "read from Json" in {
      val testJson = Json.parse(
        """{
          |  "years": 48,
          |  "days": 11,
          |  "daysInPartialYear": 5
          |}""".stripMargin)

      val model = LeaseTerm(years = 48, days = 11, daysInPartialYear = 5)

      Json.fromJson[LeaseTerm](testJson) shouldBe JsSuccess(model)
    }
  }

  "LeaseDetails" should {
    "read from Json" when {
      "all fields are defined" in  {
        val testJson = Json.parse(
          """
            |{
            |  "startDateDay": 15,
            |  "startDateMonth": 1,
            |  "startDateYear": 1949,
            |  "endDateDay": 31,
            |  "endDateMonth": 12,
            |  "endDateYear": 2049,
            |  "leaseTerm":  {
            |    "years": 33,
            |    "days": 0,
            |    "daysInPartialYear": 0
            |   },
            |  "year1Rent": 10000,
            |  "year2Rent": 20000,
            |  "year3Rent": 30000,
            |  "year4Rent": 40000,
            |  "year5Rent": 50000
            |}
          """.stripMargin)

        val model = LeaseDetails(
          startDate = LocalDate.of(1949, 1, 15),
          endDate = LocalDate.of(2049, 12,  31),
          leaseTerm = LeaseTerm(
            years = 33,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 10000,
          year2Rent = Some(20000),
          year3Rent = Some(30000),
          year4Rent = Some(40000),
          year5Rent = Some(50000)
        )

        Json.fromJson[LeaseDetails](testJson) shouldBe JsSuccess(model)
      }

      "only one rent field is defined" in  {
        val testJson = Json.parse(
          """
            |{
            |  "startDateDay": 15,
            |  "startDateMonth": 1,
            |  "startDateYear": 1949,
            |  "endDateDay": 31,
            |  "endDateMonth": 12,
            |  "endDateYear": 2049,
            |  "leaseTerm":  {
            |    "years": 33,
            |    "days": 0,
            |    "daysInPartialYear": 0
            |   },
            |  "year1Rent": 10000
            |}
          """.stripMargin)

        val model = LeaseDetails(
          startDate = LocalDate.of(1949, 1, 15),
          endDate = LocalDate.of(2049, 12,  31),
          leaseTerm = LeaseTerm(
            years = 33,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 10000,
          year2Rent = None,
          year3Rent = None,
          year4Rent = None,
          year5Rent = None
        )

        Json.fromJson[LeaseDetails](testJson) shouldBe JsSuccess(model)
      }
    }
  }

  "PropertyDetails" should {
    "read from Json with full details" in {
      val testJson = Json.parse(
        """
          |{
          |  "individual": "Yes",
          |  "twoOrMoreProperties": "No",
          |  "replaceMainResidence": "Yes"
          |}
        """.stripMargin)

      val model = PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(true)
      )

      Json.fromJson[PropertyDetails](testJson) shouldBe JsSuccess(model)
    }

    "read from Json with partial details" in {
      val testJson = Json.parse(
        """
          |{
          |  "individual": "No"
          |}
        """.stripMargin)

      val model = PropertyDetails(
        individual = false,
        twoOrMoreProperties = None,
        replaceMainResidence = None
      )

      Json.fromJson[PropertyDetails](testJson) shouldBe JsSuccess(model)
    }
  }

  "RelevantRentDetails" should {
    "read from Json with full details" in {
      val testJson = Json.parse(
        """
          |{
          |  "contractPre201603": "Yes",
          |  "contractVariedPost201603": "No",
          |  "relevantRent": 15438
          |}
        """.stripMargin)

      val model = RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(true),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(15438)
      )

      Json.fromJson[RelevantRentDetails](testJson) shouldBe JsSuccess(model)
    }

    "read from Json with partial details" in {
      val testJson = Json.parse(
        """
          |{
          |  "contractPre201603": "No"
          |}
        """.stripMargin)

      val model = RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(false),
        contractChangedSinceMar16 = None,
        relevantRent = None
      )

      Json.fromJson[RelevantRentDetails](testJson) shouldBe JsSuccess(model)
    }
  }


  "Request" should {
    "read from full Json" in {
      val testJson = Json.parse(
        """
          |{
          |  "holdingType": "Leasehold",
          |  "propertyType": "Residential",
          |  "effectiveDateDay": 13,
          |  "effectiveDateMonth": 7,
          |  "effectiveDateYear": 2017,
          |  "premium": 500000,
          |  "highestRent": 50000,
          |  "propertyDetails": {
          |    "individual": "Yes",
          |    "twoOrMoreProperties": "Yes",
          |    "replaceMainResidence": "Yes"
          |  },
          |  "leaseDetails": {
          |    "startDateDay": 15,
          |    "startDateMonth": 1,
          |    "startDateYear": 1949,
          |    "endDateDay": 31,
          |    "endDateMonth": 12,
          |    "endDateYear": 2049,
          |    "leaseTerm":  {
          |      "years": 33,
          |      "days": 0,
          |      "daysInPartialYear": 0
          |     },
          |    "year1Rent": 10000,
          |    "year2Rent": 20000,
          |    "year3Rent": 30000,
          |    "year4Rent": 40000,
          |    "year5Rent": 50000
          |  },
          |  "relevantRentDetails": {
          |    "contractPre201603": "Yes",
          |    "contractVariedPost201603": "No",
          |    "relevantRent": 1000
          |  }
          |}
        """.stripMargin)

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2017, 7, 13),
        premium = 500000,
        highestRent = 50000,
        propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(true)
        )),
        leaseDetails = Some(LeaseDetails(
          startDate = LocalDate.of(1949, 1, 15),
          endDate = LocalDate.of(2049, 12,  31),
          leaseTerm = LeaseTerm(
            years = 33,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 10000,
          year2Rent = Some(20000),
          year3Rent = Some(30000),
          year4Rent = Some(40000),
          year5Rent = Some(50000)
        )),
        relevantRentDetails = Some(RelevantRentDetails(
          exchangedContractsBeforeMar16 = Some(true),
          contractChangedSinceMar16 = Some(false),
          relevantRent = Some(1000)
        ))
      )

      Json.fromJson[Request](testJson) shouldBe JsSuccess(model)
    }

    "read from Json with no lease details or relevant rent details" in {
      val testJson = Json.parse(
        """
          |{
          |  "holdingType": "Leasehold",
          |  "propertyType": "Residential",
          |  "effectiveDateDay": 13,
          |  "effectiveDateMonth": 7,
          |  "effectiveDateYear": 2017,
          |  "premium": 500000,
          |  "highestRent": 50000,
          |  "propertyDetails": {
          |    "individual": "Yes",
          |    "twoOrMoreProperties": "Yes",
          |    "replaceMainResidence": "Yes"
          |  }
          |}
        """.stripMargin)

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2017, 7, 13),
        premium = 500000,
        highestRent = 50000,
        propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(true)
        )),
        leaseDetails = None,
        relevantRentDetails = None
      )

      Json.fromJson[Request](testJson) shouldBe JsSuccess(model)
    }

    "read from Json with no property details" in {
      val testJson = Json.parse(
        """
          |{
          |  "holdingType": "Freehold",
          |  "propertyType": "Residential",
          |  "effectiveDateDay": 13,
          |  "effectiveDateMonth": 7,
          |  "effectiveDateYear": 2017,
          |  "premium": 500000,
          |  "highestRent": 50000
          |}
        """.stripMargin)

      val model = Request(
        holdingType = HoldingTypes.freehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2017, 7, 13),
        premium = 500000,
        highestRent = 50000,
        propertyDetails = None,
        leaseDetails = None,
        relevantRentDetails = None
      )

      Json.fromJson[Request](testJson) shouldBe JsSuccess(model)
    }
  }
}
