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

import javax.inject.Singleton

@Singleton
object TransactionTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.transactionQuestion.heading"),
      rows = Seq(
        buildTransactionRow(fullReturn).build(fullReturn)
      )
    )

  def buildTransactionRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {
    
    val url = controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
    
    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.transactionQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "transactionQuestionDetailRow",
      checks = scheme => Seq(fullReturn.transaction.isDefined),
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    )
  }

}
