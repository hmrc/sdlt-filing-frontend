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

package utils

import models.{Purchaser, UserAnswers}
import play.api.i18n.Messages
import services.purchaser.PopulatePurchaserService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

import javax.inject.Inject

class PurchaserPaginationHelper @Inject()(populatePurchaserService: PopulatePurchaserService) {

  private val ROWS_ON_PAGE = 15

  def getPaginationInfoText[A](paginationIndex: Int, itemList: Seq[A])
                              (implicit messages: Messages): Option[String] = {

    if (itemList.length <= ROWS_ON_PAGE || paginationIndex <= 0) { None }
    else {
      val paged = itemList.grouped(ROWS_ON_PAGE).toSeq

      paged.lift(paginationIndex - 1).map { detailsChunk =>
        val total = itemList.length
        val start = (paginationIndex - 1) * ROWS_ON_PAGE + 1
        val end = math.min(paginationIndex * ROWS_ON_PAGE, total)
        messages("purchaser.purchaserOverview.summaryInfo.text", start, end, total)
      }
    }
  }

  def getNumberOfPages[A](itemList: Seq[A]): Int =
    itemList
      .grouped(ROWS_ON_PAGE)
      .size


  def generatePurchaserSummary(paginationIndex: Int, purchasers: Seq[Purchaser], userAnswers: UserAnswers)
                              (implicit messages: Messages): Option[SummaryList] = {
    
    val paged: Seq[Seq[Purchaser]] = purchasers.grouped(ROWS_ON_PAGE).toSeq
    val currentPage: Option[Seq[Purchaser]] = paged.lift(paginationIndex - 1)

    currentPage.flatMap { pagePurchasers =>
      if (pagePurchasers.forall(_.purchaserID.isDefined)) {
        Some(SummaryList(
          rows = pagePurchasers.flatMap { purchaserDetails =>
            val mainPurchaserIDCheck = populatePurchaserService.isMainPurchaser(purchaserDetails.purchaserID.get, userAnswers)

            val maybeName: Option[String] = (purchaserDetails.surname, purchaserDetails.companyName, mainPurchaserIDCheck) match {
              case (Some(name), _, true) =>  Some(FullName.fullName(purchaserDetails.forename1, purchaserDetails.forename2, name) + " (lead purchaser)")
              case (Some(name), _, false) =>  Some(FullName.fullName(purchaserDetails.forename1, purchaserDetails.forename2, name))
              case (_, Some(name), true) => Some(name + " (lead purchaser)")
              case (_, Some(name), false) => Some(name)
              case _ => None
            }

            for {
              fullName <- maybeName
              purchaserId <- purchaserDetails.purchaserID
            } yield {
              SummaryListRow(
                key = Key(
                  content = Text(fullName),
                  classes = "govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"
                ),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.purchaser.routes.PurchaserOverviewController.changePurchaser(purchaserId).url,
                      content = Text(messages("site.change")),
                      visuallyHiddenText = Some(fullName)
                    ),
                    ActionItem(
                      href = controllers.purchaser.routes.PurchaserOverviewController.removePurchaser(purchaserId).url,
                      content = Text(messages("site.remove")),
                      visuallyHiddenText = Some(fullName)
                    )
                  ),
                  classes = "govuk-!-width-one-third"
                ))
              )
            }
          }
        ))
      } else {
        None
      }
    }
  }
  

  def generatePagination(paginationIndex: Int, numberOfPages: Int)
                        (implicit messages: Messages): Option[Pagination] =
    if (numberOfPages < 2) None
    else
      Some(
        Pagination(
          items = Some(generatePaginationItems(paginationIndex, numberOfPages)),
          previous = generatePreviousLink(paginationIndex, numberOfPages),
          next = generateNextLink(paginationIndex, numberOfPages),
          landmarkLabel = None,
          classes = "",
          attributes = Map.empty
        )
      )

  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = controllers.purchaser.routes.PurchaserOverviewController.onPageLoad(pageIndex).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  def generatePreviousLink(paginationIndex: Int, numberOfPages: Int)
                          (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = controllers.purchaser.routes.PurchaserOverviewController.onPageLoad(paginationIndex - 1).url,
          text = Some(messages("site.previous")),
          attributes = Map.empty
        )
      )
    }

  def generateNextLink(paginationIndex: Int, numberOfPages: Int)
                      (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) None
    else {
      Some(
        PaginationLink(
          href = controllers.purchaser.routes.PurchaserOverviewController.onPageLoad(paginationIndex + 1).url,
          text = Some(messages("site.next")),
          attributes = Map.empty
        )
      )
    }
}
