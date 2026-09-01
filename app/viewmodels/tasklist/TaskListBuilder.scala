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

package viewmodels.tasklist

import config.FrontendAppConfig
import models.UserAnswers
import play.api.i18n.Messages
import services.crossflow.fields.CrossFlowValidationService
import services.crossflow.{ReturnSection, SectionStatus}
import utils.{LeaseHelper, PropertyTypeHelper}

import javax.inject.{Inject, Singleton}

@Singleton
class TaskListBuilder @Inject()(crossFlowService: CrossFlowValidationService) {

  private def statusFor(userAnswers: UserAnswers, section: ReturnSection): SectionStatus =
    crossFlowService.sectionStatuses(userAnswers)
      .getOrElse(section, SectionStatus(section, false, Nil, Nil, Nil))

  def displaySections(userAnswers: UserAnswers)
                     (implicit messages: Messages, appConfig: FrontendAppConfig): Seq[TaskListSection] =
    userAnswers.fullReturn.fold(Seq.empty[TaskListSection]) { fullReturn =>
      List(
        Some(PurchaserTaskList.build(fullReturn)),
        if (PurchaserTaskList.isPurchaserComplete(fullReturn)) Some(PurchaserAgentTaskList.build(fullReturn)) else None,
        Some(VendorTaskList.build(fullReturn)),
        if (VendorTaskList.isVendorComplete(fullReturn)) Some(VendorAgentTaskList.build(fullReturn)) else None,
        Some(LandTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Land))),
        if (PropertyTypeHelper.isResidentialProperty(fullReturn)) Some(UkResidencyTaskList.build(fullReturn)) else None,
        Some(TransactionTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Transaction))),
        if (LeaseHelper.isLeaseType(fullReturn)) Some(LeaseTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Lease))) else None,
        Some(TaxCalculationTaskList.build(fullReturn))
      ).flatten
    }

  def completenessSections(userAnswers: UserAnswers)
                          (implicit messages: Messages, appConfig: FrontendAppConfig): Seq[TaskListSection] =
    userAnswers.fullReturn.fold(Seq.empty[TaskListSection]) { fullReturn =>
      List(
        Some(VendorTaskList.build(fullReturn)),
        Some(VendorAgentTaskList.build(fullReturn)),
        Some(PurchaserTaskList.build(fullReturn)),
        Some(PurchaserAgentTaskList.build(fullReturn)),
        Some(LandTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Land))),
        if (PropertyTypeHelper.isResidentialProperty(fullReturn)) Some(UkResidencyTaskList.build(fullReturn)) else None,
        Some(TransactionTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Transaction))),
        if (LeaseHelper.isLeaseType(fullReturn)) Some(LeaseTaskList.build(fullReturn, statusFor(userAnswers, ReturnSection.Lease))) else None,
        Some(TaxCalculationTaskList.build(fullReturn))
      ).flatten
    }

  def allComplete(userAnswers: UserAnswers)
                 (implicit messages: Messages, appConfig: FrontendAppConfig): Boolean = {
    val sections = completenessSections(userAnswers)
    sections.nonEmpty && TaskListSections.allComplete(sections)
  }

  def sections(userAnswers: UserAnswers)
              (implicit messages: Messages, appConfig: FrontendAppConfig): Seq[TaskListSection] =
    userAnswers.fullReturn.fold(Seq.empty[TaskListSection]) { fullReturn =>
      displaySections(userAnswers) :+
        SubmissionTaskList.build(fullReturn, completenessSections(userAnswers))
    }
}