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

package viewmodels.submission

import base.SpecBase
import constants.FullReturnConstants.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class SubmissionReceiptViewModelSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  ".apply" - {

    "return None when the full return has no submission" in {
      SubmissionReceiptViewModel(completeFullReturn.copy(submission = None)) mustBe None
    }

    "return None when the submission has no UTRN" in {
      val fullReturn = completeFullReturn.copy(submission = Some(completeSubmission.copy(UTRN = None)))
      SubmissionReceiptViewModel(fullReturn) mustBe None
    }

    "return None when the submission has no submission receipt" in {
      val fullReturn = completeFullReturn.copy(submission = Some(completeSubmission.copy(submissionReceipt = None)))
      SubmissionReceiptViewModel(fullReturn) mustBe None
    }

    "return None when the submission has no submission request date" in {
      val fullReturn = completeFullReturn.copy(submission = Some(completeSubmission.copy(submissionRequestDate = None)))
      SubmissionReceiptViewModel(fullReturn) mustBe None
    }

    "return the purchaser's full name for an individual purchaser" in {
      val viewModel = SubmissionReceiptViewModel(completeFullReturn).value
      viewModel.purchaserName mustEqual "John David Smith"
    }

    "return the company name for a company purchaser" in {
      val companyPurchaser = completePurchaser1.copy(isCompany = Some("YES"), companyName = Some("Acme Ltd"))
      val fullReturn = completeFullReturn.copy(purchaser = Some(Seq(companyPurchaser)))

      val viewModel = SubmissionReceiptViewModel(fullReturn).value
      viewModel.purchaserName mustEqual "Acme Ltd"
    }

    "format the submission time as a lowercase 12-hour clock time" in {
      val viewModel = SubmissionReceiptViewModel(completeFullReturn).value
      viewModel.submissionTime mustEqual "10:15am"
    }

    "format the submission date in the site-standard long date format" in {
      val viewModel = SubmissionReceiptViewModel(completeFullReturn).value
      viewModel.submissionDate mustEqual "15 October 2024"
    }

    "use the submission receipt field, not the UTRN, as the submission receipt number" in {
      val viewModel = SubmissionReceiptViewModel(completeFullReturn).value
      viewModel.submissionReceiptNumber mustEqual "RECEIPT-001"
    }

    "build a table with the UTRN, address, purchaser, vendor, transaction type, effective date, agent reference, title number and NLPG UPRN rows" in {
      val viewModel = SubmissionReceiptViewModel(completeFullReturn).value

      viewModel.table.rows.map(_.head.content) mustEqual Seq(
        Text("submission.submissionReceipt.table.utrn"),
        Text("submission.submissionReceipt.table.address"),
        Text("submission.submissionReceipt.table.purchaser"),
        Text("submission.submissionReceipt.table.vendor"),
        Text("submission.submissionReceipt.table.transactionType"),
        Text("submission.submissionReceipt.table.effectiveDate"),
        Text("submission.submissionReceipt.table.agentRef"),
        Text("submission.submissionReceipt.table.titleNumber"),
        Text("submission.submissionReceipt.table.uprn")
      )

      viewModel.table.rows.map(_.last.content) mustEqual Seq(
        Text("UTRN123456789012"),
        Text("123, Baker Street, Marylebone, London, NW1 6XE"),
        Text("John David Smith"),
        Text("Johnson"),
        Text("prelim.transactionType.conveyanceTransfer"),
        Text("1 October 2024"),
        Text("SP/2024/001"),
        Text("TGL123456"),
        Text("10012345678")
      )
    }

    "omit the agent reference row when the purchaser has no agent" in {
      val fullReturn = completeFullReturn.copy(returnAgent = None)
      val viewModel = SubmissionReceiptViewModel(fullReturn).value

      viewModel.table.rows.map(_.head.content) must not contain Text("submission.submissionReceipt.table.agentRef")
    }

    "omit the title number row when the land has no title number" in {
      val fullReturn = completeFullReturn.copy(land = Some(Seq(completeLand.copy(titleNumber = None))))
      val viewModel = SubmissionReceiptViewModel(fullReturn).value

      viewModel.table.rows.map(_.head.content) must not contain Text("submission.submissionReceipt.table.titleNumber")
    }

    "omit the NLPG UPRN row when the land has no NLPG UPRN" in {
      val fullReturn = completeFullReturn.copy(land = Some(Seq(completeLand.copy(NLPGUPRN = None))))
      val viewModel = SubmissionReceiptViewModel(fullReturn).value

      viewModel.table.rows.map(_.head.content) must not contain Text("submission.submissionReceipt.table.uprn")
    }

    "only build the six required rows when no optional data is present" in {
      val fullReturn = completeFullReturn.copy(
        returnAgent = None,
        land = Some(Seq(completeLand.copy(titleNumber = None, NLPGUPRN = None)))
      )
      val viewModel = SubmissionReceiptViewModel(fullReturn).value

      viewModel.table.rows.size mustEqual 6
    }
  }
}
