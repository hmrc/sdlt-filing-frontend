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

package viewmodels.taxCalculation.selfAssessedViewModels

import base.SpecBase
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.taxCalculation.selfAssessedViewModels.CannotCalculateViewModel.toViewModel

class CannotCalculateViewModelSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  ".toViewModel" - {

    "renders CannotCalculateViewModel correctly when List is empty" in {
      val vm = toViewModel(List())
      vm.paragraph           mustEqual Some(messages("taxCalculation.cannotCalculateSdltDue.noReason.p1"))
      vm.additionalParagraph mustEqual Some(messages("taxCalculation.cannotCalculateSdltDue.noReason.p2"))
      vm.bulletPoints        mustEqual Nil
    }

    "renders CannotCalculateViewModel correctly when List has one reason without bullet points" in {
      val vm = toViewModel(List("reason1"))
      vm.paragraph           mustEqual Some(messages("taxCalculation.cannotCalculateSdltDue.p1", messages("taxCalculation.cannotCalculateSdltDue.reason1")))
      vm.additionalParagraph mustEqual None
      vm.bulletPoints        mustEqual Nil
    }

    "renders CannotCalculateViewModel correctly when List has one reason with two bullet points" in {
      val vm = toViewModel(List("reason5"))
      vm.paragraph           mustEqual Some(messages("taxCalculation.cannotCalculateSdltDue.p1", messages("taxCalculation.cannotCalculateSdltDue.reason5")))
      vm.additionalParagraph mustEqual None
      vm.bulletPoints        mustEqual
        Seq(
          "taxCalculation.cannotCalculateSdltDue.reason5.b1",
          "taxCalculation.cannotCalculateSdltDue.reason5.b2"
        )
    }

    "renders CannotCalculateViewModel correctly when List has one reason with three bullet points" in {
      val vm = toViewModel(List("reason7"))
      vm.paragraph mustEqual Some(messages("taxCalculation.cannotCalculateSdltDue.p1", messages("taxCalculation.cannotCalculateSdltDue.reason7")))
      vm.additionalParagraph mustEqual None
      vm.bulletPoints mustEqual
        Seq(
          "taxCalculation.cannotCalculateSdltDue.reason7.b1",
          "taxCalculation.cannotCalculateSdltDue.reason7.b2",
          "taxCalculation.cannotCalculateSdltDue.reason7.b3"
        )
    }

    "renders CannotCalculateViewModel correctly when List has multiple reasons" in {
      val vm = toViewModel(List("reason1", "reason2", "reason5"))
      vm.paragraph mustEqual Some("taxCalculation.cannotCalculateSdltDue.p1.withColon")
      vm.additionalParagraph mustEqual None
      vm.bulletPoints mustEqual
        Seq(
          "taxCalculation.cannotCalculateSdltDue.multipleReasons.b1",
          "taxCalculation.cannotCalculateSdltDue.multipleReasons.b2",
          "taxCalculation.cannotCalculateSdltDue.multipleReasons.b5"
        )
    }
  }
}
