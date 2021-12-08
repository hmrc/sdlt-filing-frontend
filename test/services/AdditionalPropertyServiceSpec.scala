/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import exceptions.RequiredValueNotDefinedException
import models.PropertyDetails
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec

class AdditionalPropertyServiceSpec extends PlaySpec {

  val additionalPropertyService = new AdditionalPropertyService

  "Additional property check" must {
    "return true for an individual with additional property and not replacing main residence" when {
      "the property is not leasehold and the premium is 40K" in {
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        additionalPropertyService.additionalPropertyRatesApply(40000, propertyDetails, None) shouldBe true
      }
      "the property is leasehold and the premium is 40K and the lease term is more than 7 years" in {
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )

        additionalPropertyService.additionalPropertyRatesApply(
          premium = 40000, oPropertyDetails = propertyDetails, leaseDetails = Some(8)) shouldBe true
      }
    }
    "return false for an individual with additional property and not replacing main residence" when {
      "the lease is 7 years" in {
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, Some(7)) shouldBe false
      }
      "the lease is less than 7 years" in {
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, Some(6)) shouldBe false
      }
      "the premium is < 40K" in {
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        additionalPropertyService.additionalPropertyRatesApply(39999, propertyDetails, Some(10)) shouldBe false
      }
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
      additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, None) shouldBe false
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
      additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, None) shouldBe false
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
      additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, None) shouldBe true
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
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails,  None))
        .should(have message "[AdditionalPropertyService] [additionalProperty]" +
          s" - individual: true, twoOrMoreProperties: None, replaceMainResidence: Some(false), leaseDetails: None")
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
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, None))
        .should(have message "[AdditionalPropertyService] [additionalProperty]" +
          s" - individual: true, twoOrMoreProperties: Some(true), replaceMainResidence: None, leaseDetails: None")
    }
    "throw the correct exception when propertyDetails is undefined" in {
      val propertyDetails = None
      the[RequiredValueNotDefinedException]
        .thrownBy(additionalPropertyService.additionalPropertyRatesApply(45000, propertyDetails, None))
        .should(have message "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation")
    }
  }
}
