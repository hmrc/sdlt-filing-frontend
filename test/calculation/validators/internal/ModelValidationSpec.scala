package calculation.validators.internal

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models._
import calculation.services.BaseCalculationService
import uk.gov.hmrc.play.test.UnitSpec

class ModelValidationSpec extends UnitSpec {

  /*
  | HoldingType | PropertyType    | EffectiveDate           | Property Details Req'd | Lease Details Req'd |
  |---------------------------------------------------------|----------------------------------------------|
  | Freehold    | Residential     | < 22/03/2012            | INVALID                | INVALID             |
  | Freehold    | Residential     | 22/03/2012 - 31/03/3016 | No                     | No                  |
  | Freehold    | Residential     | > 31/03/3016            | Yes                    | No                  |
  | Freehold    | Non-Residential | Any Date                | No                     | No                  |
  | Leasehold   | Residential     | < 22/03/2012            | INVALID                | INVALID             |
  | Leasehold   | Residential     | 22/03/2012 - 31/03/3016 | No                     | Yes                 |
  | Leasehold   | Residential     | > 31/03/3016            | Yes                    | Yes                 |
  | Leasehold   | Residential     | Any Date                | No                     | Yes                 |
   */

  private val validPropertyDetails = PropertyDetails(
    individual = false,
    twoOrMoreProperties = None,
    replaceMainResidence = None
  )

  private val validTestLeaseDetails = LeaseDetails(
    startDate = LocalDate.of(2000, 1, 30),
    endDate = LocalDate.of(2099, 12, 31),
    leaseTerm = LeaseTerm(
      years = 85,
      days = 286,
      daysInPartialYear = 0
    ),
    year1Rent = 5000,
    year2Rent = Some(10000),
    year3Rent = Some(10000),
    year4Rent = Some(10000),
    year5Rent = Some(10000)
  )

  private val validTestLeaseDetailsAllLessTan2000 = LeaseDetails(
    startDate = LocalDate.of(2000, 1, 30),
    endDate = LocalDate.of(2099, 12, 31),
    leaseTerm = LeaseTerm(
      years = 83,
      days = 200,
      daysInPartialYear = 0
    ),
    year1Rent = 500,
    year2Rent = Some(1000),
    year3Rent = Some(1000),
    year4Rent = Some(1000),
    year5Rent = Some(1000)
  )

  import ModelValidation._

  "validLeaseDetails" should{
    "return a ValidationSuccess" when{
      "the Holding Type is Freehold" in{
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }

      "the Holding Type is leasehold & only the year1Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 1,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = None,
          year3Rent = None,
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }

      "the Holding Type is leasehold & only the year[1-5]Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 5,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = Some(5000),
          year4Rent = Some(5000),
          year5Rent = Some(5000)
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }

