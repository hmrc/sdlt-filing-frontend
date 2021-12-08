/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._

class StringUtilsSpec extends PlaySpec {

  "Calling intToMonetaryString" must {
    "return '1,200' for 1200" in {
      StringUtils.intToMonetaryString(1200) shouldBe "1,200"
    }

    "return '15,395,342' for 15395342" in {
      StringUtils.intToMonetaryString(15395342) shouldBe "15,395,342"
    }

    "return '993' for 993" in {
      StringUtils.intToMonetaryString(993) shouldBe "993"
    }
  }

}
