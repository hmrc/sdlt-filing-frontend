package calculation.services

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.PropertyDetails
import uk.gov.hmrc.play.test.UnitSpec

class RefundEntitlementServiceSpec extends UnitSpec {

  val refundService = new RefundEntitlementService

  "Additional property check" should {
    "return true for an individual with additional property and not replacing main residence" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false))
      )
      refundService.individualWithAdditionalProperty(propertyDetails) shouldBe true
    }
    "return true for a non-individual" in {
      val propertyDetails = Some(PropertyDetails(
        individual = false,
        twoOrMoreProperties = None,
        replaceMainResidence = None)
      )
      refundService.individualWithAdditionalProperty(propertyDetails) shouldBe false
    }
    "throw the correct exception when twoOrMoreProperties is required but undefined" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = None,
        replaceMainResidence = Some(false))
      )
      the[RequiredValueNotDefinedException]
        .thrownBy(refundService.individualWithAdditionalProperty(propertyDetails))
        .should(have message "[FreeholdCalculationService] [additionalProperty]" +
          s" - twoOrMoreProperties: None, replaceMainResidence: Some(false)")
    }
    "throw the correct exception when replaceMainResidence is required but undefined" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = None)
      )
      the[RequiredValueNotDefinedException]
        .thrownBy(refundService.individualWithAdditionalProperty(propertyDetails))
        .should(have message "[FreeholdCalculationService] [additionalProperty]" +
          s" - twoOrMoreProperties: Some(true), replaceMainResidence: None")
    }
    "throw the correct exception when propertyDetails is undefined" in {
      val propertyDetails = None
      the[RequiredValueNotDefinedException]
        .thrownBy(refundService.individualWithAdditionalProperty(propertyDetails))
        .should(have message "[FreeholdCalculationService] [individualWithAdditionalProperty]" +
          " - property details not defined in freehold residential additional property calculation")
    }
  }


}