      "the Holding Type is leasehold & only the year[1-4]Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 4,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = Some(5000),
          year4Rent = Some(5000),
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }

      "the Holding Type is leasehold & only the year[1-3]Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 3,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = Some(5000),
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }

      "the Holding Type is leasehold & only the year[1-2]Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 2,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = None,
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationSuccess
      }
    }

    "return a ValidationFailure" when{
      "the Holding Type is leasehold & only the year1Rent and year3Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 2,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = None,
          year3Rent = Some(5000),
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationFailure("Lease details have been input incorrectly")
      }

      "the Holding Type is leasehold & only the year[1-2]Rent and year4Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 3,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = None,
          year4Rent = Some(5000),
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationFailure("Lease details have been input incorrectly")
      }

      "the Holding Type is leasehold & only the year[1-3]Rent and year5Rent has been applied" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 4,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = Some(5000),
          year4Rent = None,
          year5Rent = Some(5000)
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationFailure("Lease details have been input incorrectly")
      }
    }
  }

  "validLeaseTerm" should{
    "return a ValidationSuccess" when{
      "only the year1Rent has been applied and it is equal to the leaseTerm years" in{
         val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 1,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = None,
          year3Rent = None,
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2098, 12, 31),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        validLeaseTerm(model) shouldBe ValidationSuccess
      }

      "the year[1-2]Rent has been applied and it is equal to the leaseTerm years" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 2,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = None,
          year4Rent = None,
          year5Rent = None
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2097, 12, 31),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseTerm(model) shouldBe ValidationSuccess
      }

    "the year[1-5]Rent has been applied and the number of leaseTerm years is greater than 5" in{
      val tempLeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2000, 1, 30),
        endDate = LocalDate.of(2099, 12, 31),
        leaseTerm = LeaseTerm(
          years = 85,
          days = 286,
          daysInPartialYear = 0
        ),
        year1Rent = 5000,
        year2Rent = Some(5000),
        year3Rent = Some(5000),
        year4Rent = Some(5000),
        year5Rent = Some(5000)
      )

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2014, 3, 20),
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
          firstTimeBuyer = None
      )
      validLeaseTerm(model) shouldBe ValidationSuccess
    }

      "the year[1-5]Rent has been applied and the number of leaseTerm years is 4 and the leaseTerm partial days is 1" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 85,
            days = 286,
            daysInPartialYear = 1
          ),
          year1Rent = 5000,
          year2Rent = Some(5000),
          year3Rent = Some(5000),
          year4Rent = Some(5000),
          year5Rent = Some(5000)
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validLeaseTerm(model) shouldBe ValidationSuccess
      }
    }


  "return a ValidationFailure" when{
    "only the year1Rent has been applied and it is NOT equal to the leaseTerm years" in{
      val tempLeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2000, 1, 30),
        endDate = LocalDate.of(2099, 12, 31),
        leaseTerm = LeaseTerm(
          years = 2,
          days = 0,
          daysInPartialYear = 0
        ),
        year1Rent = 5000,
        year2Rent = None,
        year3Rent = None,
        year4Rent = None,
        year5Rent = None
      )

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2014, 3, 20),
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
          firstTimeBuyer = None
      )

      validLeaseTerm(model) shouldBe ValidationFailure("Lease term: 2 does not match amount of lease year rents: 1 and 0 partial days")
    }

    "the year[1-4]Rent has been applied and the number of leaseTerm years is greater than 5" in{
      val tempLeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2000, 1, 30),
        endDate = LocalDate.of(2099, 12, 31),
        leaseTerm = LeaseTerm(
          years = 7,
          days = 0,
          daysInPartialYear = 0
        ),
        year1Rent = 5000,
        year2Rent = Some(5000),
        year3Rent = Some(5000),
        year4Rent = Some(5000),
        year5Rent = None
      )

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2014, 3, 20),
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
          firstTimeBuyer = None
      )
      validLeaseTerm(model) shouldBe ValidationFailure("Lease term: 7 does not match amount of lease year rents: 4 and 0 partial days")
    }

    "the year[1-4]Rent has been applied and the number of leaseTerm years is 4 and the leaseTerm partial days is 1" in{
      val tempLeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2000, 1, 30),
        endDate = LocalDate.of(2099, 12, 31),
        leaseTerm = LeaseTerm(
          years = 4,
          days = 0,
          daysInPartialYear = 1
        ),
        year1Rent = 5000,
        year2Rent = Some(5000),
        year3Rent = Some(5000),
        year4Rent = Some(5000),
        year5Rent = None
      )

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2014, 3, 20),
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
          firstTimeBuyer = None
      )
      validLeaseTerm(model) shouldBe ValidationFailure("Lease term: 4 does not match amount of lease year rents: 4 and 1 partial days")
    }
   }
  }

  "validPropertyDetailsStructure" should {
    "Return a ValidationSuccess for a PropertyDetailsModel" when {
      "individual is 'false' and other fields are defined" in {
        val deets = PropertyDetails(
          individual = false,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true)
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationSuccess
      }

      "individual is 'false' and other fields are not defined" in {
        val deets = PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationSuccess
      }
      "individual is 'true', twoOrMoreProperties 'false' and replaceMainResidence not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationSuccess
      }
      "individual is 'true', twoOrMoreProperties 'true' and replaceMainResidence defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false)
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationSuccess
      }
    }
    "Return a correctly messaged ValidationFailure" when {
      "individual is true and twoOrMoreProperties is not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = None,
          replaceMainResidence = Some(false)
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationFailure(
          "Property details failed validation with 'individual': true, " +
            "'twoOrMoreProperties': None, " +
            "'replaceMainResidence': Some(false)"
        )
      }
      "individual is true, twoOrMoreProperties 'true' and replaceMainResidence not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = None
        )
        validPropertyDetailsStructure(deets) shouldBe ValidationFailure(
          "Property details failed validation with 'individual': true, " +
            "'twoOrMoreProperties': Some(true), " +
            "'replaceMainResidence': None"
        )
      }
    }
  }

  "allRentsBelow2000" should {
    def details(year2Rent: BigDecimal) = LeaseDetails(
      startDate = LocalDate.of(2000, 1, 31),
      endDate = LocalDate.of(2050, 1, 31),
      leaseTerm = LeaseTerm(
        years = 30, days = 0, daysInPartialYear = 365
      ),
      year1Rent = 1000,
      year2Rent = Some(year2Rent),
      year3Rent = Some(1999.99),
      year4Rent = Some(3),
      year5Rent = Some(200)
    )
    "return true when all rents are <2000" in {
      allRentsBelow2000(details(1999)) shouldBe true
    }

    "return false when there is a rent of 2000" in {
      allRentsBelow2000(details(2000)) shouldBe false
    }

    "return false when there is a rent of >2000" in {
      allRentsBelow2000(details(2001)) shouldBe false
    }
  }

  "validRelevantRentDetails" should {

    def validLeaseDetails(year2Rent: BigDecimal) = LeaseDetails(
      startDate = LocalDate.of(2000, 12, 31),
      endDate = LocalDate.of(2020, 12, 31),
      leaseTerm = LeaseTerm(
        years = 3, days = 0, daysInPartialYear = 365
      ),
      year1Rent = 1000,
      year2Rent = Some(year2Rent),
      year3Rent = Some(800),
      year4Rent = None,
      year5Rent = None
    )
    val testRelevantRentDetails = RelevantRentDetails(
      exchangedContractsBeforeMar16 = Some(false),
      contractChangedSinceMar16 = None,
      relevantRent = None
    )

    "return a ValidationSuccess" when {
      "relevant rent details are not defined" when {
        "holding type is freehold" in {
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "property type is residential" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            premium = 140000,
            highestRent = 1000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(800)),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "premium is >=£150000" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            premium = 150000,
            highestRent = 2000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(800)),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "there is a rent >= £2000" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            premium = 140000,
            highestRent = 2000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(2000)),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }
      }
      "leasehold, non-residential, premium is <£150000, all rents are <£2000 and relevant rent is defined" in {
        val request = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validLeaseDetails(800)),
          relevantRentDetails = Some(testRelevantRentDetails),
          firstTimeBuyer = None
        )
        validRelevantRentDetails(request) shouldBe ValidationSuccess
      }
    }
    "return the correct validation failure response" when {
      "lease details are not defined" in {
        val request = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = Some(testRelevantRentDetails),
          firstTimeBuyer = None
        )
        validRelevantRentDetails(request) shouldBe ValidationFailure("No lease details provided for leasehold property")
      }
      "relevant rent is not defined" in {
        val request = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validLeaseDetails(800)),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validRelevantRentDetails(request) shouldBe ValidationFailure(
          "Relevant rent details not provided when premium: 140000, " +
            "holding type: leasehold, property type: non-residential and all rents <£2000"
        )
      }
    }
  }

  "validRelevantRentDetailsStructure" should {
    val postMar2016Date = LocalDate.of(2016, 3, 17)
    val preMar2016Date  = LocalDate.of(2016, 3, 16)
    "return a ValidationSuccess" when {
      "effective date is on or after 17/03/2016" when {
        "contractPre201603 is false" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(false),
            contractChangedSinceMar16 = None,
            relevantRent = None
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationSuccess
        }
        "contractPre201603 is true and contractVariedPost201603 is true" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16 = Some(true),
            relevantRent = None
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationSuccess
        }
        "contractPre201603 is true and contractVariedPost201603 is false and relevant rent is defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16 = Some(false),
            relevantRent = Some(1000)
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationSuccess
        }
      }
      "effective date is before 17/03/2016" when {
        "relevant rent is defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = None,
            contractChangedSinceMar16 = None,
            relevantRent = Some(1000)
          )
          validRelevantRentDetailsStructure(testDetails, preMar2016Date) shouldBe ValidationSuccess
        }
      }
    }
    "return the correct validation failure response" when {
      "effective date is on or after 17/03/2016" when {
        "contractPre201603 is true and contractVariedPost201603 is false and relevant rent is not defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16 = Some(false),
            relevantRent = None
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationFailure(
            "Relevant Rent details failed validation with " +
              "'exchangedContractsBeforeMar16': Some(true), " +
              "'contractChangedSinceMar16': Some(false), " +
              "'relevantRent': None"
          )
        }
        "contractPre201603 is true and contractVariedPost201603 is not defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16 = None,
            relevantRent = Some(12)
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationFailure(
            "Relevant Rent details failed validation with " +
              "'exchangedContractsBeforeMar16': Some(true), " +
              "'contractChangedSinceMar16': None, " +
              "'relevantRent': Some(12)"
          )
        }
        "exchangedContractsBeforeMar16 is not defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = None,
            contractChangedSinceMar16 = Some(false),
            relevantRent = Some(12)
          )
          validRelevantRentDetailsStructure(testDetails, postMar2016Date) shouldBe ValidationFailure(
            "Relevant Rent details failed validation with " +
              "'exchangedContractsBeforeMar16': None, " +
              "'contractChangedSinceMar16': Some(false), " +
              "'relevantRent': Some(12)"
          )
        }
      }
      "effective date is before 17/03/2016" when {
        "relevant rent is not defined" in {
          val testDetails = RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(false),
            contractChangedSinceMar16 = None,
            relevantRent = None
          )
          validRelevantRentDetailsStructure(testDetails, preMar2016Date) shouldBe ValidationFailure(
            "No relevant rent amount provided"
          )
        }
      }
    }
  }

  "validFirstTimeBuyer" should {
    "return a ValidationSuccess" when {
      "the effective date is before 22/11/2017" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 12, 31),
          premium = 140000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validFirstTimeBuyer(request) shouldBe ValidationSuccess
      }

      "the effective date is after 30/11/2019" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2020, 12, 31),
          premium = 140000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validFirstTimeBuyer(request) shouldBe ValidationSuccess
      }

      "the property type is not residential" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 12, 31),
          premium = 140000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        validFirstTimeBuyer(request) shouldBe ValidationSuccess
      }

      "the effective date is after 22/11/2017 and the property type is residential " when {
        "the user is an individual who doesn't own twoOrMoreProperties and firstTimeBuyer is defined" in {
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false)
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = Some(true)
          )
          validFirstTimeBuyer(request) shouldBe ValidationSuccess
        }

        "the user is an individual owning twoOrMoreProperties" in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(true),
                replaceMainResidence = Some(false)
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationSuccess
        }

        "the user is not an individual" in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = false,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false)
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationSuccess

        }
      }
    }

    "return a ValidationFailure" when{
      "the effective date is after 22/11/2017, the property type is residential" when{
        "there are no property details " in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationFailure("No property details found for first time buyer.")
        }

        "there are valid property details but firstTimeBuyer is undefined. " in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false)
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationFailure("First time buyer was not defined.")
        }
      }
    }
  }

  "listValidationErrors" should {
    "have no errors" when {
      "holding type is freehold, non-residential and there are no lease details" in {
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 6, 30),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq.empty
      }
      "holding type is leasehold and lease details have been provided" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq.empty
      }
      "property details aren't provided" when {
        "freehold, residential and effective date between 22/03/2012 and 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2012, 3, 22),
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "leasehold, residential and effective date between 22/03/2012 and 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2014, 3, 20),
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "property type is non-residential" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2014, 3, 20),
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
      }
      "property details are provided" when {
        "freehold, residential and effective date > 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2012, 3, 22),
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = None,
            relevantRentDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "leasehold, residential and effective date > 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 3, 20),
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = Some(LeaseDetails(
              startDate = LocalDate.of(2000, 1, 30),
              endDate = LocalDate.of(2099, 12, 31),
              leaseTerm = LeaseTerm(
                years = 82,
                days = 286,
                daysInPartialYear = 0
              ),
              year1Rent = 5000,
              year2Rent = Some(10000),
              year3Rent = Some(10000),
              year4Rent = Some(10000),
              year5Rent = Some(10000)
            )),
            relevantRentDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
      }

    }

    "have the correct errors" when {
      "effective date is < 22/03/2012" in {
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 21),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("Effective date of '2012-03-21' is before 22 March, 2012")
        )
      }

      "holding type is leasehold and there are no lease details" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No lease details provided for leasehold property")
        )
      }

      "freehold, residential, effective date > 31/03/2016 and there are no property details" in {
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No property details for 'freehold' residential property with effective date of '2016-04-01'")
        )
      }

      "leasehold, residential, effective date > 31/03/2016 and there are no property details" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetails),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No property details for 'leasehold' residential property with effective date of '2016-04-01'"),
          ValidationFailure("Lease term year: 85 or Lease term date: 286 does not match the difference between 2016-04-01 and 2099-12-31")
        )
      }
      "leasehold, non-residential, premium is <£150000, all rents are <£2000 and relevant rent is not defined" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetailsAllLessTan2000),
          relevantRentDetails = None,
          firstTimeBuyer = None
        )
        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("Lease term year: 83 or Lease term date: 200 does not match the difference between 2017-01-31 and 2099-12-31"),
          ValidationFailure("Relevant rent details not provided when premium: 140000, " +
              "holding type: leasehold, property type: non-residential and all rents <£2000"
          )
        )
      }

      "there are multiple errors" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No lease details provided for leasehold property"),
          ValidationFailure("No property details for 'leasehold' residential property with effective date of '2016-04-01'")
        )
      }
    }
  }
}
