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
import models.land.LandTypeOfProperty.{Mixed, Residential}
import models.land.{LandInterestTransferredOrCreated, LandTypeOfProperty}
import models.{FullReturn, Land}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

object LandSummary {

  def getSummaryCards(fullReturn: FullReturn)(implicit messages: Messages): Option[Seq[SummaryList]] = {
    fullReturn.land.map { lands =>
      lands.map { land =>
        getSummaryCard(land)
      }
    }.map(_.flatMap(_.toSeq)).filter(_.nonEmpty)
  }

  private def getSummaryCard(land: Land)(implicit messages: Messages): Option[SummaryList] = {
    land.address1.map { address1 =>
      SummaryListViewModel(
        Seq(
          getOptSummaryRow(
            messages("land.landTypeOfProperty.checkYourAnswersLabel"),
            land.propertyType.flatMap(LandTypeOfProperty.fromCode).map(code =>
              messages(s"land.landTypeOfProperty.$code")
            )
          ),
          getOptSummaryRow(
            messages("land.landInterestTransferredOrCreated.checkYourAnswersLabel"),
            land.interestCreatedTransferred.flatMap(LandInterestTransferredOrCreated.fromCode).map(code =>
              messages(s"land.landInterestTransferredOrCreated.$code")
            )
          ),
          getOptSummaryRowHtml(
            messages("land.address.checkYourAnswersLabel"),
            land.address1.map(address1 =>
              HtmlContent(toHtml(
                Address(
                  line1 = address1,
                  line2 = land.address2,
                  line3 = land.address3,
                  line4 = land.address4,
                  postcode = land.postcode
                )
              ))
            )
          ),
          getOptSummaryRow(
            messages("land.localAuthorityCode.checkYourAnswersLabel"),
            land.localAuthorityNumber
          ),
          getOptSummaryRow(
            messages("land.titleNumber.checkYourAnswersLabel"),
            land.titleNumber
          ),
          getOptSummaryRow(
            messages("land.nlpgUprn.checkYourAnswersLabel"),
            land.NLPGUPRN
          ),
          getOptSummaryRow(
            messages("land.landSendingPlanByPost.checkYourAnswersLabel"),
            getOptYesNo(land.willSendPlanByPost)
          ),
          getOptSummaryRow(
            messages("land.landMineralsOrMineralRights.checkYourAnswersLabel"),
            getOptYesNo(land.mineralRights)
          ),
          getOptSummaryRow(
            messages("land.agriculturalOrDevelopmental.checkYourAnswersLabel"),
            getLandAreaKnownYesNo(land)
          ),
          getOptSummaryRow(
            messages("land.doYouKnowTheAreaOfLand.checkYourAnswersLabel"),
            getLandAreaKnownYesNo(land)
          ),
          getOptSummaryRow(
            messages("land.areaOfLand.checkYourAnswersLabel"),
            (land.areaUnit, land.landArea) match {
              case (Some(unit), Some(area)) => Some(s"$area ${messages(s"land.areaOfLand.$unit.suffix")}")
              case _ => None
            }
          )
        ).flatMap(_.toSeq)
      ).withCard(
        messages(
          "submission.completedSdltReturn.land.header",
          address1
        )
      )
    }
  }

  private def getLandAreaKnownYesNo(land: Land)(implicit messages: Messages): Option[String] = {
    val propertyType = land.propertyType.flatMap(LandTypeOfProperty.fromCode)
    (propertyType, land.areaUnit, land.landArea) match {
      case (Some(propertyType), Some(_), Some(_)) if propertyType == Mixed || propertyType == Residential =>
        Some(messages("site.yes"))
      case (Some(propertyType), _, _) if !(propertyType == Mixed || propertyType == Residential) =>
        Some(messages("site.no"))
      case _ =>
        None
    }
  }
}
