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

package viewmodels.submission.summary

import models.address.Address
import models.address.Address.toHtml
import models.vendor.whoIsTheVendor.{Company, Individual}
import models.vendor.whoIsTheVendor
import models.{FullReturn, Vendor}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.{FullName, SortService}
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

object VendorSummary {

  def getSummaryCards(fullReturn: FullReturn)(implicit messages: Messages): Option[Seq[SummaryList]] = {
    val mainVendorId = fullReturn.returnInfo.flatMap(_.mainVendorID)
    fullReturn.vendor.map { vendors =>
      val sortedVendors = SortService.sortByMainObjectLastUpdateDate[Vendor](vendors, mainVendorId)(_.lastUpdateDate, _.vendorID).filter(_.vendorID.isDefined)
      sortedVendors.map { vendor =>
        getSummaryCard(vendor)
      }
    }.map(_.flatMap(_.toSeq)).filter(_.nonEmpty)
  }

  private def getSummaryCard(vendor: Vendor)(implicit messages: Messages): Option[SummaryList] = {
    val vendorType = if (vendor.forename1.isDefined || vendor.forename2.isDefined) Individual else Company
    val vendorNameOpt = vendorType match {
      case Company => vendor.name
      case Individual => FullName.optionalFullName(vendor.forename1, vendor.forename2, vendor.name)
    }

    vendorNameOpt.map { vendorName =>
      SummaryListViewModel(
        Seq(
          getOptSummaryRow(
            messages("vendor.checkYourAnswers.whoIsTheVendor.label"),
            Some(messages(s"vendor.checkYourAnswers.whoIsTheVendor.${vendorType.toString}"))
          ),
          getOptSummaryRowHtml(
            messages("vendor.checkYourAnswers.vendorAddress.label"),
            vendor.address1.map(address1 =>
              HtmlContent(toHtml(
                Address(
                  line1 = address1,
                  line2 = vendor.address2,
                  line3 = vendor.address3,
                  line4 = vendor.address4,
                  postcode = vendor.postcode
                )
              ))
            )
          )
        ).flatMap(_.toSeq)
      ).withCard(
        messages(
          s"submission.completedSdltReturn.vendor.header",
          vendorName
        )
      )
    }
  }
}
