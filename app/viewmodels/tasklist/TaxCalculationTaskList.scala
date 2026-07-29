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
import utils.{LeaseHelper, PropertyTypeHelper}
import viewmodels.tasklist.LandTaskList.isLandComplete
import viewmodels.tasklist.LeaseTaskList.isLeaseComplete
import viewmodels.tasklist.PurchaserAgentTaskList.{isPurchaserAgentComplete, isPurchaserAgentStarted}
import viewmodels.tasklist.PurchaserTaskList.isPurchaserComplete
import viewmodels.tasklist.TransactionTaskList.isTransactionComplete
import viewmodels.tasklist.UkResidencyTaskList.isResidencyComplete
import viewmodels.tasklist.VendorAgentTaskList.{isVendorAgentComplete, isVendorAgentStarted}
import viewmodels.tasklist.VendorTaskList.isVendorComplete

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

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    
    val commonMandatoryFields = Seq(
      fullReturn.taxCalculation.exists(_.amountPaid.isDefined),
      fullReturn.taxCalculation.exists(_.includesPenalty.isDefined)
    )
    
    if (fullReturn.taxCalculation.exists(_.taxDue.isDefined)) {
      commonMandatoryFields ++ Seq(fullReturn.taxCalculation.exists(_.taxDue.isDefined))
    } else {
      commonMandatoryFields ++ Seq(
        fullReturn.taxCalculation.exists(_.taxDuePremium.isDefined),
        fullReturn.taxCalculation.exists(_.taxDueNPV.isDefined)
      )
    }
  }

  def isTaxCalculationComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  private def isLeaseRequired(fullReturn: FullReturn): Boolean = {
    LeaseHelper.isLeaseDefined(fullReturn)
  }

  private def isResidencyRequired(fullReturn: FullReturn): Boolean = {
    PropertyTypeHelper.isResidentialProperty(fullReturn)
  }

  def canStartTaxCalculation(fullReturn: FullReturn): Boolean = {
    isVendorComplete(fullReturn) &&
      isPurchaserComplete(fullReturn) &&
      isLandComplete(fullReturn) &&
      isTransactionComplete(fullReturn) &&
      (!isVendorAgentStarted(fullReturn) || isVendorAgentComplete(fullReturn)) &&
      (!isPurchaserAgentStarted(fullReturn) || isPurchaserAgentComplete(fullReturn)) &&
      (!isLeaseRequired(fullReturn) || isLeaseComplete(fullReturn)) &&
      (!isResidencyRequired(fullReturn) || isResidencyComplete(fullReturn))
  }
  
  def taxCalculationRowBuilder(fullReturn: FullReturn)(implicit messages: Messages, appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url = controllers.taxCalculation.routes.TaxCalculationConfirmEffectiveDateOfTransactionController.onPageLoad().url

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.taxCalculationQuestion.details",
      hint = fullReturn => {
        if (!canStartTaxCalculation(fullReturn))
          Some("tasklist.taxCalculationQuestion.hint")
        else
          None
      },
      url = _ => _ => {
        url
      },
      tagId = "taxCalculationQuestionDetailRow",
      checks = scheme => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => {
        val mandatory = Seq(
          VendorTaskList.vendorRowBuilder(fullReturn),
          PurchaserTaskList.purchaserRowBuilder(fullReturn),
          LandTaskList.landRowBuilder(fullReturn, viewmodels.tasklist.LandTaskList.noFailures),
          TransactionTaskList.transactionRowBuilder(fullReturn, viewmodels.tasklist.TransactionTaskList.noFailures),
        )

        val conditional = Seq(
          Option.when(isLeaseRequired(fullReturn))(
            LeaseTaskList.leaseRowBuilder(fullReturn, viewmodels.tasklist.LeaseTaskList.noFailures)
          ),
          Option.when(isResidencyRequired(fullReturn))(
            UkResidencyTaskList.ukResidencyRowBuilder(fullReturn)
          ),
          Option.when(isLeaseRequired(fullReturn))(
            LeaseTaskList.leaseRowBuilder(fullReturn, viewmodels.tasklist.LeaseTaskList.noFailures)
          ),
          Option.when(isPurchaserAgentStarted(fullReturn))(
            PurchaserAgentTaskList.purchaserAgentRowBuilder(fullReturn)
          ),
          Option.when(isVendorAgentStarted(fullReturn))(
            VendorAgentTaskList.vendorAgentRowBuilder(fullReturn)
          )
        ).flatten

        mandatory ++ conditional
      }
    )
  }

  def buildTaxCalculationRow(fullReturn: FullReturn)(implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSectionRow =
    taxCalculationRowBuilder(fullReturn).build(fullReturn)

}
