/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package validators.internal

import enums.sdltRebuild.{AcquisitionByBodiesEstablishedForNationalPurposes, AlternativeFinanceInvestmentBondsRelief, AlternativePropertyFinance, CharitiesTaxReliefs, CombinationOfReliefs, ComplianceWithPlanningObligations, CompulsoryPurchaseFacilitatingDevelopment, CroftingCommunityRightToBuy, DemutualisationOfBuildingSociety, DemutualisationOfInsuranceCompany, DiplomaticPrivileges, FreeportsTaxSiteRelief, GroupRelief, IncorporationOfLimitedLiabilityPartnership, InvestmentZonesTaxSiteRelief, OtherTaxReliefs, PartExchange, ReConstructionRelief, ReLocationEmployment, RegisteredSocialLandlords, SeedingRelief, StandardZeroRate, TaxReliefCode, TransferInConsequenceOfReorganisationOfParliamentaryConstituencies, TransfersInvolvingPublicBodies, ZeroRate}

import java.time.LocalDate
import enums.{HoldingTypes, PropertyTypes}
import models._
import models.sdltRebuild.TaxReliefDetails
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

class ModelValidationSpec extends PlaySpec {

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
    replaceMainResidence = None,
    sharedOwnership = None,
    currentValue = None
  )

  private val validTestLeaseDetails = LeaseDetails(
    startDate = LocalDate.of(2000, 1, 30),
    endDate = LocalDate.of(2099, 12, 31),
    leaseTerm = LeaseTerm(
      years = 85,
      days = 287,
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

  private val zeroRateFreePortReliefGen:Gen[TaxReliefCode with ZeroRate] = Gen.oneOf(FreeportsTaxSiteRelief,InvestmentZonesTaxSiteRelief)
  private val zeroRateWithoutFreePortReliefGen: Gen[TaxReliefCode with StandardZeroRate] = Gen.oneOf(PartExchange, ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment, ComplianceWithPlanningObligations,
    GroupRelief, ReConstructionRelief, DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety, IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies, TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs, AcquisitionByBodiesEstablishedForNationalPurposes, RegisteredSocialLandlords,
    AlternativePropertyFinance, CroftingCommunityRightToBuy, DiplomaticPrivileges, OtherTaxReliefs,
    CombinationOfReliefs, AlternativeFinanceInvestmentBondsRelief, SeedingRelief)

  import ModelValidation._

  "validLeaseDetails" must {
    "return a ValidationSuccess" when{
      "the Holding Type is Freehold" in{
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        validLeaseDetails(model) shouldBe ValidationFailure("Lease details have been input incorrectly")
      }
    }
  }

  "validLeaseTerm" must{
    "return a ValidationSuccess" when{
      "the effective date is before the lease start date" in{
        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2098, 12, 31),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 1,
            days = 1,
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
          effectiveDate = LocalDate.of(2000, 1, 30),
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )

        validLeaseTerm(model) shouldBe ValidationSuccess
      }

      "only the year1Rent has been applied and it is equal to the leaseTerm years" in{
         val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2000, 1, 30),
          endDate = LocalDate.of(2099, 12, 31),
          leaseTerm = LeaseTerm(
            years = 1,
            days = 1,
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
          nonUKResident = Some(true),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
            days = 1,
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
          nonUKResident = Some(false),
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          days = 287,
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
        nonUKResident = None,
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
        isLinked = None,
        interestTransferred = None,
        taxReliefDetails = None,
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
            days = 287,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(tempLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
        nonUKResident = None,
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
        isLinked = None,
        interestTransferred = None,
        taxReliefDetails = None,
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
        nonUKResident = None,
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
        isLinked = None,
        interestTransferred = None,
        taxReliefDetails = None,
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
        nonUKResident = None,
        premium = 500000,
        highestRent = 0,
        propertyDetails = None,
        leaseDetails = Some(tempLeaseDetails),
        relevantRentDetails = None,
        isLinked = None,
        interestTransferred = None,
        taxReliefDetails = None,
          firstTimeBuyer = None
      )
      validLeaseTerm(model) shouldBe ValidationFailure("Lease term: 4 does not match amount of lease year rents: 4 and 1 partial days")
    }
   }
  }

  "validLeaseLength" must{
    "return a ValidationSuccess" when{
      "given a lease start date in a leap year(feb 29th) and an end date not in a leap year" in {

        val effectiveDate = LocalDate.of(2016, 2, 29)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2016, 2, 29),
          endDate = LocalDate.of(2019, 2, 28),
          leaseTerm = LeaseTerm(
            years = 3,
            days = 1,
            daysInPartialYear = 0
          ),
          year1Rent = 1000,
          year2Rent = Some(2000),
          year3Rent = Some(3000),
          year4Rent = None,
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }

      "given a lease start date in a leap year(march 1st) and an end date not in a leap year" in{
        val effectiveDate = LocalDate.of(2016,3,1)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2016, 3, 1),
          endDate = LocalDate.of(2019, 2, 28),
          leaseTerm = LeaseTerm(
            years = 3,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 1000,
          year2Rent = Some(2000),
          year3Rent = Some(3000),
          year4Rent = None,
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }


      "given a lease start date in a leap year(feb 29th) and an end date in a leap year" in{
        val effectiveDate = LocalDate.of(2016,2,29)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2016, 2, 29),
          endDate = LocalDate.of(2020, 2, 29),
          leaseTerm = LeaseTerm(
            years = 4,
            days = 1,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(1000),
          year3Rent = Some(2000),
          year4Rent = Some(3000),
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }

      "given a lease start date in a leap year(march 1st) and an end date in a leap year" in{
        val effectiveDate = LocalDate.of(2016,3,1)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2016, 3, 1),
          endDate = LocalDate.of(2020, 2, 29),
          leaseTerm = LeaseTerm(
            years = 4,
            days = 0,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(1000),
          year3Rent = Some(2000),
          year4Rent = Some(3000),
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }

      "given a lease start date not in a leap year and an end date in a leap year" in{
        val effectiveDate = LocalDate.of(2017,2,28)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2017, 2, 28),
          endDate = LocalDate.of(2020, 2, 29),
          leaseTerm = LeaseTerm(
            years = 3,
            days = 2,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(1000),
          year3Rent = Some(2000),
          year4Rent = Some(3000),
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }

      "given a lease start date not in a leap year and an end date not in a leap year" in{
        val effectiveDate = LocalDate.of(2017,2,28)

        val tempLeaseDetails = LeaseDetails(
          startDate = LocalDate.of(2017, 2, 28),
          endDate = LocalDate.of(2021, 3, 11),
          leaseTerm = LeaseTerm(
            years = 4,
            days = 12,
            daysInPartialYear = 0
          ),
          year1Rent = 5000,
          year2Rent = Some(1000),
          year3Rent = Some(2000),
          year4Rent = Some(3000),
          year5Rent = None
        )

        validLeaseLength(effectiveDate, tempLeaseDetails) shouldBe ValidationSuccess
      }
    }
  }

  "validPropertyDetailsStructureFreehold" must {
    "Return a ValidationSuccess for a PropertyDetailsModel" when {
      "individual is 'false' and other fields are defined" in {
        val deets = PropertyDetails(
          individual = false,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationSuccess
      }

      "individual is 'false' and other fields are not defined" in {
        val deets = PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationSuccess
      }
      "individual is 'true', twoOrMoreProperties 'false' and replaceMainResidence not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationSuccess
      }
      "individual is 'true', twoOrMoreProperties 'true' and replaceMainResidence defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationSuccess
      }
    }
    "Return a correctly messaged ValidationFailure" when {
      "individual is true and twoOrMoreProperties is not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = None,
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationFailure(
          "Property details failed validation with 'individual': true, " +
            "'twoOrMoreProperties': None, " +
            "'replaceMainResidence': Some(false)"
        )
      }
      "individual is true, twoOrMoreProperties 'true' and replaceMainResidence not defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureFreehold(deets) shouldBe ValidationFailure(
          "Property details failed validation with 'individual': true, " +
            "'twoOrMoreProperties': Some(true), " +
            "'replaceMainResidence': None"
        )
      }
    }
  }



  "validPropertyDetailsStructureLeasehold" must {
    "Return a ValidationSuccess for a PropertyDetailsModel" when {
      "individual is 'true' and other fields are defined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None,
          sharedOwnership = Some(true),
          currentValue = Some(true)
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationSuccess
      }

      "individual is 'true', twoOrMoreProperties is false and other fields are undefined" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationSuccess
      }

      "individual is 'true' and sharedOwnership 'true' and currentValue 'false'" in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None,
          sharedOwnership = Some(true),
          currentValue = Some(false)
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationSuccess
      }
      "individual is 'true' and other fields are sharedOwnership 'false' " in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = None,
          sharedOwnership = Some(false),
          currentValue = None
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationSuccess
      }
    }
    "Return a ValidationFailure for a PropertyDetailsModel" when {
      "individual is 'true' and other fields are replaceMainResidence 'Some(true)' and sharedOwnership 'Some(false)') " in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = Some(false),
          currentValue = None
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationFailure(
          s"Property details failed validation with 'individual': true, " +
            s"'twoOrMoreProperties': Some(false), " +
            s"'replaceMainResidence': Some(true)," +
            s"'sharedOwnership': Some(false)," +
            s"'currentValue' : None")
      }
      "individual is 'true' and other fields are sharedOwnership 'Some(false)' and currentValue 'Some(true)' " in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(false),
          sharedOwnership = Some(false),
          currentValue = Some(true)
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationFailure(
          s"Property details failed validation with 'individual': true, " +
            s"'twoOrMoreProperties': Some(false), " +
            s"'replaceMainResidence': Some(false)," +
            s"'sharedOwnership': Some(false)," +
            s"'currentValue' : Some(true)")
      }

      "individual is 'true' and other fields are currentValue 'Some(true)' " in {
        val deets = PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = Some(true)
        )
        validPropertyDetailsStructureLeasehold(deets) shouldBe ValidationFailure(
          s"Property details failed validation with 'individual': true, " +
            s"'twoOrMoreProperties': Some(false), " +
            s"'replaceMainResidence': Some(false)," +
            s"'sharedOwnership': None," +
            s"'currentValue' : Some(true)")
      }
    }
  }

      "allRentsBelow2000" must {
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

  "validRelevantRentDetails" must {

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
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "property type is residential" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 1000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(800)),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "property type is mixed" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.mixed,
            effectiveDate = LocalDate.of(2017, 1, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 1000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(800)),
            relevantRentDetails = None,
            isLinked = None,
            taxReliefDetails = None,
            interestTransferred = None,
            firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "premium is >=£150000" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            nonUKResident = None,
            premium = 150000,
            highestRent = 2000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(800)),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )
          validRelevantRentDetails(request) shouldBe ValidationSuccess
        }

        "there is a rent >= £2000" in {
          val request = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2017, 1, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 2000,
            propertyDetails = None,
            leaseDetails = Some(validLeaseDetails(2000)),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validLeaseDetails(800)),
          relevantRentDetails = Some(testRelevantRentDetails),
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = Some(testRelevantRentDetails),
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        validRelevantRentDetails(request) shouldBe ValidationFailure("No lease details provided for leasehold property")
      }
      "relevant rent is not defined" in {
        val request = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          nonUKResident = None,
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validLeaseDetails(800)),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        validRelevantRentDetails(request) shouldBe ValidationFailure(
          "Relevant rent details not provided when premium: 140000, " +
            "holding type: leasehold, property type: non-residential and all rents <£2000"
        )
      }
    }
  }

  "validRelevantRentDetailsStructure" must {
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

  "validFirstTimeBuyer" must {
    "return a ValidationSuccess" when {
      "the effective date is before 22/11/2017" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 12, 31),
          nonUKResident = None,
          premium = 140000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        validFirstTimeBuyer(request) shouldBe ValidationSuccess
      }

      "the effective date is after 30/11/2019" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2020, 12, 31),
          nonUKResident = None,
          premium = 140000,
          highestRent = 0,
          propertyDetails = Some(
            PropertyDetails(
              individual = true,
              twoOrMoreProperties = Some(true),
              replaceMainResidence = Some(false),
              sharedOwnership = None,
              currentValue = None
            )
          ),
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        validFirstTimeBuyer(request) shouldBe ValidationSuccess
      }

      "the property type is not residential" in{
        val request = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 12, 31),
          nonUKResident = None,
          premium = 140000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false),
                sharedOwnership = None,
                currentValue = None
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
            firstTimeBuyer = Some(true)
          )
          validFirstTimeBuyer(request) shouldBe ValidationSuccess
        }

        "the user is an individual owning twoOrMoreProperties" in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(true),
                replaceMainResidence = Some(false),
                sharedOwnership = None,
                currentValue = None
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationSuccess
        }

        "the user is not an individual" in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = false,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false),
                sharedOwnership = None,
                currentValue = None
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
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
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationFailure("No property details found for first time buyer.")
        }

        "there are valid property details but firstTimeBuyer is undefined. " in{
          val request = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 12, 31),
            nonUKResident = None,
            premium = 140000,
            highestRent = 0,
            propertyDetails = Some(
              PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(false),
                replaceMainResidence = Some(false),
                sharedOwnership = None,
                currentValue = None
              )
            ),
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
            firstTimeBuyer = None
          )
          validFirstTimeBuyer(request) shouldBe ValidationFailure("First time buyer was not defined.")
        }
      }
    }
  }

  "listValidationErrors" must {
    "have no errors" when {
      "holding type is freehold, non-residential and there are no lease details" in {
        val model = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 6, 30),
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq.empty
      }
      "holding type is leasehold and lease details have been provided" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 3, 20),
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "leasehold, residential and effective date between 22/03/2012 and 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2014, 3, 20),
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "property type is non-residential" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.nonResidential,
            effectiveDate = LocalDate.of(2014, 3, 20),
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "property type is mixed" in {
          val model = Request(
            holdingType = HoldingTypes.leasehold,
            propertyType = PropertyTypes.mixed,
            effectiveDate = LocalDate.of(2014, 3, 20),
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = None,
            leaseDetails = Some(validTestLeaseDetails),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
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
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = None,
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
          firstTimeBuyer = None
          )

          listValidationErrors(model) shouldBe Seq.empty
        }
        "leasehold, residential and effective date > 31/03/2016" in {
          val model = Request(
            holdingType = HoldingTypes.freehold,
            propertyType = PropertyTypes.residential,
            effectiveDate = LocalDate.of(2017, 3, 20),
            nonUKResident = None,
            premium = 500000,
            highestRent = 0,
            propertyDetails = Some(validPropertyDetails),
            leaseDetails = Some(LeaseDetails(
              startDate = LocalDate.of(2000, 1, 30),
              endDate = LocalDate.of(2099, 12, 31),
              leaseTerm = LeaseTerm(
                years = 82,
                days = 287,
                daysInPartialYear = 0
              ),
              year1Rent = 5000,
              year2Rent = Some(10000),
              year3Rent = Some(10000),
              year4Rent = Some(10000),
              year5Rent = Some(10000)
            )),
            relevantRentDetails = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetails),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )

        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("No property details for 'leasehold' residential property with effective date of '2016-04-01'"),
          ValidationFailure("Lease term year: 85, Lease term day: 287, comparisonDate: 2102-01-12 does not match the difference between 2016-04-01 and 2099-12-31")
        )
      }
      "leasehold, non-residential, premium is <£150000, all rents are <£2000 and relevant rent is not defined" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 31),
          nonUKResident = None,
          premium = 140000,
          highestRent = 1000,
          propertyDetails = None,
          leaseDetails = Some(validTestLeaseDetailsAllLessTan2000),
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )
        listValidationErrors(model) shouldBe Seq(
          ValidationFailure("Lease term year: 83, Lease term day: 200, comparisonDate: 2100-08-18 does not match the difference between 2017-01-31 and 2099-12-31"),
          ValidationFailure("Relevant rent details not provided when premium: 140000, " +
              "holding type: leasehold, property type: non-residential and all rents <£2000"
          )
        )
      }


      "validIsPartialRelief" must {
        "return a ValidationSuccess" when {
          "the HoldingTypes is LeaseHold" when {
            "the value of isPartialRelief is false and isLinked is false and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.leasehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = None,
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = Some(false)
                    ))
                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }
            "the isPartialRelief is true and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.leasehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = None,
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = Some(true)
                    ))

                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }
            "the taxReliefCode is not FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief and isLinked is true/false" in  {
              forAll(zeroRateWithoutFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.leasehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = Some(true),
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = None
                    ))
                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }
          }
          "the HoldingTypes is FreeHold" when {
            "the value of isPartialRelief is false, isLinked is false and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.freehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = None,
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = Some(false)
                    ))
                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }

            "the isPartialRelief is true and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.freehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = None,
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = Some(true)
                    ))
                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }

            "the taxReliefCode is not FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief and isLinked is true/false" in {
              forAll(zeroRateWithoutFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.freehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = None,
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = Some(true)
                    ))
                  )
                  validTaxRelief(request) shouldEqual ValidationSuccess
              }
            }
          }
        }
        "return a ValidationFailure" when{
          "the Holding Types is LeaseHold and isLinked is false" when {
            "the isPartialRelief is not defined(None) and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.leasehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = Some(false),
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = None
                    ))
                  )
                  validTaxRelief(request) shouldBe ValidationFailure("No partial relief type defined.")
              }
            }
          }
          "the Holding Types is FreeHold" when {
            "the isPartialRelief is not defined(None) and taxReliefCode is either FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" in {
              forAll(zeroRateFreePortReliefGen) {
                value =>
                  val request = Request(
                    holdingType = HoldingTypes.freehold,
                    propertyType = PropertyTypes.residential,
                    effectiveDate = LocalDate.of(2017, 12, 31),
                    nonUKResident = None,
                    premium = 140000,
                    highestRent = 0,
                    propertyDetails = Some(
                      PropertyDetails(
                        individual = true,
                        twoOrMoreProperties = Some(false),
                        replaceMainResidence = Some(false),
                        sharedOwnership = None,
                        currentValue = None
                      )
                    ),
                    leaseDetails = None,
                    relevantRentDetails = None,
                    firstTimeBuyer = None,
                    isLinked = Some(false),
                    interestTransferred = None,
                    taxReliefDetails = Some(TaxReliefDetails(
                      taxReliefCode = value,
                      isPartialRelief = None
                    ))
                  )
                  validTaxRelief(request) shouldBe ValidationFailure("No partial relief type defined.")
              }
            }
          }
        }
      }

      "there are multiple errors" in {
        val model = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          nonUKResident = None,
          premium = 500000,
          highestRent = 0,
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = None,
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
