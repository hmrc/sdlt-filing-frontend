package calculation.services

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.PropertyDetails
import uk.gov.hmrc.play.test.UnitSpec
import org.scalamock.scalatest.MockFactory

class RefundEntitlementServiceSpec extends UnitSpec with MockFactory {

  val refundService = new RefundEntitlementService

  val individualPropertyDetails = PropertyDetails(
    individual = true,
    twoOrMoreProperties = Some(false),
    replaceMainResidence = None
  )

  val nonIndividualPropertyDetails = PropertyDetails(
    individual = false,
    twoOrMoreProperties = None,
    replaceMainResidence = None
  )

  "calculateRefundEntitlement" should {
    "return None" when {
      "previous tax is more than current tax" in {
        refundService.calculateRefundEntitlement(100000, 100001, Some(individualPropertyDetails)) shouldBe None
      }
      "previous tax is the same as current tax" in {
        refundService.calculateRefundEntitlement(100000, 100000, Some(individualPropertyDetails)) shouldBe None
      }
      "the purchaser is not an individual" in {
        refundService.calculateRefundEntitlement(100000, 90000, Some(nonIndividualPropertyDetails)) shouldBe None
      }
    }
    "return the correct entitlement" when {
      "the purchaser is an individual with current tax more than previous tax" in {
        refundService.calculateRefundEntitlement(100001, 100000, Some(individualPropertyDetails)) shouldBe Some(1)
      }
    }
    "throw the correct exception" when {
      "property details is required but not defined" in {
        the[RequiredValueNotDefinedException] thrownBy
          refundService.calculateRefundEntitlement(100001, 100000, None) should
            have message "[RefundEntitlementService] [eligibleForRefund] - property details not defined when expected"
      }
    }
  }
}
