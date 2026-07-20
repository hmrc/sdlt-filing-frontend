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

package controllers.taxCalculation.freeholdSelfAssessed

import controllers.actions.*
import models.NormalMode
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CannotCalculateHelper.getCannotCalculateReason
import viewmodels.taxCalculation.selfAssessedViewModels.CannotCalculateViewModel
import views.html.taxCalculation.shared.CannotCalculateSdltDueView

import javax.inject.{Inject, Singleton}

@Singleton
class FreeholdCannotCalculateSdltDueController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   statusCheck: CheckSubmissionStatusAction,
                                                   sdltCalculationService: SdltCalculationService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CannotCalculateSdltDueView
                                                 ) extends FrontendBaseController with I18nSupport {

  private val sectionKey: String = "site.taxCalculation.freeholdSelfAssessed.section"

  private lazy val continueUrl: String = routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(NormalMode).url

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>
      sdltCalculationService.whenInFlow(FreeholdSelfAssessed) {
        val reasons = getCannotCalculateReason(request.userAnswers)
        val viewModel = CannotCalculateViewModel.toViewModel(reasons)
        Ok(view(viewModel, sectionKey, continueUrl))
      }
  }
}
