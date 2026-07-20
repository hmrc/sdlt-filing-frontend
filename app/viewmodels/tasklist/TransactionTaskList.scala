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

import javax.inject.Singleton

@Singleton
object TransactionTaskList {
  
  val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Transaction, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  def build(fullReturn: FullReturn, status: SectionStatus = noFailures)
           (implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.transactionQuestion.heading"),
      rows    = Seq(buildTransactionRow(fullReturn, status))
    )

  def isTransactionComplete(fullReturn: FullReturn): Boolean = {
    fullReturn.transaction.exists(_.effectiveDate.isDefined)
    //TODO ADD ALL REQUIRED FIELDS FOR TRANSACTION
  }

  def transactionRowBuilder(fullReturn: FullReturn, status: SectionStatus)
                         (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val cyaUrl   = controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
    val startUrl = controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
    val errorUrl = controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url
    
    val url =
      if isTransactionComplete(fullReturn) && status.hasFailures then errorUrl
      else if isTransactionComplete(fullReturn) then cyaUrl
      else startUrl

    TaskListRowBuilder(
      canEdit       = _ => true,
      messageKey    = _ => "tasklist.transactionQuestion.details",
      url           = _ => _ => url,
      tagId         = "transactionQuestionDetailRow",
      checks        = _ => Seq(isTransactionComplete(fullReturn)),
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    )
  }

  def buildTransactionRow(fullReturn: FullReturn, status: SectionStatus)
                         (implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    transactionRowBuilder(fullReturn, status).build(fullReturn)
}