/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.purchaser
import base.SpecBase
import models.CheckMode
import models.purchaser.NameOfPurchaser
import pages.purchaser.{NameOfPurchaserPage, PurchaserAndVendorConnectedPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running


class PurchaserAndVendorConnectedSummarySpec extends SpecBase {

  val purchaserName = NameOfPurchaser(None, None, "AnyPurchaser")

  "PurchaserAndVendorConnectedSummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with full name" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)
          val userAnswers = emptyUserAnswers
            .set(PurchaserAndVendorConnectedPage, true).success.value
            .set(NameOfPurchaserPage,NameOfPurchaser(forename1 = Some("Test"),forename2 = Some("Test2"), name= "Test")).success.value
         
          val result = PurchaserAndVendorConnectedSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.purchaserAndVendorConnected.checkYourAnswersLabel",userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))

          result.value.content.asHtml.toString() mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.purchaserAndVendorConnected.change.hidden")
        }
      }


    }
  }
}