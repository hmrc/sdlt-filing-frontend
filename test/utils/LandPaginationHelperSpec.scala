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

import base.SpecBase
import constants.FullReturnConstants
import models.{FullReturn, Land, ReturnInfo}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class LandPaginationHelperSpec extends SpecBase {
  private implicit val messages: Messages = stubMessages()

  val sortService = new SortService()
  val helper = new LandPaginationHelper(sortService)

  private def createLand(landId: String, address: Option[String] = None, landResourceRef: Option[String] = None): Land = {
    Land(
      landID = Some(landId),
      returnID = Some("RET123456789"),
      propertyType = Some("01"),
      interestCreatedTransferred = Some("FG"),
      houseNumber = Some("123"),
      address1 = address,
      address2 = Some("Marylebone"),
      address3 = Some("London"),
      address4 = None,
      postcode = Some("NW1 6XE"),
      landArea = Some("250.5"),
      areaUnit = Some("SQMETRE"),
      localAuthorityNumber = Some("5900"),
      mineralRights = Some("NO"),
      NLPGUPRN = Some("10012345678"),
      willSendPlanByPost = Some("NO"),
      titleNumber = Some("TGL12456"),
      landResourceRef = landResourceRef,
      nextLandID = None,
      DARPostcode = Some("NW1 6XE")
    )
  }

  private val mainLand = createLand("LDN001", Some("Address 1"), Some("LDN-REF-01"))

  private val fullReturnWithMainLand = FullReturnConstants.emptyFullReturn
      .copy(land = Some(Seq(mainLand)), returnInfo = Some(ReturnInfo(mainLandID = Some("LDN001"))))

  private val UserAnswersWithLand = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithMainLand))

  "LandPaginationHelper" - {

    ".getPaginationInfoText" - {

      "must return None when item list is less than or equal to ROWS_ON_PAGE" in {
        val lands = (1 to 10).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(1, lands)
        result mustBe None
      }

      "must return None when item list equals ROWS_ON_PAGE" in {
        val lands = (1 to 15).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(1, lands)
        result mustBe None
      }

      "must return None when paginationIndex is 0 or less" in {
        val lands = (1 to 20).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(0, lands)
        result mustBe None
      }

      "must return Some text for first page" in {
        implicit val messages: Messages = stubMessages()
        val lands = (1 to 30).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(1, lands)
        result mustBe defined
      }

      "must return Some text for second page" in {
        implicit val messages: Messages = stubMessages()
        val lands = (1 to 30).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(2, lands)
        result mustBe defined
      }

      "must return Some text for middle page" in {
        implicit val messages: Messages = stubMessages()
        val lands = (1 to 45).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(2, lands)
        result mustBe defined
      }

      "must return Some text for last page with partial results" in {
        implicit val messages: Messages = stubMessages()
        val lands = (1 to 37).map(i => createLand(s"LND$i"))
        val result = helper.getPaginationInfoText(3, lands)
        result mustBe defined
      }
    }

    ".getNumberOfPages" - {

      "must return 1 for empty list" in {
        val result = helper.getNumberOfPages(Seq.empty[Land])
        result mustBe 0
      }

      "must return 1 for items less than ROWS_ON_PAGE" in {
        val lands = (1 to 10).map(i => createLand(s"LND$i"))
        val result = helper.getNumberOfPages(lands)
        result mustBe 1
      }

      "must return 1 for exactly ROWS_ON_PAGE items" in {
        val lands = (1 to 15).map(i => createLand(s"LND$i"))
        val result = helper.getNumberOfPages(lands)
        result mustBe 1
      }

      "must return 2 for ROWS_ON_PAGE + 1 items" in {
        val lands = (1 to 16).map(i => createLand(s"LND$i"))
        val result = helper.getNumberOfPages(lands)
        result mustBe 2
      }

      "must return correct number for multiple pages" in {
        val lands = (1 to 45).map(i => createLand(s"LND$i"))
        val result = helper.getNumberOfPages(lands)
        result mustBe 3
      }

      "must return 7 for 99 lands" in {
        val lands = (1 to 99).map(i => createLand(s"LND$i"))
        val result = helper.getNumberOfPages(lands)
        result mustBe 7
      }
    }

    ".generateLandSummary" - {

      "must return None when pagination index is out of range" in {
        val lands = (1 to 10).map(i => createLand(s"LND$i", address = Some(s"Address $i"), landResourceRef = Some(s"LND-REF-$i")))
        val result = helper.generateLandSummary(5, lands, UserAnswersWithLand)
        result mustBe None
      }

      "must return None when any land is missing landResourceRef" in {
        val lands = Seq(
          createLand("LND001", address = Some("Address 1"), landResourceRef = Some("LND-REF-001")),
          createLand("LND002", address = Some("Address 2"), landResourceRef = None),
          createLand("LND003", address = Some("Address 3"), landResourceRef = Some("LND-REF-003"))
        )
        val result = helper.generateLandSummary(1, lands, UserAnswersWithLand)
        result mustBe None
      }

      "must filter out lands with no address" in {
        val lands = Seq(
          createLand("LND001", address = Some("Address 1"), landResourceRef = Some("LND-REF-001")),
          createLand("LND002", address = Some("Address 2"), landResourceRef = Some("LND-REF-002")),
          createLand("LND003", address = None, landResourceRef = Some("LND-REF-0013"))
        )
        val result = helper.generateLandSummary(1, lands, UserAnswersWithLand)

        result mustBe defined
        result.get.rows.length mustBe 2
      }

      "must generate correct change and remove links" in {
        val lands = Seq(
          createLand("LND001", address = Some("Address 1"), landResourceRef = Some("LND-REF-001")),
        )
        val result = helper.generateLandSummary(1, lands, UserAnswersWithLand)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.length mustBe 2
        actions.head.href must include("change-land/LND001")
        actions(1).href must include("remove-land/LND001")
      }

      "must paginate correctly for multiple pages" in {
        val lands = (1 to 30)
          .map(i => createLand(s"LND$i", address = Some(s"Address $i"), landResourceRef = Some(s"LND-REF-$i"))
        )

        val page1 = helper.generateLandSummary(1, lands, UserAnswersWithLand)
        val page2 = helper.generateLandSummary(2, lands, UserAnswersWithLand)

        page1 mustBe defined
        page2 mustBe defined
        page1.get.rows.length mustBe 15
        page2.get.rows.length mustBe 15
      }

      "must handle last page with partial results" in {
        val lands = (1 to 20)
          .map(i => createLand(s"LND$i", address = Some(s"Address $i"), landResourceRef = Some(s"LND-REF-$i"))
        )

        val page2 = helper.generateLandSummary(2, lands, UserAnswersWithLand)

        page2 mustBe defined
        page2.get.rows.length mustBe 5
      }

      "must include visuallyHiddenText in actions" in {
        val lands = Seq(
          createLand("LND001", address = Some("Address 1"), landResourceRef = Some("LND-REF-001")),
        )
        val result = helper.generateLandSummary(1, lands, UserAnswersWithLand)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.head.visuallyHiddenText mustBe Some("Address 1")
        actions(1).visuallyHiddenText mustBe Some("Address 1")
      }
    }

    ".generatePagination" - {

      "must return None when numberOfPages is less than 2" in {
        val result = helper.generatePagination(1, 1)
        result mustBe None
      }

      "must return None when numberOfPages is 0" in {
        val result = helper.generatePagination(1, 0)
        result mustBe None
      }

      "must return Pagination with correct items for 2 pages" in {
        val result = helper.generatePagination(1, 2)

        result mustBe defined
        result.get.items.get.length mustBe 2
        result.get.items.get.head.current mustBe Some(true)
        result.get.items.get(1).current mustBe Some(false)
      }

      "must return Pagination with correct items for multiple pages" in {
        val result = helper.generatePagination(2, 5)

        result mustBe defined
        result.get.items.get.length mustBe 5
        result.get.items.get(1).current mustBe Some(true)
      }

      "must include previous link when not on first page" in {
        val result = helper.generatePagination(2, 3)

        result mustBe defined
        result.get.previous mustBe defined
        result.get.previous.get.href must include("/about-the-land/land-or-property-overview")
      }

      "must not include previous link when on first page" in {
        val result = helper.generatePagination(1, 3)

        result mustBe defined
        result.get.previous mustBe None
      }

      "must include next link when not on last page" in {
        val result = helper.generatePagination(1, 3)

        result mustBe defined
        result.get.next mustBe defined
        result.get.next.get.href must include("/about-the-land/land-or-property-overview?paginationIndex=2")
      }

      "must not include next link when on last page" in {
        val result = helper.generatePagination(3, 3)

        result mustBe defined
        result.get.next mustBe None
      }
    }

    ".generatePaginationItems" - {

      "must generate correct pagination items" in {
        val result = helper.generatePaginationItems(2, 5)

        result.length mustBe 5
        result.head.number mustBe Some("1")
        result.head.current mustBe Some(false)
        result(1).number mustBe Some("2")
        result(1).current mustBe Some(true)
        result.last.number mustBe Some("5")
      }

      "must generate single item for single page" in {
        val result = helper.generatePaginationItems(1, 1)

        result.length mustBe 1
        result.head.number mustBe Some("1")
        result.head.current mustBe Some(true)
      }
    }

    ".generatePreviousLink" - {

      "must return None when on first page" in {
        val result = helper.generatePreviousLink(1, 5)
        result mustBe None
      }

      "must return link to previous page when not on first page" in {
        val result = helper.generatePreviousLink(3, 5)

        result mustBe defined
        result.get.href must include("/land-or-property-overview")
        result.get.href must include("2")
        result.get.text mustBe Some("site.previous")
      }

      "must return link to page 1 when on page 2" in {
        val result = helper.generatePreviousLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-land/land-or-property-overview")
      }
    }

    ".generateNextLink" - {

      "must return None when on last page" in {
        val result = helper.generateNextLink(5, 5)
        result mustBe None
      }

      "must return link to next page when not on last page" in {
        val result = helper.generateNextLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-land/land-or-property-overview?paginationIndex=3")
        result.get.text mustBe Some("site.next")
      }

      "must return link to page 2 when on page 1" in {
        val result = helper.generateNextLink(1, 5)

        result mustBe defined
        result.get.href must include("/about-the-land/land-or-property-overview?paginationIndex=2")
      }
    }
  }
}
