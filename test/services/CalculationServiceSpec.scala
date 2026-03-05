/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.ResultText.RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT
import enums.HoldingTypes._
import enums.PropertyTypes._
import enums.sdltRebuild._
import enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import exceptions.InvalidDateException
import generators.RequestGenerators
import models._
import models.sdltRebuild.TaxReliefDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class CalculationServiceSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach
  with RequestGenerators with ScalaCheckPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 50)

  val april2021EffectiveDate: LocalDate = LocalDate.of(2021, 4, 1)
  val july2020EffectiveDate: LocalDate = LocalDate.of(2020, 7, 8)
  val july2021EffectiveDate: LocalDate = LocalDate.of(2021, 7, 1)
  val APRIL2009_EFFECTIVE_DATE: LocalDate = LocalDate.of(2009, 4, 23)
  val mockLeaseholdCalculationService: LeaseholdCalculationService = mock[LeaseholdCalculationService]
  val mockFreeholdCalculationService: FreeholdCalculationService = mock[FreeholdCalculationService]
  val mockAdditionalPropertyService: AdditionalPropertyService = mock[AdditionalPropertyService]
  val testCalculationService = new CalculationService(
    mockLeaseholdCalculationService,
    mockFreeholdCalculationService,
    mockAdditionalPropertyService,

  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAdditionalPropertyService)
    reset(mockFreeholdCalculationService)
    reset(mockLeaseholdCalculationService)
  }

  "CalculateTax" must {
    val calcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      taxDue = 0,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      rate = None,
      slices = None
    )

    val generateIsLinkedFalseAndNoneValue :Gen[Option[Boolean]] = Gen.oneOf(None, Some(false))

    val generateIsLinkedAllPossibleValues : Gen[Option[Boolean]] = Gen.oneOf(None, Some(false), Some(true))

    val generateMixedAndNonResidentialPropertyTypes : Gen[enums.PropertyTypes.Value] = Gen.oneOf(mixed, nonResidential)

    def createResult(msg: String) = Result(
      totalTax = 0,
      resultHeading = Some(msg),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDetails)
    )
    def createResultInSeq(msg: String) = Seq(
      createResult(msg)
    )

    def createRequest(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate, taxReliefCode: Option[TaxReliefCode] = None, isLinked: Option[Boolean] = None, isAddProp: Boolean = false, isPartialRelief: Option[Boolean] = None, isMultipleLand: Option[Boolean] = None) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = None,
      premium = BigDecimal(0),
      highestRent = BigDecimal(0),
      propertyDetails = Option.when(isAddProp)(
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
      interestTransferred = None,
      isLinked = isLinked,
      taxReliefDetails = taxReliefCode.map(trc =>
        TaxReliefDetails(
          taxReliefCode = trc,
          isPartialRelief = isPartialRelief
        )
      ),
      firstTimeBuyer = None,
      isMultipleLand = isMultipleLand
    )

    def createRequestWithPropDetails(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate, premiumAmount: BigDecimal= 0, nonUKResident: Option[Boolean] = None) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = nonUKResident,
      premium = premiumAmount,
      highestRent = BigDecimal(0),
      propertyDetails =  Some(
        PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
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

    "select the freeholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 3))
        val result = createResult("freeholdResidential, March2012 to December2014")

        when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(testRequest)
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("freeholdResidential, March2012 to December2014")

        when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(testRequest)
      }
    }

    "select the freeholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2017, 1, 1))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
      }
    }

    "select the freeholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2016, 3, 16))
        val result = createResult("freeholdNonResidential, March2012 to March2016")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2011, 3, 4))
        val result = createResult("freeholdNonResidential, March2012 to March2016")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }
    }

    "select the freeholdResidential November2017 first time buyer function" when {
      "given a request with an effective date of 22/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 11, 22))
        val result = createResult("freeholdResidential, November2017 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 23/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 11, 23))
        val result = createResult("freeholdResidential, November2017 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 30/11/2019 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30))
        val result = createResult("freeholdResidential, November2017 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }
    }

    "select the freeholdResidential September2022 first time buyer function" when {
      "given a request with an effective date of 23/9/2022 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2022, 9, 23))
        val result = createResult("freeholdResidential, September2022 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialSept22OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 24/9/2022 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2022, 9, 24))
        val result = createResult("freeholdResidential, September2022 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialSept22OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 23/1/2023 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2023, 1, 23))
        val result = createResult("freeholdResidential, September2022 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialSept22OnwardsFTB(testRequest)
      }
    }







    "select the freeholdResidential April2025 first time buyer function" when {
      "given a request with an effective date of 01/04/2025 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResult("freeholdResidential, April2025 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 01/05/2025 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2025, 5, 1))
        val result = createResult("freeholdResidential, April2025 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 5/1/2026 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2026, 1, 5))
        val result = createResult("freeholdResidential, April2025 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialNov17OnwardsFTB(testRequest)
      }
    }

    "select the freeholdResidential additional property function" when {
      "given a request with an effective date of 08/07/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate, BigDecimal(500001))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 11/7/2020 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2020, 7, 11))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 31/3/2021 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 3, 31))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 1/10/2021 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 10, 1))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 30/11/2019,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 2/4/2016 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 2))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 1/4/2016 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 1))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 23/9/2022 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2022, 9, 23))
        val result = createResultInSeq("freeholdResidential, July2021 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropJuly21Onwards(testRequest)
      }
      "given a request with an effective date of 01/04/2025 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResultInSeq("freeholdResidential, April2025 onwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropApril25Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropApril25Onwards(testRequest)
      }
      "given a request with an effective date of 31/10/2024 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2024, 10, 31))
        val result = createResultInSeq("freeholdResidential, Oct24BeforeApril25")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropOct24BeforeApril25(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropOct24BeforeApril25(testRequest)
      }
    }

    "select the freeholdResidential function for July 2021" when {
      "given a request with an effective date of 1/7/2021" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 7, 1))
        val result = createResult("freeholdResidential, July 2021")

        when(mockFreeholdCalculationService.freeholdResidentialJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }

      "given a request with an effective date of 23/9/2022" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2022, 9, 23))
        val result = createResult("freeholdResidential, July 2021")

        when(mockFreeholdCalculationService.freeholdResidentialJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }

      "given a request with an effective date of 01/4/2025" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResult("freeholdResidential, April 2025")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }
    }

    "select the freeholdResidential function for July 2020" when {
      "given a request with an effective date of 09/07/2020" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2020, 7, 30))
        val result = createResult("freeholdResidential, July2020 onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), ArgumentMatchers.eq(None))).thenReturn(false)

        when(mockFreeholdCalculationService.freeholdResidentialJuly20Onwards(testRequest)).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), ArgumentMatchers.eq(None))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 08/07/2020" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("freeholdResidential, July2020 onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())).thenReturn(false)

        when(mockFreeholdCalculationService.freeholdResidentialJuly20Onwards(testRequest)).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("freeholdResidential, July2020 onwards")


        when(mockFreeholdCalculationService.freeholdResidentialJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialJuly20Onwards(testRequest)

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }

      "given a request with an effective date of 31/3/2021 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 3, 31))
        val result = createResult("freeholdResidential, July2020 onwards")


        when(mockFreeholdCalculationService.freeholdResidentialJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialJuly20Onwards(testRequest)

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }
    }

    "select the freeholdResidential function for December2014 to April2016" when {

      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 3, 31))
        val result = createResult("freeholdResidential, December2014 to April2016")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(testRequest)).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(testRequest)
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("freeholdResidential, December2014 to April2016")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(testRequest)).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(testRequest)
      }

      "given a request with an effective date of 1/4/2016 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 1))
        val result = createResult("freeholdResidential, December2014 to April2016")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(testRequest)
      }
    }

    "throw an exception for freehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))
        the [InvalidDateException] thrownBy testCalculationService.calculateTax(testRequest) must have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }

    ///////////LEASEHOLD TESTS ///////////

    "select the leaseholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2017, 1, 1))
        val result = createResultInSeq("leaseholdNonResidential, March2016 onwards")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar16Onwards(testRequest)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("leaseholdNonResidential, March2016 onwards")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar16Onwards(testRequest)
      }
    }

    "select the leaseholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 16))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(testRequest)
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2011, 3, 4))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(testRequest)
      }
    }

    "select the leaseholdResidential November2017 first time buyer function" when {
      "given a request with an effective date of 22/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2017, 11, 22))
        val result = createResult("leaseholdResidential, November2017 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 23/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2017, 11, 23))
        val result = createResult("leaseholdResidential, November2017 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 30/11/2019 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30))
        val result = createResult("leaseholdResidential, November2017 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }
    }

    "select the leaseholdResidential September 2022 first time buyer function" when {
      "given a request with an effective date of 23/9/2022 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2022, 9, 23))
        val result = createResult("leaseholdResidential, Sept2022 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialSept22OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 24/9/2022 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2022, 9, 24))
        val result = createResult("leaseholdResidential, Sept2022 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialSept22OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 30/1/2023 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2023, 1, 30))
        val result = createResult("leaseholdResidential, Sept2022 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialSept22OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialSept22OnwardsFTB(testRequest)
      }
    }




    "select the leaseholdResidential April 2025 first time buyer function" when {
      "given a request with an effective date of 23/9/2022 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResult("leaseholdResidential, April2025 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 01/04/2025 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResult("leaseholdResidential, April2025 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }

      "given a request with an effective date of 01/04/2024 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResult("leaseholdResidential, April2025 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialNov17OnwardsFTB(testRequest)
      }
    }




    "select the leaseholdResidential additional property function for July2020 onwards" when {
      "given a request with an effective date of 30/11/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 11/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2020, 7, 11))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly20Onwards(testRequest)
      }
    }

    "select the leaseholdResidential additional property function for July2021 onwards" when {
      "given a request with an effective date of 30/11/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, july2021EffectiveDate, BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, July2021 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly21Onwards(testRequest)
      }

      "given a request with an effective date of 11/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2022, 9, 23))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly21Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2023, 1, 30))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly21Onwards(testRequest)
      }
    }

    "select the leaseholdResidential additional property function for Oct24 Before April25 " when {
      "given a request with an effective date of 31/10/2024 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2024, 10, 31))
        val result = createResultInSeq("leaseholdResidential, Oct24 Before April25")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropOct24BeforeApril25(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropOct24BeforeApril25(testRequest)
      }
    }

    "select the leaseholdResidential additional property function for April25 Onwards" when {
      "given a request with an effective date of 02/04/2025 and additional property rates check true" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResultInSeq("leaseholdResidential, April25 Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApril25Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApril25Onwards(testRequest)
      }
    }

    "select the leaseholdResidential additional property function for April2025 onwards" when {
      "given a request with an effective date of 30/11/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, july2021EffectiveDate, BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, July2021 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly21Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropJuly21Onwards(testRequest)
      }

      "given a request with an effective date of 11/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 4, 1))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApril25Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApril25Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2025, 7, 30))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApril25Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApril25Onwards(testRequest)
      }
    }

    "select the leaseholdResidential additional property function for April2016 onwards" when {
      "given a request with an effective date of 30/11/2019,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 2/4/2016 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 2))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApr16Onwards(testRequest)
      }

      "given a request with an effective date of 1/4/2016 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 1))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialAddPropApr16Onwards(testRequest)
      }
    }
    "select the leaseholdResidential function for July 2020 onwards" when {
      "given a request with an effective date of 31/7/2020" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 7, 31))
        val result = createResult("leaseholdResidential, July2020 onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 11/7/2020" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 7, 11))
        val result = createResult("leaseholdResidential, July2020 onwards")


        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), ArgumentMatchers.eq(None), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly20Onwards(testRequest)
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check false" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("leaseholdResidential, July2020 onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly20Onwards(testRequest)

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())
      }
    }

    "select the leaseholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 3, 31))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(testRequest)
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(testRequest)
      }

      "given a request with an effective date of 1/4/2016 and additional property rates check false" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 1))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(testRequest)

      }
    }

    "select the leaseholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2014, 12, 3))
        val result = createResult("leaseholdResidential, March2012 to December2014")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(testRequest)
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("leaseholdResidential, March2012 to December2014")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(testRequest)
      }
    }

    "select the leaseholdResidential function for July2021 to September 2021" when {
      "given a request with an effective date of 1/7/2022" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2021, 7, 1))
        val result = createResult("leaseholdResidential, July2021 Onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly21Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly21Onwards(testRequest)
      }

      "given a request with an effective date of 23/9/2022" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2022, 9, 23))
        val result = createResult("leaseholdResidential, July2021 Onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly21Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly21Onwards(testRequest)
      }
    }

    "select the leaseholdResidential function for Sept2022 to Ap April2025" when {
      "given a request with an effective date of 23/9/2022" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2023, 9, 23))
        val result = createResult("leaseholdResidential, July2021 Onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialJuly21Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialJuly21Onwards(testRequest)
      }

      "given a request with an effective date of 01/4/2025" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2025, 4, 1))
        val result = createResult("leaseholdResidential, April2025 Onwards")

        when(mockAdditionalPropertyService.additionalPropertyRatesApply(any(), any(), any())).thenReturn(false)

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockAdditionalPropertyService, times(2)).additionalPropertyRatesApply(any(), any(), any())

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(testRequest)
      }
    }








    "throw an exception for leasehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))

        the [InvalidDateException] thrownBy testCalculationService.calculateTax(testRequest) must have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }

    "select the leaseholdResidential additional property of an individual for April2016 onwards" when {
      "when given a request with effective date is on 1/4/2016 & the individual has an additional property & taxReliefCode is PreCompletionTransaction" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2016,4,1), Some(PreCompletionTransaction), isLinked = Some(false), isAddProp = true)
        val result = createResult("leaseholdResAddPropPreCompletionTransactionApr2016Onwards")

        when(mockLeaseholdCalculationService.leaseholdResAddPropPreCompletionTransactionApr2016Onwards(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResAddPropPreCompletionTransactionApr2016Onwards(any())
      }
      "when given a request with effective date is after 1/4/2016 & the individual has an additional property & taxReliefCode is PreCompletionTransaction" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2021,6,1), Some(PreCompletionTransaction), isLinked = Some(false), isAddProp = true)
        val result = createResult("leaseholdResAddPropPreCompletionTransactionApr2016Onwards")

        when(mockLeaseholdCalculationService.leaseholdResAddPropPreCompletionTransactionApr2016Onwards(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResAddPropPreCompletionTransactionApr2016Onwards(any())
      }

      "given relief code PreCompletionTransaction but effective date is before 1/4/2016 should fall back to normal calculation" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2016,3,31), Some(PreCompletionTransaction), isLinked = Some(false), isAddProp = true)
        val result = createResult("freeholdStandardZeroRateTaxRelief")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
    }

    "select freeHold / residential property with tax relief code" when {
      "given relief code PartExchange" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2017, 11, 22), Some(PartExchange), isLinked = Some(false))
        val result = createResult("freeholdStandardZeroRateTaxRelief")

        when(mockFreeholdCalculationService.freeholdStandardZeroRateTaxRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdStandardZeroRateTaxRelief
      }

      "given relief code FreeportsTaxSiteRelief(36)" in {
        val freeportTestRequest = createRequest(freehold, residential, LocalDate.of(2000, 11, 22), Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdFreeportRelief")

        when(mockFreeholdCalculationService.freeholdFreeportRelief).thenReturn(result)

        testCalculationService.calculateTax(freeportTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdFreeportRelief
      }

      "given freehold | property type: residental and non-residental | zero rate taxReliefCode :: expect return zero tax" in {

        val result = createResult("freeholdStandardZeroRateTaxRelief")

        forAll( freeHoldRequestWithStandardPropertyTypesGenerator ) {
          freeHoldRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdStandardZeroRateTaxRelief).thenReturn(result)
            testCalculationService.calculateTax(freeHoldRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockFreeholdCalculationService, times(1)).freeholdStandardZeroRateTaxRelief
        }
      }

      "given leasehold | non-residential and mixed property types | zero rate taxReliefCode :: expect return zero tax" in {

        val result = createResult("leaseholdMixedNonResPropStandardZeroRelief")

        forAll( leasedHoldNonResidentialMixedRequestGenerator ) {
          freeHoldRequest =>
            reset(mockLeaseholdCalculationService)
            when(mockLeaseholdCalculationService.leaseholdMixedNonResPropStandardZeroRelief(any())).thenReturn(result)
            testCalculationService.calculateTax(freeHoldRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedNonResPropStandardZeroRelief(any())
        }
      }

      "given selfAssessed freeHold with before 04/12/2014 | taxReliefCode :: expect return zero tax" in {

        val result = createResult("freeholdStandardSelfAssessedReliefBeforeDec14")

        forAll( freeHoldSelfAssessedBeforeDateRequestGenerator ) {
          freeHoldSelfAssessedBeforeDateRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdStandardSelfAssessedReliefBeforeDec14).thenReturn(result)
            testCalculationService.calculateTax(freeHoldSelfAssessedBeforeDateRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockFreeholdCalculationService, times(1)).freeholdStandardSelfAssessedReliefBeforeDec14
        }
      }

      "given selfAssessed freeHold residential with effective date from 22/03/2012 and to 04/12/2014" in {

        val result = createResult("freeholdResidentialRightToBuyMar12ToDec14")

        forAll( freeHoldResidentialRightToBuyFromMarch2012ToApril2014 ) {
          calRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdResidentialRightToBuyMar12ToDec14).thenReturn(result)
            testCalculationService.calculateTax(calRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockFreeholdCalculationService, times(1)).freeholdResidentialRightToBuyMar12ToDec14
        }
      }

      "given relief code InvestmentZonesTaxSiteRelief(37)" in {
        val investmentTestRequest = createRequest(freehold, residential, LocalDate.of(2000, 11, 22), Some(InvestmentZonesTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdFreeportRelief")

        when(mockFreeholdCalculationService.freeholdFreeportRelief).thenReturn(result)

        testCalculationService.calculateTax(investmentTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdFreeportRelief
      }

      "given selfAssessed any property type ~ TaxReliefCode is one of: [8|9|10|11|12|13|15|16|17|18|19|20|21|23|24|26|27|28|29|31]~ with effective date 04/12/2014" in {
        // no additional property in scope
        forAll( freeHoldAnyPropertyTypeAndTaxReliefSet ) {
          calRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdSelfAssessedOnOrAfterDecember2014)
              .thenReturn(Result(
                totalTax = 0,
                resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
                resultHint = None,
                npv = None,
                taxCalcs = Seq.empty
              ))
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(calRequest)
            verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedOnOrAfterDecember2014
            res shouldBe CalculationResponse(Seq(Result(
              totalTax = 0,
              resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
              resultHint = None,
              npv = None,
              taxCalcs = Seq.empty
            )))
        }

        // with additional property
        forAll( freeHoldAnyPropertyTypeAndTaxReliefSet ) {
          calcRequest =>
            val calcRequestWithAdditionalProperty = calcRequest
              .copy(propertyDetails = Some(PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(true),
                replaceMainResidence = Some(true),
                sharedOwnership = None,
                currentValue = None
              )))
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdSelfAssessedOnOrAfterDecember2014)
              .thenReturn(Result(
                totalTax = 0,
                resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
                resultHint = None,
                npv = None,
                taxCalcs = Seq.empty
              ))
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(calcRequestWithAdditionalProperty)
            verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedOnOrAfterDecember2014
            res shouldBe CalculationResponse(Seq(Result(
              totalTax = 0,
              resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
              resultHint = None,
              npv = None,
              taxCalcs = Seq.empty
            )))
        }
      }


      "given self-assessed residential Freeport relief" in {
        val freeportTestRequest = createRequest(freehold, residential, LocalDate.of(2000, 11, 22), Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(true))
        val result = createResult("freeholdFreeportPartialRelief")

        when(mockFreeholdCalculationService.freeholdFreeportPartialRelief).thenReturn(result)

        testCalculationService.calculateTax(freeportTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdFreeportPartialRelief
      }

      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdPreCompletionTransactionReliefApr13Onwards")

        when(mockFreeholdCalculationService.freeholdPreCompletionTransactionReliefApr13Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdPreCompletionTransactionReliefApr13Onwards
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = None, isPartialRelief = Some(false))
        val result = createResult("freeholdResidentialMar12toDec14")

        when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any())
      }

      "given relief code AcquisitionRelief(14)" in {
        val AcquisitionReliefTestRequest = createRequest(freehold, residential, LocalDate.of(2000, 11, 22), Some(AcquisitionRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdAcquisitionTaxRelief")

        when(mockFreeholdCalculationService.freeholdAcquisitionTaxRelief(AcquisitionReliefTestRequest)).thenReturn(result)

        testCalculationService.calculateTax(AcquisitionReliefTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionTaxRelief(AcquisitionReliefTestRequest)
      }

      "given relief code Right to buy transactions(22)" in {
        val result = createResult("freeholdRightToBuyBeforeMarch2016")

        forAll( freeHoldRightToBuy ) {
          freeHoldRightToRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdRightToBuyBeforeMarch2016(freeHoldRightToRequest)).thenReturn(result)
            testCalculationService.calculateTax(freeHoldRightToRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockFreeholdCalculationService, times(1)).freeholdRightToBuyBeforeMarch2016(freeHoldRightToRequest)
        }
      }

      "tax relief code is FirstTimeBuyersRelief(32), isLinked = true and date is on or after 22rd March 2012 and before 25rd March 2012" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2012, 3, 22), taxReliefCode = Some(FirstTimeBuyersRelief), isLinked = Some(true))
        val result = createResult("freeholdResidentialFTB22Mar12Before25Mar12")

        when(mockFreeholdCalculationService.freeholdResidentialFTB22Mar12Before25Mar12).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialFTB22Mar12Before25Mar12
      }
    }


    "select freehold / non-residential property with tax relief code" when {
      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdPreCompletionTransactionReliefApr13Onwards")

        when(mockFreeholdCalculationService.freeholdPreCompletionTransactionReliefApr13Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdPreCompletionTransactionReliefApr13Onwards
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
      }
    }

    "select freehold / mixed property with tax relief code" when {
      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdPreCompletionTransactionReliefApr13Onwards")

        when(mockFreeholdCalculationService.freeholdPreCompletionTransactionReliefApr13Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdPreCompletionTransactionReliefApr13Onwards
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to standard calculation" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }
      "given relief code ReliefFrom15PercentRate(35) :: falls back to calculateNoTaxRelief, isLinked = true" when {
        "effective date is after 17th of March 2016" when {
          "property type is mixed or Non-residential" in {
            forAll(generateMixedAndNonResidentialPropertyTypes){
              propertyType =>
                val testRequest = createRequest(freehold, propertyType, LocalDate.of(2019, 4, 6), Some(ReliefFrom15PercentRate), isLinked = Some(true))
                val result = createResult("freeholdMixedNonResMar16Onwards")

                reset(mockFreeholdCalculationService)

                when(mockFreeholdCalculationService.freeholdMixedNonResMar16Onwards).thenReturn(result)

                testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

                verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResMar16Onwards
            }
          }
        }
      }
    }

    "select leasehold / residential property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdPreCompletionTransactionApr2013Onwards")

        when(mockLeaseholdCalculationService.leaseholdPreCompletionTransactionApr2013Onwards(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdPreCompletionTransactionApr2013Onwards(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdResidentialMar12toDec14")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any())
      }
    }

    "select leasehold / non-residential property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdPreCompletionTransactionApr2013Onwards")

        when(mockLeaseholdCalculationService.leaseholdPreCompletionTransactionApr2013Onwards(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdPreCompletionTransactionApr2013Onwards(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdNonResidentialMar12toMar16")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(any(), any(), any())
      }
    }

    "select leasehold / mixed property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2013, 4, 6), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdPreCompletionTransactionApr2013Onwards")

        when(mockLeaseholdCalculationService.leaseholdPreCompletionTransactionApr2013Onwards(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdPreCompletionTransactionApr2013Onwards(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2013, 4, 5), Some(PreCompletionTransaction), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }
    }

    "select leaseHold freePort / residential property with tax relief code" when {
      "given relief code FreeportsTaxSiteRelief" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22), Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdFreeportRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportRelief(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportRelief(any())
      }
      "given relief code InvestmentZonesTaxSiteRelief" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22), Some(InvestmentZonesTaxSiteRelief), isLinked = Some(false) , isPartialRelief = Some(false))
        val result = createResult("leaseholdFreeportRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportRelief(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportRelief(any())
      }
      "given isPartialRelief is true with residential leasehold transaction" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22), Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(true))
        val result = createResult("leaseholdFreeportPartialRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportPartialRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportPartialRelief
      }
    }
    "select leaseHold freePort / non-residential property with tax relief code" when {
      "given relief code FreeportsTaxSiteRelief" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2017, 11, 22), Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdFreeportRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportRelief(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportRelief(any())
      }
      "given relief code InvestmentZonesTaxSiteRelief" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2017, 11, 22), Some(InvestmentZonesTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(false))
        val result = createResult("leaseholdFreeportRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportRelief(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportRelief(any())
      }
      "given isPartialRelief is true with non-residential leasehold transaction" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2017, 11, 22), taxReliefCode = Some(FreeportsTaxSiteRelief), isLinked = Some(false), isPartialRelief = Some(true))
        val result = createResult("leaseholdFreeportPartialRelief")

        when(mockLeaseholdCalculationService.leaseholdFreeportPartialRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdFreeportPartialRelief
      }

      "given relief code AcquisitionRelief(14)" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2012, 3, 23), Some(AcquisitionRelief), isLinked = Some(false))
        val result = createResult("leaseholdAcquisitionTaxReliefRes")

        when(mockLeaseholdCalculationService.leaseholdAcquisitionTaxReliefRes(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdAcquisitionTaxReliefRes(any())
      }
    }

    "Given relief code CollectiveEnfranchisementByLeaseholders(25)" when{
      "effective date is on or after 23/04/2009 and is freehold" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2012, 4, 22), Some(CollectiveEnfranchisementByLeaseholders))
        val result = createResult("freeholdCollectiveEnfranchisementByLeaseholdersReliefAfterApr09")

        when(mockFreeholdCalculationService.freeholdCollectiveEnfranchisementByLeaseholdersReliefAfterApr09).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdCollectiveEnfranchisementByLeaseholdersReliefAfterApr09
      }

      "effective date is before 23/04/2009" in {
        val badTestRequest = createRequest(freehold, nonResidential, LocalDate.of(2009, 4, 22), Some(CollectiveEnfranchisementByLeaseholders))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(badTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
      }

      "effective date is on or after 23/04/2009 and is leasehold" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2009, 4, 23), Some(CollectiveEnfranchisementByLeaseholders))
        val result = createResult("leaseholdCollectiveEnfranchisementByLeaseholdersApr09Onwards")

        when(mockLeaseholdCalculationService.leaseholdCollectiveEnfranchisementByLeaseholdersApr09Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdCollectiveEnfranchisementByLeaseholdersApr09Onwards
      }
    }


    "select the freeholdSelfAssessedRes function given the request with taxReliefDetails" when {
      "given a request with relief code ReliefFrom15PercentRate :: property type is Residential, effective date is on or after 6/4/2013 and before 4/12/2014, and isLinked is true" in {

        val dates = Seq(
          LocalDate.of(2013, 4, 6),
          LocalDate.of(2014, 1, 11),
          LocalDate.of(2014, 12, 3)
        )

        val result = createResult("freeholdResidentialReliefFrom15PercentRateApr13BeforeDec14")

        dates.foreach { date =>
          reset(mockFreeholdCalculationService)

          val testRequest = createRequest(freehold, residential, date, taxReliefCode = Some(ReliefFrom15PercentRate), isLinked = Some(true))

          when(mockFreeholdCalculationService.freeholdResidentialReliefFrom15PercentRateApr13BeforeDec14).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialReliefFrom15PercentRateApr13BeforeDec14
        }
      }
      "given a request with relief code ReliefFrom15PercentRate :: property type is Residential, effective date is on or after 4/12/2014 and isLinked is true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2014, 12, 4), taxReliefCode = Some(ReliefFrom15PercentRate), isLinked = Some(true))
        val result = createResult("freeholdResidentialReliefFrom15PercentRateDec14Onwards")

        when(mockFreeholdCalculationService.freeholdResidentialReliefFrom15PercentRateDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialReliefFrom15PercentRateDec14Onwards
      }
      "given a request with relief code ReliefFrom15PercentRate :: property type is Residential Additional Property, effective date is on or after 1/4/2016 and isLinked is true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = Some(ReliefFrom15PercentRate), isAddProp = true, isLinked = Some(true))
        val result = createResult("freeholdResAddPropReliefFrom15PercentRateApr16Onwards")

        when(mockFreeholdCalculationService.freeholdResAddPropReliefFrom15PercentRateApr16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResAddPropReliefFrom15PercentRateApr16Onwards
      }
      "given a request with relief code RightToBuy and property Type is Residential with twoOrMoreProperties and isLinked = true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = Some(RightToBuy), isAddProp = true, isLinked = Some(true))
        val result = createResult("freeholdResAddPropRightToBuyReliefApr16Onwards")

        when(mockFreeholdCalculationService.freeholdResAddPropRightToBuyReliefApr16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResAddPropRightToBuyReliefApr16Onwards
      }
      "given the request with tax relief code  RightToBuy  and property type is residential property effective date is on or after 04/12/2014 and isLinked = true " in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2015, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdResidentialRightToBuyDec14Onwards")

        when(mockFreeholdCalculationService.freeholdResidentialRightToBuyDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialRightToBuyDec14Onwards
      }
      "given the request with tax relief code  RightToBuy  and property type is  mixed property effective date is before 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefBeforeMar16")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefBeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefBeforeMar16
      }

      "given the request with tax relief code  RightToBuy  and property type is  mixed property effective date is on 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 17), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefMar16Onwards
      }

      "given the request with tax relief code  RightToBuy  and property type is  mixed property effective date is after 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2021, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefMar16Onwards
      }
      "given the request with tax relief code  RightToBuy  and property type is  nonResidential property effective date is before 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefBeforeMar16")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefBeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefBeforeMar16
      }

      "given the request with tax relief code  RightToBuy  and property type is  nonResidential property effective date is on 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 17), Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefMar16Onwards
      }

      "given the request with tax relief code  RightToBuy  and property type is  nonResidential property effective date is after 17/03/2016 and isLinked = true " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2026, 3, 16), Some(RightToBuy), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResRightToBuyReliefMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResRightToBuyReliefMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResRightToBuyReliefMar16Onwards
      }
      "given a request with relief code AcquisitionRelief(14), residential property type on or after 04/12/2014 and is linked transaction" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("freeholdAcquisitionReliefDec14Onwards")

        when(mockFreeholdCalculationService.freeholdAcquisitionReliefDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionReliefDec14Onwards
      }
      "given a request with relief code AcquisitionRelief(14), non-residential property type on or after 04/12/2014 and is linked transaction" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 4, 1), taxReliefCode = Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("freeholdAcquisitionReliefDec14Onwards")

        when(mockFreeholdCalculationService.freeholdAcquisitionReliefDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionReliefDec14Onwards
      }
      "given a request with relief code AcquisitionRelief(14), mixed property type on or after 04/12/2014 and is linked transaction" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 4, 1), taxReliefCode = Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("freeholdAcquisitionReliefDec14Onwards")

        when(mockFreeholdCalculationService.freeholdAcquisitionReliefDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionReliefDec14Onwards
      }

      "given taxReliefCode:ReliefFrom15PercentRate(35) and isLinked = true " when {
        "effective date is before 17th of March 2016 " when {
          "property type is Mixed or Non-residential" in {
            forAll(generateMixedAndNonResidentialPropertyTypes){
              propertyType  =>
                val testRequest = createRequest(freehold, propertyType, LocalDate.of(2012, 4, 1), Some(ReliefFrom15PercentRate), isLinked = Some(true))
                val result = createResult("freeHoldReliefFrom15PercentRateBefore17March2016")

                reset(mockFreeholdCalculationService)

                when(mockFreeholdCalculationService.freeHoldReliefFrom15PercentRateBefore17March2016).thenReturn(result)

                testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

                verify(mockFreeholdCalculationService, times(1)).freeHoldReliefFrom15PercentRateBefore17March2016
            }
          }
        }
      }
    }
    "fallback on normal calculation for freeholdSelfAssessedRes with taxReliefDetails" when {
      "given a request with relief code AcquisitionRelief(14), leasehold, residential property type on or after 04/12/2014 and isLinked = true " in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2015, 3, 6), Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
      "given a request with relief code AcquisitionRelief(14), freehold, residential property type on or after 04/12/2014 and isLinked = false " in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2015, 3, 6), taxReliefCode = Some(AcquisitionRelief), isLinked = Some(false))
        val result = createResult("freeholdAcquisitionTaxRelief")

        when(mockFreeholdCalculationService.freeholdAcquisitionTaxRelief(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionTaxRelief(testRequest)
      }
      "given a request with relief code RightToBuy(22), leasehold, residential property type on or after 04/12/2014 and isLinked = true " in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2015, 3, 6), taxReliefCode = Some(RightToBuy), isLinked = Some(true))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
      "given a request with relief code RightToBuy(22), freehold, residential property type on or after 04/12/2014 and isLinked = false " in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2015, 3, 6), taxReliefCode = Some(RightToBuy), isLinked = Some(false))
        val result = createResult("freeholdResidentialDec14Onwards")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
      }
      "given a request with taxReliefCode ReliefFrom15PercentRate(35), freehold, isLinked = false " when {
        "effective date is before 17th of March 2016" when {
          "property type is Mixed or Non-residential" in {
            forAll(generateMixedAndNonResidentialPropertyTypes){
              propertyType =>
                val testRequest = createRequest(freehold, propertyType, LocalDate.of(2012, 3, 16), taxReliefCode = Some(ReliefFrom15PercentRate), isLinked = Some(false))
                val result = createResult("freeHoldNonResidentialOrMixedPropertyOnOrBefore17March2016")

                reset(mockFreeholdCalculationService)
                when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)
                testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
                verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
            }
          }

        }
        "effective date is after 17th of March 2016" when {
          "property type is Mixed or Non-residential" in {
            forAll(generateMixedAndNonResidentialPropertyTypes){
              propertyType =>
                val testRequest = createRequest(freehold, propertyType, LocalDate.of(2021, 3, 16), taxReliefCode = Some(ReliefFrom15PercentRate), isLinked = Some(false))
                val result = createResult("freeHoldNonResidentialOrMixedPropertyOnOrAfter17March2016")

                reset(mockFreeholdCalculationService)
                when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any())).thenReturn(Seq(result))
                testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
                verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any())
            }

          }
        }
      }
    }

    "select the freeHoldSelfAssessedRes function given the request without taxReliefDetails" when {
      "property type is nonResidential, date is onOrAfter 17/03/2016 and isLinked = true" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 17), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResMar16Onwards
      }
      "property type is mixed, date is onOrAfter 17/03/2016 and isLinked = true" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2017, 3, 18), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResMar16Onwards
      }
      "property type is residential, date is onOrAfter 22/03/2012 and is on or before 14/12/2013 and isLinked = true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 3, 17), isLinked = Some(true))
        val result = createResult("freeholdResidentialMar12BeforeDec14")

        when(mockFreeholdCalculationService.freeholdResidentialMar12BeforeDec14).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12BeforeDec14
      }
      "property type is residential, date is onOrAfter 4/12/2014 and isLinked = true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2014, 12, 4), isLinked = Some(true))
        val result = createResult("freeholdResidentialAfterDec14")

        when(mockFreeholdCalculationService.freeholdResidentialAfterDec14).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAfterDec14
      }
      "property type is residential additional property, date is onOrAfter 1/4/2016 and isLinked = true" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), isLinked = Some(true), isAddProp = true)
        val result = createResult("freeholdResidentialAddPropAprOnwards")

        when(mockFreeholdCalculationService.freeholdResidentialAddPropAprOnwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAddPropAprOnwards
      }
      "given the request holding type is freehold, property type is mixed property effective date is before 17/03/2017 and isLinked = false " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 6), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }
      "given the request property type is mixed property effective date is before 17/03/2017 and isLinked = true " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2017, 3, 16), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResMar16Onwards
      }
      "given the request property type is non residential property effective date is before 17/03/2017 and isLinked = true " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2017, 3, 16), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResMar16Onwards")

        when(mockFreeholdCalculationService.freeholdMixedNonResMar16Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResMar16Onwards
      }
    }
    "fallback on normal calculation for freeholdSelfAssessedRes without taxReliefDetails" when {
      "given the request holding type is leasehold, property type is mixed property effective date is before 17/03/2017 and isLinked = true " in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2016, 3, 6), isLinked = Some(true))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }
      "given the request holding type is leasehold, property type is non residential property effective date is before 17/03/2017 and isLinked = true " in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2016, 3, 6), isLinked = Some(true))
        val result = createResult("leaseholdNonResidentialMar12toMar16")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(any(), any(), any())
      }
      "given the request holding type is freehold property type is residential property effective date is before 17/03/2017 and isLinked = true :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 3, 6), isLinked = Some(true))

        val result = createResult("freeholdResidentialAfterDec14")

        when(mockFreeholdCalculationService.freeholdResidentialAfterDec14).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialAfterDec14
      }
      "given the request holding type is freehold, property type is non residential property effective date is after 17/03/2017 and isLinked = None " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2018, 3, 6))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
      }
      "given the request holding type is freehold, property type is non residential property effective date is before 17/03/2017 and isLinked = false " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2018, 3, 6), isLinked = Some(false))
        val result = createResult("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any())).thenReturn(Seq(result))

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any())
      }
    }

    "select the leaseholdSelfAssessedRes function" when {
      "given a request with for a leasehold mixed property an effective date of 12/03/2008 (minimum mixed date inclusive)" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.mixed, LocalDate.of(2008, 3, 12))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }

      "given a request with for a leasehold mixed property an effective date of 16/03/2016 (day before 17/03/2016)" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.mixed, LocalDate.of(2016, 3, 16))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }

      "given a request with relief code FirstTimeBuyersRelief and property type is Residential with no twoOrMoreProperties and effective date on or after 25/3/2010 or before 25/3/2012" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2012, 3, 23), taxReliefCode = Some(FirstTimeBuyersRelief))
        val result = createResult("leaseholdResFTBReliefMar10BeforeMar12")

        when(mockLeaseholdCalculationService.leaseholdResFTBReliefMar10BeforeMar12).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResFTBReliefMar10BeforeMar12
      }

      "given no taxReliefDetails" when {
        "transaction type is Leasehold, date is on or after 22/11/2017, and isLinked is true" in {

          val result = createResult("leaseholdNov17Onwards")

          forAll(leaseholdNoTaxReliefGenerator(LocalDate.of(2017, 11, 22), onOrAfter = true)) {
            leaseholdNoTaxReliefRequest =>
              reset(mockLeaseholdCalculationService)

              when(mockLeaseholdCalculationService.leaseholdNov17Onwards).thenReturn(result)
              testCalculationService.calculateTax(leaseholdNoTaxReliefRequest) shouldBe CalculationResponse(Seq(result))
              verify(mockLeaseholdCalculationService, times(1)).leaseholdNov17Onwards
          }
        }
      }
    }

    "should fall back to normal calculation" when {
      "given a request with relief code FirstTimeBuyersRelief & Residential Additional Property" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2012, 3, 23), taxReliefCode = Some(FirstTimeBuyersRelief), isAddProp = true)
        val result = createResult("leaseholdResidentialMar12toDec14")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any())
      }

      "given a request with relief code FirstTimeBuyerRelief & Non-residential Property " in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2012, 3, 23), taxReliefCode = Some(FirstTimeBuyersRelief))
        val result = createResult("leaseholdNonResidentialMar12toMar16")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(any(), any(), any())
      }

      "given a relief code FirstTimeBuyerRelief, is freehold & effective date is after the maximum allowed date :: falls back to calculateNoTaxRelief" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2012, 3, 26), taxReliefCode = Some(FirstTimeBuyersRelief), isLinked = Some(true))
        val result = createResult("freeholdResidentialMar12BeforeDec14")

        when(mockFreeholdCalculationService.freeholdResidentialMar12BeforeDec14).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12BeforeDec14
      }


      "given a relief code FirstTimeBuyerRelief, is leasehold & effective date before the minimum allowed date" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2008, 3, 11), taxReliefCode = Some(FirstTimeBuyersRelief))
        val ex = the [InvalidDateException] thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        ex.getMessage mustBe s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }

      "given a relief code FirstTimeBuyerRelief is leasehold & effective date after the maximum allowed date" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2016, 3, 17), taxReliefCode = Some(FirstTimeBuyersRelief))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }

      "given relief code RightToBuy but isLinked = false" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = Some(RightToBuy))
        val result = createResult("freeholdResidentialDec14Onwards")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
      }

      "given a tax relief code RightToBuy(22)" when {
        "Residential property & isLinked = true" when {
          "effective date is between  2014/12/4 - 2016/03/31 (including these dates)" in {
            val testRequest = createRequest(freehold, residential, LocalDate.of(2014, 12, 14), taxReliefCode = Some(RightToBuy))
            val result = createResult("freeholdResidentialDec14Onwards")

            when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
          }
          "effective date is in between 2012/3/22 - 2014/12/3 (including these dates) " in {

            val testRequest = createRequest(freehold, residential, LocalDate.of(2012, 4, 12), taxReliefCode = Some(RightToBuy))
            val result = createResult("freeholdResidentialMar12toDec14")

            when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any())
          }
          "effective date is before 2012/03/22" in {
            val testRequest = createRequest(freehold, residential, LocalDate.of(2010, 4, 12), taxReliefCode = Some(RightToBuy))
            val ex = the [InvalidDateException] thrownBy {
              testCalculationService.calculateTax(testRequest)
            }

            ex.getMessage mustBe s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
          }
        }

        "Mixed property and isLinked = false" when {
          "effective date is before 17th of March 2016" in {
            val testRequest = createRequest(freehold, mixed, LocalDate.of(2010, 4, 12), isLinked = Some(false), taxReliefCode = Some(RightToBuy))
            val result = createResult("freeholdRightToBuyBeforeMarch2016")

            when(mockFreeholdCalculationService.freeholdRightToBuyBeforeMarch2016(any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdRightToBuyBeforeMarch2016(any())

          }
          "effective date is on or after 17th of March 2016" in {
            val testRequest = createRequest(freehold, mixed, LocalDate.of(2021, 3, 16), isLinked = Some(false), taxReliefCode = Some(RightToBuy))
            val result = createResult("freeholdNonResidentialMar16Onwards")

            when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any())).thenReturn(Seq(result))

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any())

          }
        }

        "Non-residential property and isLinked = false" when {
          "effective date is before 17th of March 2016" in {
            val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(false))
            val result = createResult("freeholdRightToBuyBeforeMarch2016")

            when(mockFreeholdCalculationService.freeholdRightToBuyBeforeMarch2016(any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdRightToBuyBeforeMarch2016(any())
          }
          "effective date is on or after 17th of March 2016" in {
            val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2021, 3, 16), taxReliefCode = Some(RightToBuy), isLinked = Some(false))
            val result = createResult("freeholdNonResidentialMar16Onwards")

            when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any())).thenReturn(Seq(result))

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any())
          }
        }
      }

      "Tax Relief Code is ReliefFrom15PercentRate(35)" when {
        "holding type is Leasehold" in {
          val testRequest = createRequest(leasehold, residential, LocalDate.of(2013, 4, 6), taxReliefCode = Some(ReliefFrom15PercentRate), isLinked = Some(true))
          val result = createResult("leaseholdResidentialMar12toDec14")

          when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any())).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any())
        }

        "property type is Non-residential" in {
          val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2013, 4, 6), isLinked = Some(true), taxReliefCode = Some(ReliefFrom15PercentRate))
          val result = createResult("freeholdSelfAssessedRes")

          when(mockFreeholdCalculationService.freeHoldReliefFrom15PercentRateBefore17March2016).thenReturn(result)
          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeHoldReliefFrom15PercentRateBefore17March2016
        }

        "property type is Residential Additional Property and date is before 1/4/2016" in {
          val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 3, 31), isLinked = Some(true), taxReliefCode = Some(ReliefFrom15PercentRate), isAddProp = true)

          val result = createResult("ResidentialAdditionalPropertyDec14Onwards")

          when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
        }

        "effective date is before 6/4/2013" in {
          val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 4, 5), isLinked = Some(false), taxReliefCode = Some(ReliefFrom15PercentRate))
          val result = createResult("freeholdResidentialMar12toDec14")

          when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any())).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any())
        }

        "isLinked is false" in {
          val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 4, 6), taxReliefCode = Some(ReliefFrom15PercentRate))
          val result = createResult("freeholdResidentialMar12toDec14")

          when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any())).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any())
        }
      }

      "transaction type is Leasehold, date is before 22/11/2017, and isLinked is true, false, or None" when {
        "property type is Residential" in {

          val result = createResult("leasehold residential")

          forAll(generateIsLinkedAllPossibleValues) {
            isLinkedValue =>
            val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 21), isLinked = isLinkedValue)

            reset(mockLeaseholdCalculationService)

            when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
          }
        }

        "property type is Non-residential" in {

          val result = createResult("leaseholdNonResidentialMar16Onwards")

          forAll(generateIsLinkedAllPossibleValues) {
            isLinkedValue =>
              val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2017, 11, 21), isLinked = isLinkedValue)

              reset(mockLeaseholdCalculationService)

              when(mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards(any())).thenReturn(Seq(result))

              testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

              verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar16Onwards(any())
          }
        }
      }

      "property type is residential, date is onOrAfter 4/12/2014 and isLinked is false or none" in {

        val result = createResult("freehold residential")

        forAll(generateIsLinkedFalseAndNoneValue) {
          isLinkedValue =>
            val testRequest = createRequest(freehold, residential, LocalDate.of(2014, 12, 4), isLinked = isLinkedValue)

            reset(mockFreeholdCalculationService)

            when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
        }
      }
    }

    "select the leaseholdSelfAssessedOnOrAfterNov2017 function" when {
      "given a request where relief code ~ TaxReliefCode is one of: [8|9|10|11|12|13|15|16|17|18|19|20|21|23|24|26|27|28|29|31]~ with effective date 22/11/2017" in {
        // no additional property in scope
        forAll(leaseHoldAnyPropertyTypeAndTaxReliefSet(LocalDate.of(2017, 11, 23), isLinked = true)) {
          calRequest =>
            reset(mockLeaseholdCalculationService)
            when(mockLeaseholdCalculationService.leaseholdSelfAssessedOnOrAfterNov2017)
              .thenReturn(Result(
                totalTax = 0,
                resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
                resultHint = None,
                npv = None,
                taxCalcs = Seq.empty
              ))
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(calRequest)
            verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedOnOrAfterNov2017
            res shouldBe CalculationResponse(Seq(Result(
              totalTax = 0,
              resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
              resultHint = None,
              npv = None,
              taxCalcs = Seq.empty
            )))
        }

        // with additional property
        forAll(leaseHoldAnyPropertyTypeAndTaxReliefSet(LocalDate.of(2017, 11, 23), isLinked = true)) {
          calcRequest =>
            val calcRequestWithAdditionalProperty = calcRequest
              .copy(propertyDetails = Some(PropertyDetails(
                individual = true,
                twoOrMoreProperties = Some(true),
                replaceMainResidence = Some(true),
                sharedOwnership = None,
                currentValue = None
              )))
            reset(mockLeaseholdCalculationService)
            when(mockLeaseholdCalculationService.leaseholdSelfAssessedOnOrAfterNov2017)
              .thenReturn(Result(
                totalTax = 0,
                resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
                resultHint = None,
                npv = None,
                taxCalcs = Seq.empty
              ))
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(calcRequestWithAdditionalProperty)
            verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedOnOrAfterNov2017
            res shouldBe CalculationResponse(Seq(Result(
              totalTax = 0,
              resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
              resultHint = None,
              npv = None,
              taxCalcs = Seq.empty
            )))
        }
      }
    }

    "given no relief code, property type is Residential Additional Property, effective date is before 1/4/2016 and isLinked = true" in {
      val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 3, 31), isAddProp = true)
      val result = createResult("freeholdResidentialDec14Onwards")

      when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)

      testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
    }

    "given no relief code, property type is Residential Additional Property, effective date is on or after 1/4/2016 and isLinked = false or None" in {

      val result = createResult("freeholdResidentialAdditionalPropertyApr16")

      forAll(generateIsLinkedFalseAndNoneValue) {
        isLinkedValue =>
          val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 4, 1), isLinked = isLinkedValue, isAddProp = true)

          reset(mockFreeholdCalculationService)

          when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any(), any(), any())).thenReturn(result)
          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any(), any(), any())
      }
    }

    "return self assessed AcquisitionRelief(14)" when {
      "given Residential property type and isLinked" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 11, 22), isLinked = Some(true), taxReliefCode = Some(AcquisitionRelief))
        val result = createResult("freeholdAcquisitionReliefBeforeDec2014")

        when(mockFreeholdCalculationService.freeholdAcquisitionReliefBeforeDec2014).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionReliefBeforeDec2014
      }
    }
    "return self assessed MultipleDwellingRelief(33)" when {
      "given Freehold & Residential property type" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("freeholdMultipleDwellingRelief")

        when(mockFreeholdCalculationService.freeholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMultipleDwellingRelief
      }
      "given Freehold Non-residential property type" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("freeholdMultipleDwellingRelief")

        when(mockFreeholdCalculationService.freeholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMultipleDwellingRelief
      }

      "given Leasehold Residential property type" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("leaseholdMultipleDwellingRelief")

        when(mockLeaseholdCalculationService.leaseholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMultipleDwellingRelief
      }

      "given Leasehold Non-residential property type" in {
        val testRequest = createRequest(leasehold, nonResidential, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("leaseholdMultipleDwellingRelief")

        when(mockLeaseholdCalculationService.leaseholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMultipleDwellingRelief
      }

      "given Leasehold mixed property type" in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("leaseholdMultipleDwellingRelief")

        when(mockLeaseholdCalculationService.leaseholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMultipleDwellingRelief
      }

      "given Freehold mixed property type" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2013, 11, 22), taxReliefCode = Some(MultipleDwellingRelief))
        val result = createResult("freeholdMultipleDwellingRelief")

        when(mockFreeholdCalculationService.freeholdMultipleDwellingRelief).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMultipleDwellingRelief
      }
    }

    "given no taxRelief details, for Freehold Non-residential property type " when {
      "effective date is before 2016/3/17 and isLinked = true" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), isLinked = Some(true))
        val result = createResult("freeholdMixedNonResBeforeMar16")

        when(mockFreeholdCalculationService.freeholdMixedNonResBeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdMixedNonResBeforeMar16
      }
      "effective date is before 2016/3/17 and isLinked = false" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "effective date is before 2016/3/17 and isLinked = None" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), None)
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
      }
      "effective date is on or after 2016/3/17 and isLinked = false or None " in {
        forAll(generateIsLinkedFalseAndNoneValue) {
          isLinkedValue =>

            reset(mockFreeholdCalculationService)

            val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2019, 3, 16), isLinked = isLinkedValue)
            val result = createResultInSeq("freeholdNonResidentialMar16Onwards")

            when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any())).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

            verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any())
        }
      }
    }

    "select the freeholdNonResidentialMar12toMar16 function" when {
      "holding type is freehold, property type is Mixed, transaction is not linked & date is before 17/03/2016" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 16), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
      }
      "holding type is freehold, property type is Non-residential, transaction is not linked & date is before 17/03/2016" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 16), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(any(), any())
      }
    }

    "return Fallback result AcquisitionRelief(14)" when {
      "given a Leasehold holding type" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2013, 1, 21), Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("leaseholdResidentialMar12toDec14")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any())
      }

      "given effective date after 4/12/2014 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2015, 1, 21), Some(AcquisitionRelief), isLinked = Some(true))
        val result = createResult("freeholdAcquisitionReliefDec14Onwards")

        when(mockFreeholdCalculationService.freeholdAcquisitionReliefDec14Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionReliefDec14Onwards
      }

      "isLinked is false" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2013, 1, 21), Some(AcquisitionRelief), isLinked = Some(false))
        val result = createResult("freeholdAcquisitionTaxRelief")

        when(mockFreeholdCalculationService.freeholdAcquisitionTaxRelief(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionTaxRelief(any())
      }
    }

    "throw an exception for leasehold mixed" when {
      "given a request with tax relief code FirstTimeBuyersRelief(32) and effective date of 21/1/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2013, 1, 21), Some(FirstTimeBuyersRelief), isLinked = Some(true))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }

      "given a request with tax relief code ReliefFrom15PercentRate(35) and effective date of 6/4/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(leasehold, mixed, LocalDate.of(2013, 4, 6), Some(ReliefFrom15PercentRate), isLinked = Some(true))
        val result = createResult("leaseholdMixedPropMar08BeforeMar16")

        when(mockLeaseholdCalculationService.leaseholdMixedPropMar08BeforeMar16).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdMixedPropMar08BeforeMar16
      }
    }

    "return fall-back result" when {
      "given no taxReliefDetails, date is 22/11/2017, and isLinked is true, false, or None" in {

        forAll(generateIsLinkedAllPossibleValues) {
          isLinkedValue =>
            val testRequest = createRequest(leasehold, mixed, LocalDate.of(2017, 11, 21), isLinked = isLinkedValue)
            val result = createResult("leaseholdNonResidentialMar16Onwards")

            reset(mockLeaseholdCalculationService)

            when(mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards(testRequest)).thenReturn(Seq(result))

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar16Onwards(testRequest)
        }
      }

      "given a request without taxRelief details for FreeHold mixed property type" when {
        "isLinked = false and for any Dates " in {
          val testRequest = createRequest(freehold, mixed, LocalDate.of(2024, 3, 16), isLinked = Some(false))
          val result = createResult("leaseholdNonResidentialMar16Onwards")

          when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(Seq(result))

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
        }
        "isLinked = None and for any Dates " in {
          val testRequest = createRequest(freehold, mixed, LocalDate.of(2024, 3, 16), None)
          val result = createResult("leaseholdNonResidentialMar16Onwards")

          when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(Seq(result))

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
        }
      }
    }

    "return self-assessed for FREEHOLD when interestTransferred contains OT" in {
      val testRequest = createRequest(freehold, residential, LocalDate.of(2017, 11, 22)).copy(interestTransferred = Some("OT"))
      val result = createResult("freeholdOtherInterestTransferred")

      when(mockFreeholdCalculationService.freeholdOtherInterestTransferred).thenReturn(result)

      testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      verify(mockFreeholdCalculationService, times(1)).freeholdOtherInterestTransferred
    }

    "return self-assessed for LEASEHOLD when interestTransferred contains OT" in {
      val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22)).copy(interestTransferred = Some("OT"))
      val result = createResult("leaseholdOtherInterestTransferred")

      when(mockLeaseholdCalculationService.leaseholdOtherInterestTransferred).thenReturn(result)

      testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      verify(mockLeaseholdCalculationService, times(1)).leaseholdOtherInterestTransferred
    }

    // SDLT - Tax Calc Case - 61 - self assessed
    "select the leaseholdResidentialFTBWithMultipleLands function" when {
      "leasehold & Residential & FirstTimeBuyersRelief & isLinked & isMultipleLand & date on or after 22/11/2017" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = Some(true))
        val result = createResult("leaseholdResidentialFTBWithMultipleLands")

        when(mockLeaseholdCalculationService.leaseholdResidentialFTBWithMultipleLands).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialFTBWithMultipleLands
      }
      "leasehold & Residential & FirstTimeBuyersRelief & isLinked & isMultipleLand & date is before 08/07/2020" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 7), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = Some(true))
        val result = createResult("leaseholdResidentialFTBWithMultipleLands")

        when(mockLeaseholdCalculationService.leaseholdResidentialFTBWithMultipleLands).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialFTBWithMultipleLands
      }
    }
    "not select the leaseholdResidentialFTBWithMultipleLands function" when {
      "isMultipleLand is false" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 7), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = Some(false))
        val result = createResult("leaseholdNov17Onwards")

        when(mockLeaseholdCalculationService.leaseholdNov17Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdNov17Onwards
      }
      "isMultipleLand is None" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 7), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = None)
        val result = createResult("leaseholdNov17Onwards")

        when(mockLeaseholdCalculationService.leaseholdNov17Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdNov17Onwards
      }
      "isLinked is false" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 7), Some(FirstTimeBuyersRelief), isLinked = Some(false), isMultipleLand = Some(true))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
      "isLinked is None" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 7), Some(FirstTimeBuyersRelief), isLinked = None, isMultipleLand = Some(true))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
      "date is not before 08/07/2020" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2020, 7, 8), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = Some(true))
        val result = createResult("leaseholdNov17Onwards")

        when(mockLeaseholdCalculationService.leaseholdNov17Onwards).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdNov17Onwards
      }
      "date is not after 22/11/2017" in {
        val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 21), Some(FirstTimeBuyersRelief), isLinked = Some(true), isMultipleLand = Some(true))
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any(), any(), any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, never).leaseholdResidentialFTBWithMultipleLands
        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any(), any(), any(), any())
      }
    }
  }

  "checkFTB" must {
    "return true" when {
      "the user is an individual who does not own twoOrMoreProperties and the premium < 500000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(true), 499999) shouldBe true
      }
    }

    "return false" when{
      "the user is not an individual regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 499999) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but FTB is false regardless of premium"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 500000) shouldBe false
      }

      "the user is an individual who owns twoOrMoreProperties regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 499999) shouldBe false
      }

      "the property details are not defined regardless of premium" in{
        val propertyDetails = None
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 500000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but the premium > 500000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(true), 500001) shouldBe false
      }

    }
  }

  "checkFTBHigherThreshold" must {
    "return true" when {
      "the user is an individual who does not own twoOrMoreProperties and the premium < 625000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(true), 624000) shouldBe true
      }
    }

    "return false" when{
      "the user is not an individual regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 624000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but FTB is false regardless of premium"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 625000) shouldBe false
      }

      "the user is an individual who owns twoOrMoreProperties regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 624000) shouldBe false
      }

      "the property details are not defined regardless of premium" in{
        val propertyDetails = None
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 625000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but the premium > 625000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(true), 626000) shouldBe false
      }
    }
  }
}