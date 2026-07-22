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
import services.crossflow.{ReturnSection, SectionStatus}
import utils.LeaseHelper

import javax.inject.Singleton

@Singleton
object LeaseTaskList {

  val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Lease, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  def build(fullReturn: FullReturn, status: SectionStatus = noFailures)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.leaseQuestion.heading"),
      rows    = Seq(
        buildLeaseRow(fullReturn, status)
      )
    )

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    
    val generalLeaseFields = Seq(
      fullReturn.lease.exists(_.leaseType.isDefined),
      fullReturn.lease.exists(_.contractStartDate.isDefined),
      fullReturn.lease.exists(_.contractEndDate.isDefined),
      fullReturn.lease.exists(_.rentFreePeriod.isDefined),
      fullReturn.lease.exists(_.startingRent.isDefined),
      fullReturn.lease.exists(_.startingRentEndDate.isDefined),
      fullReturn.lease.exists(_.laterRentKnown.isDefined)
    )

    // if transaction type is Grand of Lease
    val isTransactionTypeGrandOfLease = fullReturn.transaction.exists(_.transactionDescription.contains("L"))
    
    val grantOfLeaseFields = Seq(
      fullReturn.lease.exists(_.totalPremiumPayable.isDefined),
      fullReturn.lease.exists(_.isAnnualRentOver1000.isDefined),
      fullReturn.lease.exists(_.netPresentValue.isDefined)
    )

    if (isTransactionTypeGrandOfLease) {
      generalLeaseFields ++ grantOfLeaseFields
    } else {
      generalLeaseFields
    }
  }

  def isLeaseComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  private def isLeaseRequired(fullReturn: FullReturn): Boolean = {
    LeaseHelper.isLeaseDefined(fullReturn)
  }

  private def isLeaseStarted(fullReturn: FullReturn): Boolean = {
    fullReturn.lease.nonEmpty
  }

  def leaseRowBuilder(fullReturn: FullReturn, status: SectionStatus)
                     (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val cyaUrl   = controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
    val startUrl = controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
    val errorUrl = controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url

    val url =
      if isLeaseComplete(fullReturn) &&status.hasFailures then errorUrl
      else if isLeaseComplete(fullReturn) then cyaUrl
      else startUrl

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _           => true
      },
      messageKey    = _ => "tasklist.leaseQuestion.details",
      url           = _ => _ => url,
      tagId         = "leaseQuestionDetailRow",
      checks        = _ => mandatoryFieldsDefined(fullReturn),
      started       = _ => if (isLeaseRequired(fullReturn)) isLeaseStarted(fullReturn) else false,
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq()
    )
  }

  def buildLeaseRow(fullReturn: FullReturn, status: SectionStatus = noFailures)
                   (implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    leaseRowBuilder(fullReturn, status).build(fullReturn)
}