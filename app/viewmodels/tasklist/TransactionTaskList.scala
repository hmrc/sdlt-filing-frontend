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
import models.land.LandTypeOfProperty
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

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {

    // All transaction types
    val generalTransactionFields = Seq(
      fullReturn.transaction.exists(_.transactionDescription.isDefined),
      fullReturn.transaction.exists(_.effectiveDate.isDefined),
      fullReturn.transaction.exists(_.isDependantOnFutureEvent.isDefined),
      fullReturn.transaction.exists(_.agreedToDeferPayment.isDefined),
      fullReturn.transaction.exists(_.isPartOfSaleOfBusiness.isDefined),
      fullReturn.transaction.exists(_.postTransRulingApplied.isDefined),
      fullReturn.transaction.exists(_.restrictionsAffectInterest.isDefined),
      fullReturn.transaction.exists(_.isLandExchanged.isDefined),
      fullReturn.transaction.exists(_.isPursuantToPreviousOption.isDefined)
    )

    // if property type is non-residential or mixed
    val isPropertyTypeMixedOrNonResidential: Boolean = {
      val mainLandId = fullReturn.returnInfo.flatMap(_.mainLandID)
      val typeOfProperty = fullReturn.land.flatMap(_.find(l => l.landID == mainLandId)).flatMap(_.propertyType)

      typeOfProperty match {
        case Some(LandTypeOfProperty.Mixed.toString | LandTypeOfProperty.NonResidential.toString) => true
        case _ => false
      }
    }

    val isAnyUseOfLandYes = fullReturn.transaction.exists {
      use =>
        List(
          use.usedAsFactory,
          use.usedAsHotel,
          use.usedAsIndustrial,
          use.usedAsOffice,
          use.usedAsOther,
          use.usedAsShop,
          use.usedAsWarehouse,
        ).exists(_.exists(_.equalsIgnoreCase("yes")))
    }

    // if transaction type is not Grand of Lease
    val isTransactionTypeNotGrandOfLease = fullReturn.transaction.exists(!_.transactionDescription.contains("L"))

    val isTotalConsiderationDefined = fullReturn.transaction.exists(_.totalConsideration.isDefined)

    val isAnyFormsOfConsiderationDefined = {
      fullReturn.transaction.exists {
        form =>
          List(
            form.considerationCash,
            form.considerationDebt,
            form.considerationBuild,
            form.considerationEmploy,
            form.considerationOther,
            form.considerationSharesQTD,
            form.considerationSharesUNQTD,
            form.considerationLand,
            form.considerationServices,
            form.considerationContingent
          ).exists(_.exists(_.equalsIgnoreCase("yes")))
      }
    }

    (isPropertyTypeMixedOrNonResidential, isTransactionTypeNotGrandOfLease) match {
        case (true, true) =>
          generalTransactionFields ++ Seq(isAnyUseOfLandYes) ++  Seq(isTotalConsiderationDefined, isAnyFormsOfConsiderationDefined)
        case (true, false) =>
          generalTransactionFields ++ Seq(isAnyUseOfLandYes)
        case (false, true) =>
          generalTransactionFields ++ Seq(isTotalConsiderationDefined, isAnyFormsOfConsiderationDefined)
        case (false, false) =>
          generalTransactionFields
      }
    }

  def isTransactionComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
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
      checks        = _ => mandatoryFieldsDefined(fullReturn),
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq()
    )
  }

  def buildTransactionRow(fullReturn: FullReturn, status: SectionStatus)
                         (implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    transactionRowBuilder(fullReturn, status).build(fullReturn)
}