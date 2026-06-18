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

package viewmodels.tasklist

import config.FrontendAppConfig
import models.FullReturn
import play.api.i18n.Messages
import utils.{LeaseHelper, PropertyTypeHelper}
import viewmodels.tasklist.LandTaskList.isLandComplete
import viewmodels.tasklist.LeaseTaskList.isLeaseComplete
import viewmodels.tasklist.PurchaserTaskList.isPurchaserComplete
import viewmodels.tasklist.TaxCalculationTaskList.isTaxCalculationComplete
import viewmodels.tasklist.TransactionTaskList.isTransactionComplete
import viewmodels.tasklist.VendorAgentTaskList.*
import viewmodels.tasklist.PurchaserAgentTaskList.*
import viewmodels.tasklist.UkResidencyTaskList.isResidencyComplete
import viewmodels.tasklist.VendorTaskList.isVendorComplete

import javax.inject.Singleton

@Singleton
object SubmissionTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.submissionQuestion.heading"),
      rows = Seq(
        buildSubmissionRow(fullReturn)
      )
    )

  def buildSubmissionRow(fullReturn: FullReturn)(implicit messages:Messages, appConfig: FrontendAppConfig): TaskListSectionRow = {
    val url = fullReturn.submission match {
      case Some(submission) if submission.submissionID.isDefined =>
        //TODO UPDATE to DTR-5731 Success page
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
      case _ =>
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.submissionQuestion.details",
      hint = fullReturn => {
        if (!canStartSubmission(fullReturn))
          Some("tasklist.submissionQuestion.hint")
        else
          None
      },
      url = _ => _ => {
        url
      },
      tagId = "submissionQuestionDetailRow",
      checks = scheme => Seq(fullReturn.submission.exists(_.submissionID.isDefined)),
      prerequisites = _ => {
        val mandatory = Seq(
          VendorTaskList.vendorRowBuilder(fullReturn),
          PurchaserTaskList.purchaserRowBuilder(fullReturn),
          LandTaskList.landRowBuilder(fullReturn, viewmodels.tasklist.LandTaskList.noFailures),
          TransactionTaskList.transactionRowBuilder(fullReturn, viewmodels.tasklist.TransactionTaskList.noFailures),
          TaxCalculationTaskList.taxCalculationRowBuilder(fullReturn)
        )

        val conditional = Seq(
          Option.when(isResidencyRequired(fullReturn))(UkResidencyTaskList.ukResidencyRowBuilder(fullReturn)),
          Option.when(isLeaseRequired(fullReturn))(LeaseTaskList.leaseRowBuilder(fullReturn)),
          Option.when(isPurchaserAgentStarted(fullReturn))(
            PurchaserAgentTaskList.purchaserAgentRowBuilder(fullReturn)
          ),
          Option.when(isVendorAgentStarted(fullReturn))(
            VendorAgentTaskList.vendorAgentRowBuilder(fullReturn)
          )
        ).flatten

        mandatory ++ conditional
      }
    ).build(fullReturn)
  }

  def canStartSubmission(fullReturn: FullReturn): Boolean = {
    isVendorComplete(fullReturn) &&
    isPurchaserComplete(fullReturn) &&
    isLandComplete(fullReturn) &&
    isTransactionComplete(fullReturn) &&
    isTaxCalculationComplete(fullReturn) &&
    (!isVendorAgentStarted(fullReturn) || isVendorAgentComplete(fullReturn)) &&
    (!isPurchaserAgentStarted(fullReturn) || isPurchaserAgentComplete(fullReturn)) &&
    (!isLeaseRequired(fullReturn) || isLeaseComplete(fullReturn)) &&
    (!isResidencyRequired(fullReturn) || isResidencyComplete(fullReturn))
  }

  private def isLeaseRequired(fullReturn: FullReturn): Boolean = {
    LeaseHelper.isLeaseDefined(fullReturn)
  }

  private def isResidencyRequired(fullReturn: FullReturn): Boolean = {
    PropertyTypeHelper.isResidentialProperty(fullReturn)
  }

}
