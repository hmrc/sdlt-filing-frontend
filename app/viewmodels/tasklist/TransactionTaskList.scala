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
import models.{CheckMode, FullReturn}
import play.api.i18n.Messages
import services.crossflow.{PageId, Pages, ReturnSection, SectionStatus}

import javax.inject.Singleton

@Singleton
object TransactionTaskList {
  
  private val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Transaction, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  def build(fullReturn: FullReturn, status: SectionStatus = noFailures)
           (implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.transactionQuestion.heading"),
      rows    = Seq(buildTransactionRow(fullReturn, status))
    )

  def buildTransactionRow(fullReturn: FullReturn, status: SectionStatus)
                         (implicit appConfig: FrontendAppConfig): TaskListSectionRow = {

    val transactionComplete = fullReturn.transaction.exists(_.effectiveDate.isDefined)

    val cyaUrl   = controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
    val startUrl = controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
    
    def urlFor(page: PageId): String = page match {
      case Pages.ReliefReason  => controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
      case Pages.EffectiveDate => controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
      case _                   => cyaUrl
    }
    
    val url =
      if (status.hasFailures && status.targets.size == 1) urlFor(status.targets.head.page)
      else if (status.hasFailures)                        cyaUrl
      else if (transactionComplete)                       cyaUrl
      else                                                startUrl

    TaskListRowBuilder(
      canEdit       = _ => true,
      messageKey    = _ => "tasklist.transactionQuestion.details",
      url           = _ => _ => url,
      tagId         = "transactionQuestionDetailRow",
      checks        = _ => Seq(transactionComplete),
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    ).build(fullReturn)
  }
}