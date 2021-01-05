/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.services

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.PropertyDetails
import uk.gov.hmrc.play.test.UnitSpec

class AdditionalPropertyServiceSpec extends UnitSpec {

  val additionalPropertyService = new AdditionalPropertyService

  "Additional property check" should {
    "return true for an individual with additional property and not replacing main residence" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
        )
      )
      additionalPropertyService.additionalPropertyRatesApply(propertyDetails) shouldBe true
    }
    "return false for an individual with additional property who is replacing main residence" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(true),
        sharedOwnership = None,
        currentValue = None
        )
      )
      additionalPropertyService.additionalPropertyRatesApply(propertyDetails) shouldBe false
    }
    "return false for an individual with only one property" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
        )
      )
      additionalPropertyService.additionalPropertyRatesApply(propertyDetails) shouldBe false
    }
    "return true for a non-individual" in {
      val propertyDetails = Some(PropertyDetails(
        individual = false,
        twoOrMoreProperties = None,
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
        )
      )
      additionalPropertyService.additionalPropertyRatesApply(propertyDetails) shouldBe true
    }
    "throw the correct exception when twoOrMoreProperties is required but undefined" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = None,
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
        )
      )
      the[RequiredValueNotDefinedException]
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(propertyDetails))
        .should(have message "[AdditionalPropertyService] [additionalProperty]" +
          s" - twoOrMoreProperties: None, replaceMainResidence: Some(false)")
    }
    "throw the correct exception when replaceMainResidence is required but undefined" in {
      val propertyDetails = Some(PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
        )
      )
      the[RequiredValueNotDefinedException]
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(propertyDetails))
        .should(have message "[AdditionalPropertyService] [additionalProperty]" +
          s" - twoOrMoreProperties: Some(true), replaceMainResidence: None")
    }
    "throw the correct exception when propertyDetails is undefined" in {
      val propertyDetails = None
      the[RequiredValueNotDefinedException]
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(propertyDetails))
        .should(have message "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation")
    }
  }
}
