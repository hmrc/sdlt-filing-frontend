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

package viewmodels.checkAnswers.purchaser

import base.SpecBase
import models.CheckMode
import models.purchaser.NameOfPurchaser
import pages.purchaser.{NameOfPurchaserPage, PurchaserCompanyTypeKnownPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class PurchaserCompanyTypeKnownSummarySpec extends SpecBase {

  "PurchaserCompanyTypeKnownSummarySpec" - {

    "must return a SummaryListRow with 'yes' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(PurchaserCompanyTypeKnownPage, true).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = None, forename2 = None, name = "Test")).success.value


        val row = PurchaserCompanyTypeKnownSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.purchaserCompanyTypeKnown.checkYourAnswersLabel", userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))

        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.yes")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.purchaserCompanyTypeKnown.change.hidden", userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))
      }
    }

    "must return a SummaryListRow with 'no' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(PurchaserCompanyTypeKnownPage, false).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = None, forename2 = None, name = "Test")).success.value


        val row = PurchaserCompanyTypeKnownSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.purchaserCompanyTypeKnown.checkYourAnswersLabel", userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))


        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.no")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.purchaserCompanyTypeKnown.change.hidden", userAnswers.get(NameOfPurchaserPage).map(_.fullName).getOrElse(""))
      }
    }

    "when userAnswers is None" - {

      "must return a Missing and redirect call to missing page when data is not present" in {

        val userAnswers = emptyUserAnswers.set(PurchaserCompanyTypeKnownPage, false).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = None, forename2 = None, name = "Test")).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserCompanyTypeKnownSummary.row(None)
          result match {
            case Missing(call) =>
              call mustEqual controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }


}
