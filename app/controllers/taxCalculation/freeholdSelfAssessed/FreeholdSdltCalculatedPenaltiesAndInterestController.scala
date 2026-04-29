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

package controllers.taxCalculation.freeholdSelfAssessed

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.PenaltiesAndInterest
import models.PenaltiesAndInterest.AmountIncludePenaltiesAndInterestYes
import models.requests.DataRequest
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedPenaltiesAndInterestPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountWithPenaltiesView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class FreeholdSdltCalculatedPenaltiesAndInterestController @Inject()(
                                                                      override val messagesApi: MessagesApi,
                                                                      identify: IdentifierAction,
                                                                      getData: DataRetrievalAction,
                                                                      requireData: DataRequiredAction,
                                                                      formProvider: PenaltiesAndInterestFormProvider,
                                                                      sdltCalculationService: SdltCalculationService,
                                                                      val controllerComponents: MessagesControllerComponents,
                                                                      view: FreeholdSelfAssessedAmountWithPenaltiesView
                                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[PenaltiesAndInterest] = formProvider()
  private val secureAction: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData

  // TODO: do we need to throw and error in case there is no penalties provided from TC-5?

  def onPageLoad: Action[AnyContent] = secureAction {
    implicit request =>
      sdltCalculationService.whenInFlow(FreeholdSelfAssessed) {
        Ok(view(form))
      }
  }

  // TODO: add test(s) around this logic
  def onSubmit(): Action[AnyContent] = secureAction.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      {
        yesNoAnswerSelected =>
          for {
            result <- Future.fromTry {
              request
                .userAnswers
                .set(FreeholdSelfAssessedPenaltiesAndInterestPage,
                  yesNoAnswerSelected == AmountIncludePenaltiesAndInterestYes)
            }
          } yield
            Redirect(controllers.routes.IndexController.onPageLoad())
        // TODO: save result and progress to CheckYourAnswers

      }
    )
  }

}