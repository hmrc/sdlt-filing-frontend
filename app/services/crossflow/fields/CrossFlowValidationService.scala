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

package services.crossflow.fields

import models.UserAnswers
import services.crossflow.*
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}

@Singleton
class CrossFlowValidationService @Inject() (rules: Set[CrossFlowRule]) extends LoggingUtil :
  logger.info(s"[CrossFlow] loaded ${rules.size} rules: ${rules.map(_.id).toSeq.sorted}")

  private val ruleList = rules.toSeq

  private val byAffectedSection: Map[ReturnSection, Seq[CrossFlowRule]] =
    ruleList.groupBy(_.affects)

  private val byTargetPage: Map[PageId, Seq[CrossFlowRule]] =
    ruleList.flatMap(r => r.targets.map(_.page).distinct.map(_ -> r))
      .groupBy(_._1).view.mapValues(_.map(_._2)).toMap

  def validateAll(ua: UserAnswers): Seq[CrossFlowFailure] =
    ruleList.flatMap(_.validate(ua)).sortBy(_.ruleId)

  def failuresAffecting(section: ReturnSection, ua: UserAnswers): Seq[CrossFlowFailure] =
    byAffectedSection.getOrElse(section, Nil).flatMap(_.validate(ua)).sortBy(_.ruleId)

  /** Failures that can be shown on a page — onPageLoad and form binding. */
  def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] =
    byTargetPage.getOrElse(page, Nil).flatMap(_.validate(ua)).filter(_.appearsOn(page)).sortBy(_.ruleId)
  
  def isSectionValid(section: ReturnSection, ua: UserAnswers): Boolean =
    byAffectedSection.getOrElse(section, Nil).forall(_.validate(ua).isEmpty)
  
  def invalidSections(ua: UserAnswers): Set[ReturnSection] =
    validateAll(ua).map(_.affects).toSet

  def hasFailures(ua: UserAnswers): Boolean =
    rules.exists(_.validate(ua).isDefined)
  
  def sectionStatus(section: ReturnSection, ua: UserAnswers): SectionStatus =
    val fs = failuresAffecting(section, ua)
    if fs.isEmpty then SectionStatus.empty(section)
    else SectionStatus(
      section     = section,
      hasFailures = true,
      ruleIds     = fs.map(_.ruleId),
      messageKeys = fs.map(_.messageKey).distinct,
      targets     = fs.flatMap(_.targets).distinct
    )

  def sectionStatuses(ua: UserAnswers): Map[ReturnSection, SectionStatus] =
    validateAll(ua).groupBy(_.affects).map { case (sec, fs) =>
      sec -> SectionStatus(
        section     = sec,
        hasFailures = true,
        ruleIds     = fs.map(_.ruleId),
        messageKeys = fs.map(_.messageKey).distinct,
        targets     = fs.flatMap(_.targets).distinct
      )
    }
  
  def rulesForPage(page: PageId): Seq[CrossFlowRule] =
    byTargetPage.getOrElse(page, Nil)

  def rulesTriggeredBy(section: ReturnSection): Seq[CrossFlowRule] =
    ruleList.filter(_.inputs.contains(section))