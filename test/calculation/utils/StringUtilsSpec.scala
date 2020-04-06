/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.utils

import uk.gov.hmrc.play.test.UnitSpec

class StringUtilsSpec extends UnitSpec {

  "Calling intToMonetaryString" should {
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
