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

import com.google.inject.Singleton
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes.ReturnTaskListController
import controllers.taxCalculation.PenaltiesAndInterestExtension
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.PenaltiesAndInterest
import models.requests.DataRequest
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountWithPenaltiesView
import pages.taxCalculation.freeholdSelfAssessed.*
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FreeholdSdltCalculatedPenaltiesAndInterestController @Inject()(
                                                                      override val messagesApi: MessagesApi,
                                                                      identify: IdentifierAction,
                                                                      getData: DataRetrievalAction,
                                                                      requireData: DataRequiredAction,
                                                                      formProvider: PenaltiesAndInterestFormProvider,
                                                                      val controllerComponents: MessagesControllerComponents,
                                                                      sdltCalculationService: SdltCalculationService,
                                                                      view: FreeholdSelfAssessedAmountWithPenaltiesView
                                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with PenaltiesAndInterestExtension with Logging {

  private val form: Form[PenaltiesAndInterest] = formProvider()
  private val secureAction: ActionBuilder[DataRequest, AnyContent] = identify andThen getData andThen requireData

  def onPageLoad: Action[AnyContent] = secureAction {
    implicit request =>
      validateFlow(request.userAnswers)(FreeholdSelfAssessed) match {
        case None =>
          Ok(view(form))
        case Some(firstErrorFound) =>
          logger.error(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onPageLoad] invalid flow state: $firstErrorFound")
          Redirect(ReturnTaskListController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = secureAction.async { implicit request =>
    validateFlow(request.userAnswers)(FreeholdSelfAssessed) match {
      case None =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              logger.error(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onSubmit] form submitting error")
              Future.successful(BadRequest(view(formWithErrors))),
            {
              yesOrNoSelected =>
                sdltCalculationService
                  .savePenaltiesAndInterestYesNoAnswer(
                    key = FreeholdSelfAssessedPenaltiesAndInterestPage,
                    value = yesOrNoSelected)
                  .map { _ =>
                    logger.info(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onSubmit] userAnswer saved :: redirecting")
                    Redirect(controllers.taxCalculation.freeholdSelfAssessed
                      .routes.FreeholdSdltCalculatedPenaltiesAndInterestController.onPageLoad())
                  }
            }
          )
      case Some(firstErrorFound) =>
        logger.error(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onSubmit] invalid flow state: $firstErrorFound")
        Future.successful(Redirect(ReturnTaskListController.onPageLoad()))
    }
  }

}