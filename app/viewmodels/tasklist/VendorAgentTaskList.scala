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
import models.{FullReturn, NormalMode}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object VendorAgentTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.vendorAgentQuestion.heading"),
      rows = Seq(
        buildVendorAgentRow(fullReturn)
      )
    )

  def buildVendorAgentRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow = {
    
    val vendorAgentCheck: Boolean = fullReturn.returnAgent.exists(_.exists(_.agentType.contains("VENDOR")))

    val url = if(vendorAgentCheck) {
      //TODO Change to the Vendor agent Overview page
        controllers.vendor.routes.VendorOverviewController.onPageLoad().url
    } else {
      //TODO Change to the Vendor agent Before You Start Page
      controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
    }
    
    TaskListRowBuilder(
      isOptional = true,
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.vendorAgentQuestion.details",
      url = _ => _ => {url},
      tagId = "vendorQuestionDetailRow",
      checks = scheme => Seq(vendorAgentCheck),
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    ).build(fullReturn)
  }

}
