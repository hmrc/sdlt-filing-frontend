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

package controllers.taxCalculation

import controllers.routes.ReturnTaskListController
import models.{NormalMode, UserAnswers}
import models.taxCalculation.{HoldingTypes, TaxCalculationFlow}
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, LeaseholdSelfAssessed}
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.SessionRepository
import utils.TaxCalculationHelper

import scala.concurrent.{ExecutionContext, Future}

object SelfAssessmentFallback {

  private val freeholdSectionKey: String  = "site.taxCalculation.freeholdSelfAssessed.section"
  private val leaseholdSectionKey: String = "site.taxCalculation.leaseholdSelfAssessed.caption"

  private def freeholdContinueUrl: String =
    controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(NormalMode).url
  private def leaseholdContinueUrl: String =
    controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(NormalMode).url

  def byHoldingType(answers: UserAnswers, sessionRepository: SessionRepository)
                   (render: (String, String) => Result)
                   (implicit ec: ExecutionContext): Future[Result] = {

    val selfAssessedFlowAndResult: Option[(TaxCalculationFlow, Result)] =
      TaxCalculationHelper.holdingType(answers).collect {
        case HoldingTypes.freehold  => FreeholdSelfAssessed  -> render(freeholdSectionKey,  freeholdContinueUrl)
        case HoldingTypes.leasehold => LeaseholdSelfAssessed -> render(leaseholdSectionKey, leaseholdContinueUrl)
      }

    selfAssessedFlowAndResult match {
      case Some((flow, page)) =>
        for {
          updated <- Future.fromTry(answers.set(TaxCalculationFlowPage, flow))
          _       <- sessionRepository.set(updated)
        } yield page
      case None =>
        Future.successful(Redirect(ReturnTaskListController.onPageLoad()))
    }
  }
}
