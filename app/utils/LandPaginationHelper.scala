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

import models.{Land, UserAnswers}
import play.api.i18n.Messages
import services.land.LandService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

import javax.inject.Inject

class LandPaginationHelper  @Inject(landService: LandService){
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
        messages("land.landOverview.summaryInfo.text", start, end, total)
      }
    }
  }

  def getNumberOfPages[A](itemList: Seq[A]): Int =
    itemList
      .grouped(ROWS_ON_PAGE)
      .size


  def generateLandSummary(paginationIndex: Int, lands: Seq[Land], userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[SummaryList] = {
    
    val sortedLands: Seq[Land] = lands.filter(_.landID.isDefined).sortBy(l => !landService.isMainLand(userAnswers, l.landID.get))
    val paged: Seq[Seq[Land]] = sortedLands.grouped(ROWS_ON_PAGE).toSeq
    val currentPage: Option[Seq[Land]] = paged.lift(paginationIndex - 1)

    currentPage.flatMap { pageLands =>
      if (pageLands.forall(_.landResourceRef.isDefined)) {
        Some(SummaryList(
          rows = pageLands.flatMap { landDetails =>
            val landAddress: Option[String] = landDetails.address1
            
            for {
              address <- landAddress
              landId <- landDetails.landID
            } yield {
              SummaryListRow(
                key = Key(
                  content = Text(address),
                  classes = "govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"
                ),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.land.routes.LandOverviewController.changeLand(landId).url,
                      content = Text(messages("site.change")),
                      visuallyHiddenText = Some(address)
                    ),
                    ActionItem(
                      href = controllers.land.routes.LandOverviewController.removeLand(landId).url,
                      content = Text(messages("site.remove")),
                      visuallyHiddenText = Some(address)
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
    if (numberOfPages < 2) {
      None
    } else {
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
    }

  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = controllers.land.routes.LandOverviewController.onPageLoad(pageIndex).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  def generatePreviousLink(paginationIndex: Int, numberOfPages: Int)
                          (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == 1) {
      None
    } else {
      Some(
        PaginationLink(
          href = controllers.land.routes.LandOverviewController.onPageLoad(paginationIndex - 1).url,
          text = Some(messages("site.previous")),
          attributes = Map.empty
        )
      )
    }

  def generateNextLink(paginationIndex: Int, numberOfPages: Int)
                      (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) {
      None
    } else {
      Some(
        PaginationLink(
          href = controllers.land.routes.LandOverviewController.onPageLoad(paginationIndex + 1).url,
          text = Some(messages("site.next")),
          attributes = Map.empty
        )
      )
    }
}
