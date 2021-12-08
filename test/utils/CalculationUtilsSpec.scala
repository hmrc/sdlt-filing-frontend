/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._
import utils.CalculationUtils.{leaseholdNRSDLTInScopeForLeaseOrPremium, leaseholdNRSDLTOutOfScope}

class CalculationUtilsSpec extends PlaySpec {

  ".leaseholdNRSDLTOutOfScope" must {
    "return true" when {
      "premium < 40K and first time buyer is true and shared ownership is true" in {
        leaseholdNRSDLTOutOfScope(39999, 10, 1001, true, true) shouldBe true
      }

      "premium < 40K and highestRent < 1000" in {
        leaseholdNRSDLTOutOfScope(39999, 10, 999, false, false) shouldBe true
      }

      "premium < 40K and leasePeriod < 7 years" in {
        leaseholdNRSDLTOutOfScope(39999, 1, 1001, false, false) shouldBe true
      }
    }
    "return false" when {
      "premium == 40K and first time buyer is true and shared ownership is true" in {
        leaseholdNRSDLTOutOfScope(40000, 10, 1001, true, true) shouldBe false
      }

      "premium > 40K and first time buyer is true and shared ownership is false" in {
        leaseholdNRSDLTOutOfScope(40001, 10, 1001, true, true) shouldBe false
      }

      "premium > 40K and leasePeriod > 7 years" in {
        leaseholdNRSDLTOutOfScope(40001, 8, 1, false, false) shouldBe false
      }
    }
  }

  ".leaseholdNRSDLTInScopeForLeaseOrPremium" must {
    "return true" when {
      "premium == 40K and first time buyer is true and shared ownership is true" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(40000, 10, 1001, true, true) shouldBe true
      }

      "premium > 40K and first time buyer is true and shared ownership is false" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(40001, 10, 1001, true, false) shouldBe true
      }

      "premium > 40K and leasePeriod > 7 years" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(40001, 8, 1, false, false) shouldBe true
      }
    }
    "return false" when {
      "premium < 40K and first time buyer is true and shared ownership is true" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(39999, 10, 1001, true, true) shouldBe false
      }

      "premium < 40K and highestRent < 1000" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(39999, 10, 999, false, false) shouldBe false
      }

      "leasePeriod < 7 years" in {
        leaseholdNRSDLTInScopeForLeaseOrPremium(50000, 1, 1001, false, false) shouldBe false
      }
    }
  }
}
