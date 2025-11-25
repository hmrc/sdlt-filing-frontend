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
import models.Vendor
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class VendorPaginationHelperSpec extends SpecBase with VendorPaginationHelper {

  private implicit val messages: Messages = stubMessages()

  private val ROWS_ON_PAGE = 15

  def createVendor(id: String, forename1: Option[String] = None, forename2: Option[String] = None, name: Option[String] = None, vendorResourceRef: Option[String] = None): Vendor = {
    Vendor(
      vendorID = Some(id),
      forename1 = forename1,
      forename2 = forename2,
      name = name,
      address1 = Some("123 Street"),
      vendorResourceRef = vendorResourceRef
    )
  }

  "VendorPaginationHelper" - {

    "getPaginationInfoText" - {

      "must return None when item list is less than or equal to ROWS_ON_PAGE" in {
        val vendors = (1 to 10).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(1, vendors)
        result mustBe None
      }

      "must return None when item list equals ROWS_ON_PAGE" in {
        val vendors = (1 to 15).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(1, vendors)
        result mustBe None
      }

      "must return None when paginationIndex is 0 or less" in {
        val vendors = (1 to 20).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(0, vendors)
        result mustBe None
      }

      "must return Some text for first page" in {
        implicit val messages: Messages = stubMessages()
        val vendors = (1 to 30).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(1, vendors)
        result mustBe defined
      }

      "must return Some text for second page" in {
        implicit val messages: Messages = stubMessages()
        val vendors = (1 to 30).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(2, vendors)
        result mustBe defined
      }

      "must return Some text for middle page" in {
        implicit val messages: Messages = stubMessages()
        val vendors = (1 to 45).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(2, vendors)
        result mustBe defined
      }

      "must return Some text for last page with partial results" in {
        implicit val messages: Messages = stubMessages()
        val vendors = (1 to 37).map(i => createVendor(s"VEN$i"))
        val result = getPaginationInfoText(3, vendors)
        result mustBe defined
      }
    }

    "getNumberOfPages" - {

      "must return 1 for empty list" in {
        val result = getNumberOfPages(Seq.empty[Vendor])
        result mustBe 0
      }

      "must return 1 for items less than ROWS_ON_PAGE" in {
        val vendors = (1 to 10).map(i => createVendor(s"VEN$i"))
        val result = getNumberOfPages(vendors)
        result mustBe 1
      }

      "must return 1 for exactly ROWS_ON_PAGE items" in {
        val vendors = (1 to 15).map(i => createVendor(s"VEN$i"))
        val result = getNumberOfPages(vendors)
        result mustBe 1
      }

      "must return 2 for ROWS_ON_PAGE + 1 items" in {
        val vendors = (1 to 16).map(i => createVendor(s"VEN$i"))
        val result = getNumberOfPages(vendors)
        result mustBe 2
      }

      "must return correct number for multiple pages" in {
        val vendors = (1 to 45).map(i => createVendor(s"VEN$i"))
        val result = getNumberOfPages(vendors)
        result mustBe 3
      }

      "must return 7 for 99 vendors" in {
        val vendors = (1 to 99).map(i => createVendor(s"VEN$i"))
        val result = getNumberOfPages(vendors)
        result mustBe 7
      }
    }

    "generateVendorSummary" - {

      "must return None when pagination index is out of range" in {
        val vendors = (1 to 10).map(i => createVendor(s"VEN$i", name = Some(s"Vendor$i"), vendorResourceRef = Some(s"REF$i")))
        val result = generateVendorSummary(5, vendors)
        result mustBe None
      }

      "must return None when any vendor is missing vendorResourceRef" in {
        val vendors = Seq(
          createVendor("VEN1", name = Some("Smith"), vendorResourceRef = Some("REF1")),
          createVendor("VEN2", name = Some("Jones"), vendorResourceRef = None),
          createVendor("VEN3", name = Some("Williams"), vendorResourceRef = Some("REF3"))
        )
        val result = generateVendorSummary(1, vendors)
        result mustBe None
      }

      "must return SummaryList with full names when all names are present" in {
        val vendors = Seq(
          createVendor("VEN1", forename1 = Some("John"), forename2 = Some("Michael"), name = Some("Smith"), vendorResourceRef = Some("REF1")),
          createVendor("VEN2", forename1 = Some("Jane"), forename2 = Some("Mary"), name = Some("Doe"), vendorResourceRef = Some("REF2"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        result.get.rows.length mustBe 2
        result.get.rows.head.key.content.asHtml.body must include("John Michael Smith")
        result.get.rows(1).key.content.asHtml.body must include("Jane Mary Doe")
      }

      "must return SummaryList with first name and surname when no middle name" in {
        val vendors = Seq(
          createVendor("VEN1", forename1 = Some("John"), forename2 = None, name = Some("Smith"), vendorResourceRef = Some("REF1"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        result.get.rows.length mustBe 1
        result.get.rows.head.key.content.asHtml.body must include("John Smith")
      }

      "must return SummaryList with surname only when no forenames" in {
        val vendors = Seq(
          createVendor("VEN1", forename1 = None, forename2 = None, name = Some("Smith"), vendorResourceRef = Some("REF1"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        result.get.rows.length mustBe 1
        result.get.rows.head.key.content.asHtml.body must include("Smith")
      }

      "must filter out vendors with no name" in {
        val vendors = Seq(
          createVendor("VEN1", name = Some("Smith"), vendorResourceRef = Some("REF1")),
          createVendor("VEN2", name = None, vendorResourceRef = Some("REF2")),
          createVendor("VEN3", name = Some("Jones"), vendorResourceRef = Some("REF3"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        result.get.rows.length mustBe 2
      }

      "must generate correct change and remove links" in {
        val vendors = Seq(
          createVendor("VEN1", name = Some("Smith"), vendorResourceRef = Some("REF1"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.length mustBe 2
        actions.head.href must include("change-vendor/REF1")
        actions(1).href must include("remove-vendor/REF1")
      }

      "must paginate correctly for multiple pages" in {
        val vendors = (1 to 30).map(i =>
          createVendor(s"VEN$i", name = Some(s"Vendor$i"), vendorResourceRef = Some(s"REF$i"))
        )

        val page1 = generateVendorSummary(1, vendors)
        val page2 = generateVendorSummary(2, vendors)

        page1 mustBe defined
        page2 mustBe defined
        page1.get.rows.length mustBe 15
        page2.get.rows.length mustBe 15
      }

      "must handle last page with partial results" in {
        val vendors = (1 to 20).map(i =>
          createVendor(s"VEN$i", name = Some(s"Vendor$i"), vendorResourceRef = Some(s"REF$i"))
        )

        val page2 = generateVendorSummary(2, vendors)

        page2 mustBe defined
        page2.get.rows.length mustBe 5
      }

      "must include visuallyHiddenText in actions" in {
        val vendors = Seq(
          createVendor("VEN1", name = Some("John Smith"), vendorResourceRef = Some("REF1"))
        )
        val result = generateVendorSummary(1, vendors)

        result mustBe defined
        val actions = result.get.rows.head.actions.get.items
        actions.head.visuallyHiddenText mustBe Some("John Smith")
        actions(1).visuallyHiddenText mustBe Some("John Smith")
      }
    }

    "generatePagination" - {

      "must return None when numberOfPages is less than 2" in {
        val result = generatePagination(1, 1)
        result mustBe None
      }

      "must return None when numberOfPages is 0" in {
        val result = generatePagination(1, 0)
        result mustBe None
      }

      "must return Pagination with correct items for 2 pages" in {
        val result = generatePagination(1, 2)

        result mustBe defined
        result.get.items.get.length mustBe 2
        result.get.items.get.head.current mustBe Some(true)
        result.get.items.get(1).current mustBe Some(false)
      }

      "must return Pagination with correct items for multiple pages" in {
        val result = generatePagination(2, 5)

        result mustBe defined
        result.get.items.get.length mustBe 5
        result.get.items.get(1).current mustBe Some(true)
      }

      "must include previous link when not on first page" in {
        val result = generatePagination(2, 3)

        result mustBe defined
        result.get.previous mustBe defined
        result.get.previous.get.href must include("/about-the-vendor/vendor-overview")
      }

      "must not include previous link when on first page" in {
        val result = generatePagination(1, 3)

        result mustBe defined
        result.get.previous mustBe None
      }

      "must include next link when not on last page" in {
        val result = generatePagination(1, 3)

        result mustBe defined
        result.get.next mustBe defined
        result.get.next.get.href must include("/about-the-vendor/vendor-overview?paginationIndex=2")
      }

      "must not include next link when on last page" in {
        val result = generatePagination(3, 3)

        result mustBe defined
        result.get.next mustBe None
      }
    }

    "generatePaginationItems" - {

      "must generate correct pagination items" in {
        val result = generatePaginationItems(2, 5)

        result.length mustBe 5
        result.head.number mustBe Some("1")
        result.head.current mustBe Some(false)
        result(1).number mustBe Some("2")
        result(1).current mustBe Some(true)
        result.last.number mustBe Some("5")
      }

      "must generate single item for single page" in {
        val result = generatePaginationItems(1, 1)

        result.length mustBe 1
        result.head.number mustBe Some("1")
        result.head.current mustBe Some(true)
      }
    }

    "generatePreviousLink" - {

      "must return None when on first page" in {
        val result = generatePreviousLink(1, 5)
        result mustBe None
      }

      "must return link to previous page when not on first page" in {
        val result = generatePreviousLink(3, 5)

        result mustBe defined
        result.get.href must include("vendor-overview")
        result.get.href must include("2")
        result.get.text mustBe Some("vendor.vendor-overview.pagination.previous")
      }

      "must return link to page 1 when on page 2" in {
        val result = generatePreviousLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-vendor/vendor-overview")
      }
    }

    "generateNextLink" - {

      "must return None when on last page" in {
        val result = generateNextLink(5, 5)
        result mustBe None
      }

      "must return link to next page when not on last page" in {
        val result = generateNextLink(2, 5)

        result mustBe defined
        result.get.href must include("/about-the-vendor/vendor-overview?paginationIndex=3")
        result.get.text mustBe Some("vendor.vendor-overview.pagination.next")
      }

      "must return link to page 2 when on page 1" in {
        val result = generateNextLink(1, 5)

        result mustBe defined
        result.get.href must include("/about-the-vendor/vendor-overview?paginationIndex=2")
      }
    }
  }
}