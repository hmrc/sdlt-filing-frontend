/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FormatUtilsSpec extends AnyWordSpec with Matchers {

  ".bigDecimalCurrency" should {
    "Place comma in appropriate place when given amount over a thousand" in {
      FormatUtils.bigDecimalFormat(1000.10) shouldBe "£1,000.10"
    }
    "Place comma in appropriate place when given amount over ten thousand" in {
      FormatUtils.bigDecimalFormat(10100.10) shouldBe "£10,100.10"
    }
    "Place comma in appropriate place when given amount over one hundred thousand" in {
      FormatUtils.bigDecimalFormat(100100.10) shouldBe "£100,100.10"
    }
    "Place commas in appropriate place when given amount over one million" in {
      FormatUtils.bigDecimalFormat(1100100.10) shouldBe "£1,100,100.10"
    }
    "Place commas in appropriate place when given amount over one hundred million" in {
      FormatUtils.bigDecimalFormat(100100100.10) shouldBe "£100,100,100.10"
    }
    "Place commas in appropriate place when given amount over ten billion" in {
      FormatUtils.bigDecimalFormat(10100100100.10) shouldBe "£10,100,100,100.10"
    }
  }
}