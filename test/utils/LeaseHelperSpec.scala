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

package utils

import base.SpecBase
import constants.FullReturnConstants.{completeLease, emptyFullReturn}
import models.Lease
import utils.LeaseHelper.isLeaseDefined

class LeaseHelperSpec extends SpecBase {

  "LeaseHelper.isLeaseDefined" - {

    "must return true when lease is complete" in {
      val fullReturn = emptyFullReturn.copy(
        lease = Some(completeLease)
      )
      isLeaseDefined(fullReturn) mustBe true
    }

    "must return true when lease is defined but empty" in {
      val fullReturn = emptyFullReturn.copy(
        lease = Some(Lease())
      )
      isLeaseDefined(fullReturn) mustBe true
    }

    "must return false when lease is not defined" in {
      val fullReturn = emptyFullReturn
      isLeaseDefined(fullReturn) mustBe false
    }
  }
}