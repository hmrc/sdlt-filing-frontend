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
import models.purchaser.PurchaserTypeOfCompanyAnswers
import models.{CompanyDetails, FullReturn, Purchaser}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.FullName
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

object PurchaserSummary {

  def getSummaryCards(fullReturn: FullReturn)(implicit messages: Messages): Option[Seq[SummaryList]] = {
    val mainPurchaserId = fullReturn.returnInfo.flatMap(_.mainPurchaserID)
    val purchasersOpt: Option[Seq[Purchaser]] = fullReturn.purchaser
    purchasersOpt.map { purchasers =>
      purchasers.map { purchaser =>
        val isFirstPurchaser = mainPurchaserId.contains(purchaser.purchaserID.getOrElse(""))
        getSummaryCard(purchaser, fullReturn.companyDetails, isFirstPurchaser)
      }
    }.map(_.flatMap(_.toSeq)).filter(_.nonEmpty)
  }

  private def getSummaryCard(purchaser: Purchaser, companyDetails: Option[CompanyDetails], isFirstPurchaser: Boolean)(implicit messages: Messages): Option[SummaryList] = {
    val firstPurchaserKeySuffix = if isFirstPurchaser then ".first" else ""
    val purchaserNameOpt = purchaser.isCompany match {
      case Some(isCompany) if isCompany.equalsIgnoreCase("YES") => purchaser.companyName
      case Some(isCompany) if isCompany.equalsIgnoreCase("NO") => FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
      case _ => None
    }

    purchaserNameOpt.map { purchaserName =>
      SummaryListViewModel(
        Seq(
          getOptSummaryRow(
            messages("purchaser.whoIsMakingThePurchase.checkYourAnswersLabel"),
            purchaser.isCompany.map(isCompany =>
              if isCompany.equalsIgnoreCase("YES") then messages(s"purchaser.whoIsMakingThePurchase.Company.checkYourAnswersLabel")
              else messages(s"purchaser.whoIsMakingThePurchase.Individual.checkYourAnswersLabel")
            )
          ),
          getOptSummaryRow(
            messages("purchaser.nameOfThePurchaser.checkYourAnswersLabel", purchaserName),
            Some(purchaserName)
          ),
          getOptSummaryRowHtml(
            messages("purchaser.checkYourAnswers.purchaserAddress.label"),
            purchaser.address1.map(address1 =>
              HtmlContent(toHtml(
                Address(
                  line1 = address1,
                  line2 = purchaser.address2,
                  line3 = purchaser.address3,
                  line4 = purchaser.address4,
                  postcode = purchaser.postcode
                )
              ))
            )
          ),
          getOptSummaryRow(
            messages("purchaser.enterPhoneNumber.checkYourAnswersLabel", purchaserName),
            purchaser.phone
          ),
          getOptSummaryRow(
            messages("purchaser.nationalInsurance.checkYourAnswersLabel", purchaserName),
            purchaser.nino
          ),
          getOptSummaryRow(
            messages("purchaser.corporationTaxUTR.checkYourAnswersLabel"),
            companyDetails.flatMap(_.UTR)
          ),
          getOptSummaryRow(
            messages("purchaser.registrationNumber.checkYourAnswersLabel"),
            companyDetails.flatMap(_.VATReference)
          ),
          getOptSummaryRowHtml(
            messages("purchaser.formOfIdIndividual.checkYourAnswersLabel", purchaserName),
            (purchaser.registrationNumber, purchaser.placeOfRegistration) match {
              case (Some(regNumber), Some(placeOfReg)) =>
                Some(HtmlContent(HtmlFormat.escape(regNumber).toString + "<br/>" + HtmlFormat.escape(placeOfReg).toString))
              case _ => None
            }
          ),
          getOptSummaryRowHtml(
            messages("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel"),
            getCompanyTypeRow(companyDetails)
          ),
          getOptSummaryRow(
            messages("purchaser.isPurchaserActingAsTrustee.checkYourAnswersLabel", purchaserName),
            getOptYesNo(purchaser.isTrustee)
          ),
          getOptSummaryRow(
            messages("purchaser.purchaserAndVendorConnected.checkYourAnswersLabel", purchaserName),
            getOptYesNo(purchaser.isConnectedToVendor)
          )
        ).flatMap(_.toSeq)
      ).withCard(
        messages(
          s"submission.completedSdltReturn.purchaser.header$firstPurchaserKeySuffix",
          FullName.fullName(purchaser.forename1, purchaser.forename2, purchaser.surname.getOrElse(""))
        )
      )
    }
  }

  private def getCompanyTypeRow(companyDetailsOpt: Option[CompanyDetails])(implicit messages: Messages): Option[HtmlContent] = {
    companyDetailsOpt.flatMap { companyDetails =>
      val typeOfCompanyAnswers = PurchaserTypeOfCompanyAnswers.fromCompanyDetails(companyDetails)

      val selectedItems = PurchaserTypeOfCompanyAnswers.toSet(typeOfCompanyAnswers).toSeq.sortBy(_.order).map(_.toString)
      if (selectedItems.nonEmpty) {
        Some(HtmlContent(
          selectedItems.map {
            answer => HtmlFormat.escape(messages(s"purchaser.purchaserTypeOfCompany.$answer")).toString
          }.mkString("<br>")
        ))
      } else None
    }
  }
}
