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
import models.{FullReturn, Vendor}
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

  // ── Per-vendor: the single source of truth for the mandatory fields ───────
  def mandatoryFieldsDefined(vendor: Vendor): Seq[Boolean] =
    Seq(
      vendor.name.isDefined,
      vendor.address1.isDefined
    )

  def isVendorComplete(vendor: Vendor): Boolean =
    mandatoryFieldsDefined(vendor).forall(identity)

  // ── Across all vendors on the return ──────────────────────────────────────
  def vendors(fullReturn: FullReturn): Seq[Vendor] =
    fullReturn.vendor.getOrElse(Seq.empty)

  // The vendors still missing a mandatory field — what the overview lists.
  def incompleteVendors(fullReturn: FullReturn): Seq[Vendor] =
    vendors(fullReturn).filterNot(vendor => isVendorComplete(vendor))

  // Flattened checks across every vendor, so the task-list row reflects them
  // all. No vendors => two failing checks (row shows "not started").
  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val all = vendors(fullReturn)
    if (all.isEmpty) Seq.fill(2)(false)
    else all.flatMap(vendor => mandatoryFieldsDefined(vendor))
  }

  // Complete only when there is at least one vendor and every one is complete.
  def isVendorComplete(fullReturn: FullReturn): Boolean = {
    val all = vendors(fullReturn)
    all.nonEmpty && all.forall(vendor => isVendorComplete(vendor))
  }

  def vendorRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url =
      if (isVendorComplete(fullReturn))
        controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      else if (incompleteVendors(fullReturn).nonEmpty) {
        //TODO redirect to incomplete overview
//        controllers.vendor.routes.VendorIncompleteOverviewController.onPageLoad().url
        controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      } else
        controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _           => true
      },
      messageKey = _ => "tasklist.vendorQuestion.details",
      url = _ => _ => url,
      tagId = "vendorQuestionDetailRow",
      checks = _ => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => Seq()
    )
  }

  def buildVendorRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    vendorRowBuilder(fullReturn).build(fullReturn)
}