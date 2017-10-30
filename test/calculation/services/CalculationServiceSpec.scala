package calculation.services

import java.time.LocalDate

import calculation.enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import calculation.models.{CalculationDetails, CalculationResponse, Request, Result}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalamock.scalatest.MockFactory

class CalculationServiceSpec extends UnitSpec with MockFactory{

  val mockLeaseholdCalculationService = mock[LeaseholdCalculationService]
  val mockFreeholdCalculationService = mock[FreeholdCalculationService]
  val testCalculationService = new CalculationService(
                                                      mockLeaseholdCalculationService,
                                                      mockFreeholdCalculationService
                                                     )

  def baseCalculationDetails(taxDue: Int, rate: Int) = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slab,
    detailHeading = None,
    bandHeading = None,
    detailFooter = None,
    taxDue = taxDue,
    rate = Some(rate),
    slices = None
  )

  "selectCalculationFunction" should{
    "select the freeholdNonResidential function for March2016 onwards" when{
      "given a request with an effective date of 1/1/2017" in{

        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 1),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

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

        val result = Seq(
          Result(
           totalTax = 0,
           resultHeading = Some("Results based on SDLT rules from 17 March 2016"),
           resultHint = None,
           npv = None,
           taxCalcs = Seq(calcDetails)
          )
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(result)
      }
    }
  }
}
