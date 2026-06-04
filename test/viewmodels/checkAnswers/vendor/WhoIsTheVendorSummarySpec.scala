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

package viewmodels.checkAnswers.vendor

import base.SpecBase
import models.CheckMode
import models.vendor.whoIsTheVendor
import pages.vendor.WhoIsTheVendorPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class WhoIsTheVendorSummarySpec extends SpecBase {

  "whoIsTheVendorSummary" - {

    "when Vendor type is present" - {

      "must return a summary list row with Vendor type value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          

          val userAnswers = emptyUserAnswers.set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value

          val row = WhoIsTheVendorSummary.row(Some(userAnswers))
          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.whoIsTheVendor.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          htmlContent mustEqual msgs("vendor.checkYourAnswers.whoIsTheVendor.Company")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.whoIsTheVendor.hidden")
        }
      }

      "must display the correct message for different Vendor types" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val individualOrCompany = Seq(
            whoIsTheVendor.Company,
            whoIsTheVendor.Individual
          )

          individualOrCompany.foreach { individualOrCompany =>
            val userAnswers = emptyUserAnswers
              .set(WhoIsTheVendorPage, individualOrCompany).success.value

            val row = WhoIsTheVendorSummary.row(Some(userAnswers))
            val result = row match {
              case Row(r) => r
              case _ => fail("Expected Row but got Missing")
            }

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"vendor.checkYourAnswers.whoIsTheVendor.$individualOrCompany")
          }
        }
      }

    }

    "when Vendor type is not present" - {

      "must return a Missing and redirect call to missing page when data is not present" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = WhoIsTheVendorSummary.row(Some(emptyUserAnswers))

          result match {
            case Missing(call) =>
              call mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return a Missing and redirect call to missing page when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = WhoIsTheVendorSummary.row(None)

          result match {
            case Missing(call) =>
              call mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}