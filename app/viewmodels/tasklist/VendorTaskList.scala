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
object VendorTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection = {
    TaskListSection(
      heading = messages("tasklist.vendorQuestion.heading"),
      rows = Seq(
        buildVendorRow(fullReturn)
      )
    )
  }

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val mainVendorId: Option[String] = fullReturn.returnInfo.flatMap(_.mainVendorID)
    val mainVendor = fullReturn.vendor.flatMap(_.find(vendor => mainVendorId.equals(vendor.vendorID)))

    Seq(
      mainVendor.exists(_.name.isDefined),
      mainVendor.exists(_.address1.isDefined)
    )

  }

  def isVendorComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  def vendorRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url = if (isVendorComplete(fullReturn)) {
      controllers.vendor.routes.VendorOverviewController.onPageLoad().url
    } else {
      controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.vendorQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "vendorQuestionDetailRow",
      checks = _ => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => Seq()
    )
  }

  def buildVendorRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    vendorRowBuilder(fullReturn).build(fullReturn)

}
