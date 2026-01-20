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

import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.{FullReturn, Purchaser, ReturnInfo, UserAnswers}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import services.purchaser.PopulatePurchaserService
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class PurchaserPaginationHelperSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()
  val service = new PopulatePurchaserService()
  val helper = new PurchaserPaginationHelper(service)

  private val individualPurchaser = Purchaser(
    purchaserID = Some("PUR001"),
    forename1 = Some("John"),
    forename2 = Some("Michael"),
    surname = Some("Smith"),
    address1 = Some("20 Test Road"),
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = Some("L1 1AA"),
    isCompany = Some("NO"),
    phone = Some("07123456789"),
    nino = Some("AB123456C"),
    dateOfBirth = Some("1985-03-15")
  )

  private val fullReturnWithIndividualPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaser)),
      returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR00"))))




  def createPurchaser(id: String,
                      forename1: Option[String] = None,
                      forename2: Option[String] = None,
                      name: Option[String] = None,
                      companyName: Option[String] = None): Purchaser = {
    Purchaser(
      purchaserID = Some(id),
      forename1 = forename1,
      forename2 = forename2,
      surname = name,
      address1 = Some("123 Street")
    )
  }

  "PurchaserPaginationHelper" - {

    "getPaginationInfoText" - {

      "must return None when item list is less than or equal to ROWS_ON_PAGE" in {
        val purchasers = (1 to 10).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(1, purchasers)
        result mustBe None
      }

      "must return None when item list equals ROWS_ON_PAGE" in {
        val purchasers = (1 to 15).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(1, purchasers)
        result mustBe None
      }

      "must return None when paginationIndex is 0 or less" in {
        val purchasers = (1 to 20).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(0, purchasers)
        result mustBe None
      }

      "must return Some text for first page" in {
        implicit val messages: Messages = stubMessages()
        val purchasers = (1 to 30).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(1, purchasers)
        result mustBe defined
      }

      "must return Some text for second page" in {
        implicit val messages: Messages = stubMessages()
        val purchasers = (1 to 30).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(2, purchasers)
        result mustBe defined
      }

      "must return Some text for middle page" in {
        implicit val messages: Messages = stubMessages()
        val purchasers = (1 to 45).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(2, purchasers)
        result mustBe defined
      }

      "must return Some text for last page with partial results" in {
        implicit val messages: Messages = stubMessages()
        val purchasers = (1 to 37).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getPaginationInfoText(3, purchasers)
        result mustBe defined
      }
    }

    "getNumberOfPages" - {

      "must return 1 for empty list" in {
        val result = helper.getNumberOfPages(Seq.empty[Purchaser])
        result mustBe 0
      }

      "must return 1 for items less than ROWS_ON_PAGE" in {
        val purchasers = (1 to 10).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getNumberOfPages(purchasers)
        result mustBe 1
      }

      "must return 1 for exactly ROWS_ON_PAGE items" in {
        val purchasers = (1 to 15).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getNumberOfPages(purchasers)
        result mustBe 1
      }

      "must return 2 for ROWS_ON_PAGE + 1 items" in {
        val purchasers = (1 to 16).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getNumberOfPages(purchasers)
        result mustBe 2
      }

      "must return correct number for multiple pages" in {
        val purchasers = (1 to 45).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getNumberOfPages(purchasers)
        result mustBe 3
      }

      "must return 7 for 99 purchasers" in {
        val purchasers = (1 to 99).map(i => createPurchaser(s"PUR$i"))
        val result = helper.getNumberOfPages(purchasers)
        result mustBe 7
      }
    }

    "generatePurchaserSummary" - {

      "must return None when pagination index is out of range" in {
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val purchasers = (1 to 10).map(i => createPurchaser(s"PUR$i", name = Some(s"Purchaser$i")))
        val result = helper.generatePurchaserSummary(5, purchasers, userAnswers)
        result mustBe None
      }

      "must return SummaryList with full names when all names are present" in {
        val purchasers = Seq(
          createPurchaser("PUR1", forename1 = Some("John"), forename2 = Some("Michael"), name = Some("Smith")),
          createPurchaser("PUR2", forename1 = Some("Jane"), forename2 = Some("Mary"), name = Some("Doe"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        result.get.rows.length mustBe 2
        result.get.rows.head.key.content.asHtml.body must include("John Michael Smith")
        result.get.rows(1).key.content.asHtml.body must include("Jane Mary Doe")
      }

      "must return SummaryList with first name and surname when no middle name" in {
        val purchasers = Seq(
          createPurchaser("PUR1", forename1 = Some("John"), forename2 = None, name = Some("Smith"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        result.get.rows.length mustBe 1
        result.get.rows.head.key.content.asHtml.body must include("John Smith")
      }

      "must return SummaryList with surname only when no forenames" in {
        val purchasers = Seq(
          createPurchaser("PUR1", forename1 = None, forename2 = None, name = Some("Smith"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        result.get.rows.length mustBe 1
        result.get.rows.head.key.content.asHtml.body must include("Smith")
      }

      "must filter out purchasers with no name" in {
        val purchasers = Seq(
          createPurchaser("PUR1", name = Some("Smith")),
          createPurchaser("PUR2", name = None),
          createPurchaser("PUR3", name = Some("Jones"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        result.get.rows.length mustBe 2
      }

      "must generate correct change and remove links" in {
        val purchasers = Seq(
          createPurchaser("REF1", name = Some("Smith"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.length mustBe 2
        actions.head.href must include("change-purchaser/REF1")
        actions(1).href must include("remove-purchaser/REF1")
      }

      "must paginate correctly for multiple pages" in {
        val purchasers = (1 to 30).map(i =>
          createPurchaser(s"PUR$i", name = Some(s"Purchaser$i"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val page1 = helper.generatePurchaserSummary(1, purchasers, userAnswers)
        val page2 = helper.generatePurchaserSummary(2, purchasers, userAnswers)

        page1 mustBe defined
        page2 mustBe defined
        page1.get.rows.length mustBe 15
        page2.get.rows.length mustBe 15
      }

      "must handle last page with partial results" in {
        val purchasers = (1 to 20).map(i =>
          createPurchaser(s"PUR$i", name = Some(s"Purchaser$i"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val page2 = helper.generatePurchaserSummary(2, purchasers, userAnswers)

        page2 mustBe defined
        page2.get.rows.length mustBe 5
      }

      "must include visuallyHiddenText in actions" in {
        val purchasers = Seq(
          createPurchaser("PUR1", name = Some("John Smith"))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
        val result = helper.generatePurchaserSummary(1, purchasers, userAnswers)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.head.visuallyHiddenText mustBe Some("John Smith")
        actions(1).visuallyHiddenText mustBe Some("John Smith")
      }
    }

    "generatePagination" - {

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
        result.get.previous.get.href must include("/about-the-purchaser/purchaser-overview")
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
        result.get.next.get.href must include("/about-the-purchaser/purchaser-overview?paginationIndex=2")
      }

      "must not include next link when on last page" in {
        val result = helper.generatePagination(3, 3)

        result mustBe defined
        result.get.next mustBe None
      }
    }

    "generatePaginationItems" - {

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

    "generatePreviousLink" - {

      "must return None when on first page" in {
        val result = helper.generatePreviousLink(1, 5)
        result mustBe None
      }

      "must return link to previous page when not on first page" in {
        val result = helper.generatePreviousLink(3, 5)

        result mustBe defined
        result.get.href must include("purchaser-overview")
        result.get.href must include("2")
        result.get.text mustBe Some("site.previous")
      }

      "must return link to page 1 when on page 2" in {
        val result = helper.generatePreviousLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-purchaser/purchaser-overview")
      }
    }

    "generateNextLink" - {

      "must return None when on last page" in {
        val result = helper.generateNextLink(5, 5)
        result mustBe None
      }

      "must return link to next page when not on last page" in {
        val result = helper.generateNextLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-purchaser/purchaser-overview?paginationIndex=3")
        result.get.text mustBe Some("site.next")
      }

      "must return link to page 2 when on page 1" in {
        val result = helper.generateNextLink(1, 5)

        result mustBe defined
        result.get.href must include("/about-the-purchaser/purchaser-overview?paginationIndex=2")
      }
    }
  }
}