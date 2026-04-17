/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.taxCalculation

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PropertyAndHoldingTypesSpec extends AnyFreeSpec with Matchers {

  "HoldingTypes.fromCode" - {

    "must map L (grant of lease) to leasehold" in {
      HoldingTypes.fromCode("L") mustBe Some(HoldingTypes.leasehold)
    }

    "must map F (conveyance transfer) to freehold" in {
      HoldingTypes.fromCode("F") mustBe Some(HoldingTypes.freehold)
    }

    "must map A (conveyance transfer lease) to freehold" in {
      HoldingTypes.fromCode("A") mustBe Some(HoldingTypes.freehold)
    }

    "must map O (other transaction) to freehold" in {
      HoldingTypes.fromCode("O") mustBe Some(HoldingTypes.freehold)
    }

    "must return None for an unrecognised description" in {
      HoldingTypes.fromCode("X") mustBe None
    }
  }

  "PropertyTypes.fromCode" - {

    "must map 01 to residential" in {
      PropertyTypes.fromCode("01") mustBe Some(PropertyTypes.residential)
    }

    "must map 04 (additional) to residential" in {
      PropertyTypes.fromCode("04") mustBe Some(PropertyTypes.residential)
    }

    "must map 02 to mixed" in {
      PropertyTypes.fromCode("02") mustBe Some(PropertyTypes.mixed)
    }

    "must map 03 to nonResidential" in {
      PropertyTypes.fromCode("03") mustBe Some(PropertyTypes.nonResidential)
    }

    "must return None for an unrecognised code" in {
      PropertyTypes.fromCode("99") mustBe None
    }
  }
}
