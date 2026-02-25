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
import pages.purchaser.{AddPurchaserPhoneNumberPage, NameOfPurchaserPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AddPurchaserPhoneNumberSummarySpec extends SpecBase {

  "AddPurchaserPhoneNumberSummarySpec" - {

    "must return a SummaryListRow with 'yes' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(AddPurchaserPhoneNumberPage, true).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test2"), name = "Test")).success.value


        val result = AddPurchaserPhoneNumberSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel",userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))

        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.yes")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.addPurchaserPhoneNumber.change.hidden")
      }
    }

    "must return a SummaryListRow with 'no' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(AddPurchaserPhoneNumberPage, false).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test2"), name = "Test")).success.value


        val result = AddPurchaserPhoneNumberSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel",userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))


        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.no")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.addPurchaserPhoneNumber.change.hidden")
      }
    }

    "when userAnswers is None" - {

      "must return a summary list row with missing link" in {

        val userAnswers = emptyUserAnswers.set(AddPurchaserPhoneNumberPage, false).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test2"), name = "Test")).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = AddPurchaserPhoneNumberSummary.row(None)

          result.key.content.asHtml.toString() contains  msgs("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include(controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(CheckMode).url)
          valueHtml  contains(msgs("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel"))
        }
      }
    }
  }
}