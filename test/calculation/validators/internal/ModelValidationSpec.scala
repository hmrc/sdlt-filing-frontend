package calculation.validators.internal

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{LeaseDetails, LeaseTerm, PropertyDetails, Request}
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
      years = 83,
      days = 200,
      daysInPartialYear = 0
    ),
    year1Rent = 5000,
    year2Rent = Some(10000),
    year3Rent = Some(10000),
    year4Rent = Some(10000),
    year5Rent = Some(10000)
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
          leaseDetails = None
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          leaseDetails = Some(tempLeaseDetails)
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
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails)
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
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails)
        )
        validLeaseTerm(model) shouldBe ValidationSuccess
      }

    "the year[1-5]Rent has been applied and the number of leaseTerm years is greater than 5" in{
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
        year5Rent = Some(5000)
      )

      val model = Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = LocalDate.of(2014, 3, 20),
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails)
      )
      validLeaseTerm(model) shouldBe ValidationSuccess
    }

      "the year[1-5]Rent has been applied and the number of leaseTerm years is 4 and the leaseTerm partial days is 1" in{
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
          year5Rent = Some(5000)
        )

        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails)
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
        leaseDetails = Some(tempLeaseDetails)
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
        leaseDetails = Some(tempLeaseDetails)
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
        leaseDetails = Some(tempLeaseDetails)
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
      "individual is 'true' and other fields are defined" in {
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
          leaseDetails = None
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
          leaseDetails = Some(validTestLeaseDetails)
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
            leaseDetails = None
          )

          listValidationErrors(model) shouldBe Seq.empty
          listValidationErrors(model.copy(effectiveDate = LocalDate.of(2016, 3, 31))) shouldBe Seq.empty
        }
        "leasehold, residential and effective date between 22/03/2012 and 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2012, 3, 22),
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails)
          )

          listValidationErrors(model) shouldBe Seq.empty
          listValidationErrors(model.copy(effectiveDate = LocalDate.of(2016, 3, 31))) shouldBe Seq.empty
        }
        "property type is non-residential" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2016, 6, 30),
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails)
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
      }
      "property details are provided" when {
        "freehold, residential and effective date > 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2014, 4, 1),
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "leasehold, residential and effective date > 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2016, 4, 1),
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = Some(validTestLeaseDetails)
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
          leaseDetails = None
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
          leaseDetails = None
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
          leaseDetails = None
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
          leaseDetails = Some(validTestLeaseDetails)
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No property details for 'leasehold' residential property with effective date of '2016-04-01'")
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
          leaseDetails = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No lease details provided for leasehold property"),
          ValidationFailure("No property details for 'leasehold' residential property with effective date of '2016-04-01'")
        )
      }
    }
  }
}
