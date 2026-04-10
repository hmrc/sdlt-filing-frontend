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
object TaxCalculationTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.taxCalculationQuestion.heading"),
      rows = Seq(
        buildTaxCalculationRow(fullReturn)
      )
    )

  def buildTaxCalculationRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow = {

    // TODO: pattern match between BYS pages & CYA page
    val url = controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url
    
    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.taxCalculationQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "taxCalculationQuestionDetailRow",
      checks = scheme => Seq(fullReturn.taxCalculation.isDefined),
      prerequisites = _ => generatePrerequisites(fullReturn)
    ).build(fullReturn)
  }

  def generatePrerequisites(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): Seq[TaskListRowBuilder] = {
    val ukResidencyPropertyTypes = Set("Residential", "Residential Additional Property") // TODO: check property type inputs
    val hasUkResCheck = fullReturn.land.exists(_.exists(_.propertyType.contains(ukResidencyPropertyTypes)))

    Seq(
      Some(LandTaskList.buildLandRow(fullReturn)),
      Some(TransactionTaskList.buildTransactionRow(fullReturn)),
      if (hasUkResCheck) Some(UkResidencyTaskList.buildUkResidencyRow(fullReturn)) else None
      // TODO: Add lease when tasklist built
    ).flatten
  }
}
