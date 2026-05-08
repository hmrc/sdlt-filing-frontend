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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.YesNoHelper.toYesNo

class YesNoHelperSpec extends AnyFreeSpec with Matchers {

  private implicit val messages: Messages = stubMessages()

  ".toYesNo" - {

    "Rights site.yes for 'yes'" in {
      toYesNo("yes") mustBe Right("site.yes")
    }

    "Rights site.no for 'no'" in {
      toYesNo("no") mustBe Right("site.no")
    }

    "is case-insensitive" in {
      toYesNo("Yes") mustBe Right("site.yes")
      toYesNo("NO")  mustBe Right("site.no")
      toYesNo("yEs") mustBe Right("site.yes")
    }

    "Lefts an UnrecognisedYesNoError for anything else" in {
      toYesNo("maybe") mustBe Left(UnrecognisedYesNoError)
    }
  }
}
