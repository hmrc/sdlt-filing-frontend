/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.ResultText.{RESULT_HEADING_GENERIC, RESULT_HEADING_TAX_RELIEF, RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT}
import enums.HoldingTypes._
import enums.PropertyTypes._
import enums.sdltRebuild._
import enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import exceptions.{InvalidDateException, RequiredValueNotDefinedException}
import generators.RequestGenerators
import models._
import models.sdltRebuild.TaxReliefDetails
import org.mockito.ArgumentMatchers.{any, eq => meq}
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

    def createResult(msg: String) = Result(
      totalTax = 0,
      resultHeading = Some(msg),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDetails)
    )

    def createSelfAssessedResult(msg: String) = Result(
      totalTax = 0,
      resultHeading = Some(msg),
      resultHint = None,
      npv = None,
      taxCalcs = Seq.empty
    )

    def createResultInSeq(msg: String) = Seq(
      createResult(msg)
    )

    def createRequest(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate, isLinked: Option[Boolean] = None) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = None,
      premium = BigDecimal(0),
      highestRent = BigDecimal(0),
      propertyDetails = None,
      leaseDetails = None,
      relevantRentDetails = None,
      interestTransferred = None,
      isLinked = isLinked,
      taxReliefDetails = None,
      firstTimeBuyer = None
    )

    def createRequestWithPropDetails(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate, premiumAmount: BigDecimal= 0, nonUKResident: Option[Boolean] = None, twoOrMoreProp: Option[Boolean] = Some(false)) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = nonUKResident,
      premium = premiumAmount,
      highestRent = BigDecimal(0),
      propertyDetails =  Some(
        PropertyDetails(
          individual = true,
          twoOrMoreProperties = twoOrMoreProp,
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

    def createRequestWithTaxRelief(hType: HoldingTypes.Value,
                                   pType: PropertyTypes.Value, eDate: LocalDate,
                                   premiumAmount: BigDecimal= 0,
                                   twoOrMoreProperties: Option[Boolean] = Some(false),
                                   replaceMainResidence: Option[Boolean] = Some(false),
                                   nonUKResident: Option[Boolean] = None,
                                   taxReliefCode: TaxReliefCode,
                                   isPartialRelief: Option[Boolean] = Some(false),
                                   isLinked: Option[Boolean] = None
                                  ) = {
      Request(
        holdingType = hType,
        propertyType = pType,
        effectiveDate = eDate,
        nonUKResident = nonUKResident,
        premium = premiumAmount,
        highestRent = BigDecimal(0),
        propertyDetails = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = twoOrMoreProperties,
            replaceMainResidence = replaceMainResidence,
            sharedOwnership = None,
            currentValue = None
          )
        ),
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = Some(true),
        interestTransferred = None,
        isLinked = isLinked,
        taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = taxReliefCode, isPartialRelief = isPartialRelief)),
      )
    }

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

    "select the leaseholdResidential additional property of an individual for April2016 onwards" when {
      "when given a request with effective date is on 1/4/2016 when replace main residence checks true taxReliefCode is PreCompletionTransaction" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2016,4,1),
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(true),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = None,
          isLinked = Some(false)
        )

        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
      }
      "when given a request with effective date is after 1/4/2016 when replace main residence checks true  taxReliefCode is PreCompletionTransaction" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2021,6,1),
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(true),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = None,
          isLinked = Some(false)
        )

        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
      }

      "given relief code PreCompletionTransaction but effective date is before 1/4/2016 should fall back to normal calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2016,3,31),
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(true),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = None,
          isLinked = Some(false)
        )

        noException shouldBe thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        verify(mockLeaseholdCalculationService, never).leaseholdZeroRateTaxReliefRes(any())
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

    "select freeHold / residential property with tax relief code" when {
      "given relief code PartExchange" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = PartExchange,
          isPartialRelief = None,
          isLinked = Some(false)
        )
        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given relief code FreeportsTaxSiteRelief(36)" in {
        val freeportTestRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2000, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(freeportTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given freehold | property type: residental and non-residental | zero rate taxReliefCode :: expect return zero tax" in {
        val calcDetails = CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
        val expectedRes = Result(
          totalTax = 0,
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = None,
          npv = None,
          taxCalcs = Seq(calcDetails)
        )

        val zeroRateTaxReliefForFreehold =
          Result(
            totalTax = 0,
            resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
            resultHint = None,
            npv = None,
            taxCalcs = Seq(
              CalculationDetails(
                taxType = TaxTypes.premium,
                calcType = CalcTypes.slab,
                taxDue = 0,
                detailHeading = None,
                bandHeading = None,
                detailFooter = None,
                rate = Some(0),
                slices = None
              )
            )
          )

        forAll( freeHoldRequestWithStandardPropertyTypesGenerator ) {
          freeHoldRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(zeroRateTaxReliefForFreehold)
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(freeHoldRequest)
            verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
            res shouldBe CalculationResponse(Seq(expectedRes))
        }
      }

      "given leasehold | non-residential and mixed property types | zero rate taxReliefCode :: expect return zero tax" in {
        val calcDetails = CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
        val expectedRes = Result(
          totalTax = 0,
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = None,
          npv = None,
          taxCalcs = Seq(calcDetails)
        )

        val zeroRateTaxReliefForLeaseHold: Result =
          Result(
            totalTax = 0,
            resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
            resultHint = None,
            npv = None,
            taxCalcs = Seq(
              CalculationDetails(
                taxType = TaxTypes.premium,
                calcType = CalcTypes.slab,
                taxDue = 0,
                detailHeading = None,
                bandHeading = None,
                detailFooter = None,
                rate = Some(0),
                slices = None
              )
            )
          )

        forAll( leasedHoldNonResidentialMixedRequestGenerator ) {
          freeHoldRequest =>
            reset(mockLeaseholdCalculationService)
            when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(any()))
              .thenReturn(zeroRateTaxReliefForLeaseHold)
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(freeHoldRequest)
            verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
            res shouldBe CalculationResponse(Seq(expectedRes))
        }
      }

      "given selfAssessed freeHold with before 04/12/2014 | taxReliefCode :: expect return zero tax" in {
        val calcDetails = CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
        val expectedRes = Result(
          totalTax = 0,
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = None,
          npv = None,
          taxCalcs = Seq(calcDetails)
        )

        val zeroRateTaxReliefSelfAssessedFreeHold: Result =
          Result(
            totalTax = 0,
            resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
            resultHint = None,
            npv = None,
            taxCalcs = Seq(
              CalculationDetails(
                taxType = TaxTypes.premium,
                calcType = CalcTypes.slab,
                taxDue = 0,
                detailHeading = None,
                bandHeading = None,
                detailFooter = None,
                rate = Some(0),
                slices = None
              )
            )
          )

        forAll( freeHoldSelfAssessedBeforeDateRequestGenerator ) {
          freeHoldSelfAssessedBeforeDateRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdSelfAssessedRes)
              .thenReturn(zeroRateTaxReliefSelfAssessedFreeHold)
            val res: CalculationResponse = testCalculationService.calculateTaxRelief(freeHoldSelfAssessedBeforeDateRequest)
            verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
            res shouldBe CalculationResponse(Seq(expectedRes))
        }
      }

      "given relief code InvestmentZonesTaxSiteRelief(37)" in {
        val investmentTestRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2000, 11, 22),
          taxReliefCode = InvestmentZonesTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(investmentTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given self-assessed residential Freeport relief" in {
        val freeportTestRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2000, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(true),
          isLinked = Some(false)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(freeportTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }

      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = None
        )

        noException shouldBe thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        verify(mockFreeholdCalculationService, never)
          .freeholdZeroRateTaxReliefRes
      }

      "given relief code AcquisitionRelief(14)" in {
        val AcquisitionReliefTestRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2000, 11, 22),
          premiumAmount = 1000000,
          taxReliefCode = AcquisitionRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdAcquisitionTaxRelief(AcquisitionReliefTestRequest)).thenReturn(result)

        testCalculationService.calculateTax(AcquisitionReliefTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionTaxRelief(AcquisitionReliefTestRequest)
      }

      "given relief code Right to buy transactions(22)" in {
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        forAll( freeHoldRightToBuy ) {
          freeHoldRightToRequest =>
            reset(mockFreeholdCalculationService)
            when(mockFreeholdCalculationService.freeholdRightToBuyBeforeMarch2016(freeHoldRightToRequest)).thenReturn(result)
            testCalculationService.calculateTax(freeHoldRightToRequest) shouldBe CalculationResponse(Seq(result))
            verify(mockFreeholdCalculationService, times(1)).freeholdRightToBuyBeforeMarch2016(freeHoldRightToRequest)
        }
      }
    }

    "select freehold / non-residential property with tax relief code" when {
      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        noException shouldBe thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        verify(mockFreeholdCalculationService, never)
          .freeholdZeroRateTaxReliefRes
      }
    }

    "select freehold / mixed property with tax relief code" when {
      "given a request with relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdZeroRateTaxReliefRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdZeroRateTaxReliefRes
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to standard calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any(), any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }

      "given relief code ReliefFrom15PercentRate(35) :: falls back to calculateNoTaxRelief" in {
        val testRequest = createRequestWithTaxRelief(
          freehold,
          mixed,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = ReliefFrom15PercentRate,
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }
    }

    "select leasehold / residential property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe
          CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1))
          .leaseholdZeroRateTaxReliefRes(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        noException shouldBe thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        verify(mockLeaseholdCalculationService, never)
          .leaseholdZeroRateTaxReliefRes(any())
      }
    }

    "select leasehold / non-residential property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe
          CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1))
          .leaseholdZeroRateTaxReliefRes(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to normal calculation" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        noException shouldBe thrownBy {
          testCalculationService.calculateTax(testRequest)
        }

        verify(mockLeaseholdCalculationService, never)
          .leaseholdZeroRateTaxReliefRes(any())
      }
    }

    "select leasehold / mixed property with tax relief code" when {
      "given relief code PreCompletionTransaction(34) and effective date of 6/4/2013" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe
          CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1))
          .leaseholdZeroRateTaxReliefRes(any())
      }

      "given relief code PreCompletionTransaction(34) and effective date before 6/4/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 4, 5),
          taxReliefCode = PreCompletionTransaction,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe
          CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1))
          .leaseholdSelfAssessedRes

      }
    }

    "select leaseHold freePort / residential property with tax relief code" when {
      "given relief code FreeportsTaxSiteRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
      }
      "given relief code InvestmentZonesTaxSiteRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = InvestmentZonesTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())

      }
      "given isPartialRelief is true with residential leasehold transaction" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(true),
          isLinked = Some(false)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
      }
    }
    "select leaseHold freePort / non-residential property with tax relief code" when {
      "given relief code FreeportsTaxSiteRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
      }
      "given relief code InvestmentZonesTaxSiteRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = InvestmentZonesTaxSiteRelief,
          isPartialRelief = Some(false),
          isLinked = Some(false)
        )
        val result = createResult("Results of calculation based on SDLT rules for the effective date entered")

        when(mockLeaseholdCalculationService.leaseholdZeroRateTaxReliefRes(testRequest.leaseDetails)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdZeroRateTaxReliefRes(any())
      }
      "given isPartialRelief is true with non-residential leasehold transaction" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2017, 11, 22),
          taxReliefCode = FreeportsTaxSiteRelief,
          isPartialRelief = Some(true),
          isLinked = Some(false)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
      }

      "given relief code AcquisitionRelief(14)" in {
        val AcquisitionReliefTestRequest = Request(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2012, 3, 23),
          nonUKResident = None,
          premium = 1000000,
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = Some(LeaseDetails(
            startDate = LocalDate.of(2012, 3, 23),
            endDate = LocalDate.of(2013, 3, 23),
            leaseTerm = LeaseTerm(
              years = 1,
              days = 1,
              daysInPartialYear = 365
            ),
            year1Rent = 999,
            year2Rent = Some(999),
            None,
            None,
            None
          )),
          relevantRentDetails = None,
          firstTimeBuyer = None,
          isLinked = Some(false),
          interestTransferred = None,
          taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = AcquisitionRelief, isPartialRelief = None))
        )

        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockLeaseholdCalculationService.leaseholdAcquisitionTaxReliefRes(AcquisitionReliefTestRequest)).thenReturn(result)

        testCalculationService.calculateTax(AcquisitionReliefTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdAcquisitionTaxReliefRes(AcquisitionReliefTestRequest)
      }

    }

    "Given relief code CollectiveEnfranchisementByLeaseholders(25)" when{
        "effective date is on or after 23/04/2009 and is freehold" in {
          val testRequest = Request(
            HoldingTypes.freehold,
            PropertyTypes.residential,
            APRIL2009_EFFECTIVE_DATE,
            nonUKResident = None,
            premium = 1000000,
            highestRent = BigDecimal(0),
            propertyDetails = None,
            leaseDetails = None,
            relevantRentDetails = None,
            firstTimeBuyer = None,
            isLinked = None,
            interestTransferred = None,
            taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = CollectiveEnfranchisementByLeaseholders, isPartialRelief = None))
          )

          val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF)

          when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
        }

      "effective date is before 23/04/2009" in {
        val badTestRequest = Request(
          HoldingTypes.freehold,
          PropertyTypes.nonResidential,
          LocalDate.of(2009, 4, 22),
          nonUKResident = None,
          premium = 0,
          highestRent = BigDecimal(10000),
          propertyDetails = None,
          leaseDetails = None,
          relevantRentDetails = None,
          firstTimeBuyer = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = CollectiveEnfranchisementByLeaseholders, isPartialRelief = None))
        )
        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], meq(false)))
          .thenReturn(result)

        testCalculationService.calculateTax(badTestRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1))
          .freeholdNonResidentialMar12toMar16(any[Request], meq(false))

        verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
      }

      "effective date is on or after 23/04/2009 and is leasehold" in {
        val testRequest = Request(
          HoldingTypes.leasehold,
          PropertyTypes.nonResidential,
          APRIL2009_EFFECTIVE_DATE,
          nonUKResident = None,
          premium = 1000000,
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = Some(LeaseDetails(
            startDate = LocalDate.of(2009, 3, 23),
            endDate = LocalDate.of(2010, 3, 23),
            leaseTerm = LeaseTerm(
              years = 1,
              days = 1,
              daysInPartialYear = 365
            ),
            year1Rent = 999,
            year2Rent = Some(999),
            None,
            None,
            None
          )),
          relevantRentDetails = None,
          firstTimeBuyer = None,
          isLinked = None,
          interestTransferred = None,
          taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = CollectiveEnfranchisementByLeaseholders, isPartialRelief = None))
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
      }
    }


    "select the freeholdSelfAssessedRes function" when {
      "given a request with relief code RightToBuy and property Type is Residential with twoOrMoreProperties and isLinked = true" in {

        val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = RightToBuy, twoOrMoreProperties = Some(true), isLinked = Some(true))
        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }

      "given a request with relief code ReliefFrom15PercentRate :: property type is Residential, effective date is 6/4/2013 and isLinked is true" in {
        val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2013, 4, 6), taxReliefCode = ReliefFrom15PercentRate, isLinked = Some(true))
        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }

      "given a request with relief code ReliefFrom15PercentRate :: property type is Residential, effective date is 3/12/2014 and isLinked is true" in {
        val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2014, 12, 3), taxReliefCode = ReliefFrom15PercentRate, isLinked = Some(true))
        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }
      "given the request with tax relief code  RightToBuy  and property type is  mixed property effective date is before 17/03/2016 and isLinked = true " in {
        val testRequest = createRequestWithTaxRelief(freehold, mixed, LocalDate.of(2016, 3, 16), taxReliefCode = RightToBuy, twoOrMoreProperties = Some(false), isLinked = Some(true))
        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }
      "given the request with tax relief code  RightToBuy  and property type is  nonResidential property effective date is before 17/03/2016 and isLinked = true " in {

        val testRequest = createRequestWithTaxRelief(freehold, nonResidential, LocalDate.of(2016, 3, 16), taxReliefCode = RightToBuy, twoOrMoreProperties = Some(false), isLinked = Some(true))
        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }
      "given the request property type is mixed property effective date is before 17/03/2017 and isLinked = true " in {
        val rawRequest = createRequest(freehold, mixed, LocalDate.of(2017, 3, 16))
        val testRequest = rawRequest.copy(isLinked = Some(true))

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }
      "given the request property type is non residential property effective date is before 17/03/2017 and isLinked = true " in {

        val rawRequest = createRequest(freehold, nonResidential, LocalDate.of(2017, 3, 16))
        val testRequest = rawRequest.copy(isLinked = Some(true))

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }
    }
    "fallback on normal calculation for freeholdSelfAssessedRes" when {
      "given the request holding type is leasehold, property type is mixed property effective date is before 17/03/2017 and isLinked = true " in {
        val rawRequest = createRequest(leasehold, mixed, LocalDate.of(2016, 3, 6))
        val testRequest = rawRequest.copy(isLinked = Some(true))

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
        verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
      }
      "given the request holding type is leasehold, property type is non residential property effective date is before 17/03/2017 and isLinked = true " in {
        val rawRequest = createRequest(leasehold, nonResidential, LocalDate.of(2016, 3, 6))
        val testRequest = rawRequest.copy(isLinked = Some(true))

        val result = createResult("leaseholdNonResidentialMar12toMar16")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(any[Request](), meq(false), meq(None))).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(any[Request], meq(false), meq(None))
        verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
      }
      "given the request holding type is freehold property type is residential property effective date is before 17/03/2017 and isLinked = true :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequest(freehold, residential, LocalDate.of(2016, 3, 6), isLinked = Some(true))

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }
      "given the request holding type is freehold, property type is non residential property effective date is after 17/03/2017 and isLinked = None " in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2018, 3, 6))

        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(testRequest)
        verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
      }
      "given the request holding type is freehold, property type is non residential property effective date is before 17/03/2017 and isLinked = false " in {
        val rawRequest = createRequest(freehold, nonResidential, LocalDate.of(2018, 3, 6))
        val testRequest = rawRequest.copy(isLinked = Some(false))

        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any[Request])).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar16Onwards(any[Request])
        verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
      }
      "given the request holding type is freehold, property type is mixed property effective date is before 17/03/2017 and isLinked = false " in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 6), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdNonResidentialMar12toMar16(testRequest)
      }
    }

    "select the freeHoldSelfAssessedRes function given the request with no taxReliefDetails" when {
      "property type is nonResidential, date is onOrAfter 17/03/2016 and isLinked = true" in {
        val testRequest = createRequest(
          freehold,
          nonResidential,
          LocalDate.of(2016, 3, 17),
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }
      "property type is mixed, date is onOrAfter 17/03/2016 and isLinked = true" in {
        val testRequest = createRequest(
          freehold,
          mixed,
          LocalDate.of(2017, 3, 18),
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }

      "property type is residential, date is onOrAfter 22/03/2012 and is on or before 14/12/2013 and isLinked = true" in {
        val testRequest = createRequest(
          freehold,
          residential,
          LocalDate.of(2013, 3, 17),
        ).copy(isLinked = Some(true))

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes

      }

      "property type is residential, date is onOrAfter 4/12/2014 and isLinked = true" in {
        val testRequest = createRequest(
          freehold,
          residential,
          LocalDate.of(2014, 12, 4),
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) mustEqual CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }
    }

    "select the leaseholdSelfAssessedRes function" when {
      "given a request with for a leasehold mixed property an effective date of 12/03/2008 (minimum mixed date inclusive)" in {

        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.mixed, LocalDate.of(2008, 3, 12))
        val result = createSelfAssessedResult("Self-assessed")

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
        verifyNoMoreInteractions(mockFreeholdCalculationService)
      }

      "given a request with for a leasehold mixed property an effective date of 16/03/2016 (day before 17/03/2016)" in {

        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.mixed, LocalDate.of(2016, 3, 16))
        val result = createSelfAssessedResult("Self-assessed")

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
        verifyNoMoreInteractions(mockFreeholdCalculationService)
      }

      "given a request with relief code FirstTimeBuyersRelief and property type is Residential with no twoOrMoreProperties and effective date on or after 25/3/2010 or before 25/3/2012" in {

        val testRequest = createRequestWithTaxRelief(leasehold, residential, LocalDate.of(2011, 6, 1), twoOrMoreProperties = Some(false), taxReliefCode = FirstTimeBuyersRelief)
        val result = createSelfAssessedResult("Self-assessed")

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
        verify(mockLeaseholdCalculationService, never).leaseholdZeroRateTaxReliefRes(any())
        verifyNoMoreInteractions(mockFreeholdCalculationService)
      }

      "given no taxReliefDetails" when {
        "transaction type is Leasehold, date is on or after 22/11/2017, and isLinked is true" in {

          val result = createSelfAssessedResult("Self-assessed")

          forAll(leaseholdNoTaxReliefGenerator(LocalDate.of(2017, 11, 22), onOrAfter = true)) {
            leaseholdNoTaxReliefRequest =>
              reset(mockLeaseholdCalculationService)

              when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)
              testCalculationService.calculateTax(leaseholdNoTaxReliefRequest) shouldBe CalculationResponse(Seq(result))
              verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
          }
        }
      }
    }

    "should fall back to normal calculation" when {
      "given a request with relief code FirstTimeBuyersRelief & Residential Additional Property" in {

        val testRequest = createRequestWithTaxRelief(leasehold, residential, LocalDate.of(2012, 3, 23), twoOrMoreProperties = Some(true), taxReliefCode = FirstTimeBuyersRelief)
        val result = createResult("leaseholdResidentialMar12toDec14")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any[Request])).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))


        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any[Request])
        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
      }

      "given a request with relief code FirstTimeBuyerRelief & Non-residential Property " in {

        val testRequest = createRequestWithTaxRelief(leasehold, nonResidential, LocalDate.of(2012, 3, 23), taxReliefCode = FirstTimeBuyersRelief)
        val result = createResult("leaseholdNonResidentialMar12toMar16")

        when(mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16(any[Request], meq(false), meq(None))).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar12toMar16(any[Request], meq(false), meq(None))

        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
      }

      "given a request with relief code FirstTimeBuyerRelief & HoldingType of Freehold" in {

        val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2012, 1, 1), taxReliefCode = FirstTimeBuyersRelief)

        intercept[InvalidDateException] {
          testCalculationService.calculateTaxRelief(testRequest)
        }

        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
        verifyNoMoreInteractions(mockLeaseholdCalculationService)
      }

      "given a relief code FirstTimeBuyerRelief & effective date before the minimum allowed date" in {

        val testRequest = createRequestWithTaxRelief(leasehold, residential, LocalDate.of(2008, 3, 11), taxReliefCode = FirstTimeBuyersRelief)
        val ex = the [InvalidDateException] thrownBy {
          testCalculationService.calculateTaxRelief(testRequest)
        }

        ex.getMessage mustBe s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"

        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
        verifyNoMoreInteractions(mockLeaseholdCalculationService)
      }

      "given a relief code FirstTimeBuyerRelief & effective date after the maximum allowed date" in {

        val testRequest = createRequestWithTaxRelief(leasehold, residential, LocalDate.of(2016, 3, 17), taxReliefCode = FirstTimeBuyersRelief)
        val result = createResult("leaseholdResidentialDec14Onwards")

        when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any[Request], meq(false), meq(None), meq(false))).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any[Request], meq(false), meq(None), meq(false))

        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
      }

      "given relief code RightToBuy but isLinked = false" in {

        val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2016, 4, 1), taxReliefCode = RightToBuy)
        val result = createResult("freeholdResidentialDec14Onwards")

        when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any[Request](), meq(false), meq(false))).thenReturn(result)
        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))
      }

      "given a tax relief code  RightToBuy , Residential property & isLinked = true " when {
        "effective date is between  2014/12/4 - 2016/03/31(including these dates)" in {

          val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2014, 12, 14), taxReliefCode = RightToBuy)
          val result = createResult("freeholdResidentialDec14Onwards")

          when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any[Request](), meq(false), meq(false))).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))
        }
        "effective date is in between 2012/3/22 - 2014/12/3(including these dates) " in {

          val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2012, 4, 12), taxReliefCode = RightToBuy)
          val result = createResult("freeholdResidentialMar12toDec14")

          when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any[Request]())).thenReturn(result)

          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any[Request])
        }
        "effective date is before 2012/03/22" in {

          val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2010, 4, 12), taxReliefCode = RightToBuy)
          val ex = the [InvalidDateException] thrownBy {
            testCalculationService.calculateTaxRelief(testRequest)
          }

          ex.getMessage mustBe s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"

          verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
          verifyNoMoreInteractions(mockLeaseholdCalculationService)


        }

        "given no relief code, property type is Residential Additional Property, effective date is 1/4/2016 and isLinked = true" in {

          val testRequest = createRequestWithPropDetails(
            freehold,
            residential,
            LocalDate.of(2016, 4, 1),
            twoOrMoreProp = Some(true)
          ).copy(isLinked = Some(true))
          val result = createResult("freeholdResidentialAdditionalPropertyDec14")

          when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))).thenReturn(result)
          testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))
          verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
        }

      }

      "Tax Relief Code is ReliefFrom15PercentRate(35)" when {
        "holding type is Leasehold" in {
          val testRequest = createRequestWithTaxRelief(leasehold, residential, LocalDate.of(2013, 4, 6), taxReliefCode = ReliefFrom15PercentRate, isLinked = Some(true))
          val result = createResult("leaseholdResidentialMar12toDec14")

          when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any[Request])).thenReturn(result)
          testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any[Request])
          verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
        }

        "property type is Non-residential" in {
          val testRequest = createRequestWithTaxRelief(freehold, nonResidential, LocalDate.of(2013, 4, 6), taxReliefCode = ReliefFrom15PercentRate, isLinked = Some(true))
          val result = createResult("freeholdSelfAssessedRes")

          when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)
          testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
        }

        "effective date is before 6/4/2013" in {
          val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2013, 4, 5), taxReliefCode = ReliefFrom15PercentRate, isLinked = Some(false))
          val result = createResult("freeholdResidentialMar12toDec14")

          when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any[Request])).thenReturn(result)
          testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any[Request])
          verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
        }

        "isLinked is false" in {
          val testRequest = createRequestWithTaxRelief(freehold, residential, LocalDate.of(2013, 4, 6), taxReliefCode = ReliefFrom15PercentRate)
          val result = createResult("freeholdResidentialMar12toDec14")

          when(mockFreeholdCalculationService.freeholdResidentialMar12toDec14(any[Request])).thenReturn(result)
          testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

          verify(mockFreeholdCalculationService, times(1)).freeholdResidentialMar12toDec14(any[Request])
          verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
        }
      }

      "transaction type is Leasehold, date is before 22/11/2017, and isLinked is true, false, or None" when {
        "property type is Residential" in {

          val result = createResult("leasehold residential")

          forAll(generateIsLinkedAllPossibleValues) {
            isLinkedValue =>
            val testRequest = createRequest(
              HoldingTypes.leasehold,
              PropertyTypes.residential,
              LocalDate.of(2017, 11, 21),
              isLinked = isLinkedValue
            )

            reset(mockLeaseholdCalculationService)

            when(mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards(any[Request], meq(false), meq(None), meq(false))).thenReturn(result)
            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialDec14Onwards(any[Request], meq(false), meq(None), meq(false))
            verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
          }
        }

        "property type is Non-residential" in {

          val result = createResultInSeq("leasehold non-residential")

          forAll(generateIsLinkedAllPossibleValues) {
            isLinkedValue =>
              val testRequest = createRequest(
                HoldingTypes.leasehold,
                PropertyTypes.nonResidential,
                LocalDate.of(2017, 11, 21),
                isLinked = isLinkedValue
              )

              reset(mockLeaseholdCalculationService)

              when(mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards(any[Request])).thenReturn(result)
              testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

              verify(mockLeaseholdCalculationService, times(1)).leaseholdNonResidentialMar16Onwards(any[Request])
              verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
          }
        }
      }

      "property type is residential, date is onOrAfter 4/12/2014 and isLinked is false or none" in {

        val result = createResult("freehold residential")

        forAll(generateIsLinkedFalseAndNoneValue) {
          isLinkedValue =>
            val testRequest = createRequest(
              freehold,
              residential,
              LocalDate.of(2014, 12, 4),
              isLinked = isLinkedValue
            )

            reset(mockFreeholdCalculationService)

            when(mockFreeholdCalculationService.freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))).thenReturn(result)
            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

            verify(mockFreeholdCalculationService, times(1)).freeholdResidentialDec14Onwards(any[Request], meq(false), meq(false))
            verify(mockFreeholdCalculationService, never).freeholdSelfAssessedRes
        }

      }
    }

    "return self assessed AcquisitionRelief(14)" when {
      "given Residential property type and isLinked" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 11, 22),
          taxReliefCode = AcquisitionRelief,
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }
    }

    "given no taxRelief details, for Freehold Non-residential property type " when {
      "effective date is before 2016/3/17 and isLinked = true" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), isLinked = Some(true))

        val result = createResult(RESULT_HEADING_GENERIC)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      }
      "effective date is before 2016/3/17 and isLinked = false" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), isLinked = Some(false))

        val result = createResult(RESULT_HEADING_GENERIC)

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      }

      "effective date is before 2016/3/17 and isLinked = None" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2012, 3, 16), None)

        val result = createResult(RESULT_HEADING_GENERIC)

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      }
      "effective date is on or after 2016/3/17 and isLinked = false or None " in {
        forAll(generateIsLinkedFalseAndNoneValue) {
          isLinkedValue =>
            val testRequest = createRequest(
              freehold,
              nonResidential,
              LocalDate.of(2019, 3, 16),
              isLinked = isLinkedValue
            )

            val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

            when(mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards(any[Request])).thenReturn(result)

            testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)

        }

      }

    }

    "select the freeholdNonResidentialMar12toMar16 function" when {
      "holding type is freehold, property type is Mixed, transaction is not linked & date is before 17/03/2016" in {
        val testRequest = createRequest(freehold, nonResidential, LocalDate.of(2016, 3, 16), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16, March2016")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
      "holding type is freehold, property type is Non-residential, transaction is not linked & date is before 17/03/2016" in {
        val testRequest = createRequest(freehold, mixed, LocalDate.of(2016, 3, 16), isLinked = Some(false))
        val result = createResult("freeholdNonResidentialMar12toMar16, March2016")

        when(mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16(any[Request], any())).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }


    "return Fallback result AcquisitionRelief(14)" when {
      "given a Leasehold holding type" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 1, 21),
          taxReliefCode = AcquisitionRelief,
          isLinked = Some(true)
        )

        val result = createResult("leaseholdResidentialMar12toDec14")

        when(mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14(any[Request])).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))


        verify(mockLeaseholdCalculationService, times(1)).leaseholdResidentialMar12toDec14(any[Request])
        verify(mockLeaseholdCalculationService, never).leaseholdSelfAssessedRes
      }

      "given effective date after 4/12/2014 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2015, 1, 21),
          taxReliefCode = AcquisitionRelief,
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)
        testCalculationService.calculateTaxRelief(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
      }

      "isLinked is false" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.freehold,
          PropertyTypes.residential,
          LocalDate.of(2013, 1, 21),
          taxReliefCode = AcquisitionRelief,
          isLinked = Some(false)
        )

        val result = createResult(RESULT_HEADING_TAX_RELIEF)

        when(mockFreeholdCalculationService.freeholdAcquisitionTaxRelief(testRequest)).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockFreeholdCalculationService, times(1)).freeholdAcquisitionTaxRelief(testRequest)
      }
    }

    "throw an exception for leasehold mixed" when {
      "given a request with tax relief code FirstTimeBuyersRelief(32) and effective date of 21/1/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 1, 21),
          taxReliefCode = FirstTimeBuyersRelief,
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
      }

      "given a request with tax relief code ReliefFrom15PercentRate(35) and effective date of 6/4/2013 :: falls back to calculateTaxNoRelief" in {
        val testRequest = createRequestWithTaxRelief(
          HoldingTypes.leasehold,
          PropertyTypes.mixed,
          LocalDate.of(2013, 4, 6),
          taxReliefCode = ReliefFrom15PercentRate,
          isLinked = Some(true)
        )

        val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

        when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

        verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
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
      val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

      when(mockFreeholdCalculationService.freeholdSelfAssessedRes).thenReturn(result)

      testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      verify(mockFreeholdCalculationService, times(1)).freeholdSelfAssessedRes
    }

    "return self-assessed for LEASEHOLD when interestTransferred contains OT" in {
      val testRequest = createRequest(leasehold, residential, LocalDate.of(2017, 11, 22)).copy(interestTransferred = Some("OT"))
      val result = createSelfAssessedResult(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT)

      when(mockLeaseholdCalculationService.leaseholdSelfAssessedRes).thenReturn(result)

      testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))

      verify(mockLeaseholdCalculationService, times(1)).leaseholdSelfAssessedRes
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