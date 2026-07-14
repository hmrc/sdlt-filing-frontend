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
import utils.PropertyTypeHelper

import javax.inject.Singleton

@Singleton
object UkResidencyTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.ukResidencyQuestion.heading"),
      rows = Seq(
        buildUkResidencyRow(fullReturn)
      )
    )

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val isCompany: Boolean = fullReturn.purchaser
      .getOrElse(Seq.empty)
      .exists(_.isCompany.contains("YES"))
    val isNonUkResidents = fullReturn.residency.exists(_.isNonUkResidents.exists(_.equalsIgnoreCase("YES")))
    val isNonUkResidentsDefined = fullReturn.residency.exists(_.isNonUkResidents.isDefined)
    val isCloseCompanyDefined = fullReturn.residency.exists(_.isCloseCompany.isDefined)
    val isCrownReliefDefined = fullReturn.residency.exists(_.isCrownRelief.isDefined)

    (isCompany, isNonUkResidents) match {
      case (true, true) =>
        Seq(isCloseCompanyDefined, isCrownReliefDefined)
      case (true, false) =>
        Seq(isCloseCompanyDefined)
      case (false, true) =>
        Seq(isCrownReliefDefined)
      case (false, false) =>
        Seq(isNonUkResidentsDefined)
    }
  }

  def isResidencyComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  private def isResidencyRequired(fullReturn: FullReturn): Boolean = {
    PropertyTypeHelper.isResidentialProperty(fullReturn)
  }

  private def isResidencyStarted(fullReturn: FullReturn): Boolean =
    fullReturn.residency.exists { res =>
      res.isNonUkResidents.isDefined ||
      res.isCloseCompany.isDefined ||
      res.isCrownRelief.isDefined
    }
  
  def ukResidencyRowBuilder(fullReturn: FullReturn)
                                 (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {


    val url = if(isResidencyComplete(fullReturn)) {
        controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
      } else {
        controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
      }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.ukResidencyQuestion.details",
      url = _ => _ => url,
      tagId = "ukResidencyQuestionRow",
      checks = _ => mandatoryFieldsDefined(fullReturn),
      started       = _ => if (isResidencyRequired(fullReturn)) isResidencyStarted(fullReturn) else false,
      prerequisites = _ => Seq()
    )
  }

  def buildUkResidencyRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    ukResidencyRowBuilder(fullReturn).build(fullReturn)
}