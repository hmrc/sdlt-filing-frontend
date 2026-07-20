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

package controllers.taxCalculation.leaseholdTaxCalculated

import controllers.actions.{CheckSubmissionStatusAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.Mode
import models.taxCalculation.TaxCalculationFlow.LeaseholdTaxCalculated
import navigation.Navigator
import pages.taxCalculation.leaseholdTaxCalculated.LeaseholdTaxCalculatedPenaltiesAndInterestPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil
import views.html.taxCalculation.AmountWithPenaltiesView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}

@Singleton
class LeaseholdSdltCalculatedPenaltiesAndInterestController @Inject()(
                                                                       override val messagesApi: MessagesApi,
                                                                       identify: IdentifierAction,
                                                                       getData: DataRetrievalAction,
                                                                       requireData: DataRequiredAction,
                                                                       statusCheck: CheckSubmissionStatusAction,
                                                                       formProvider: PenaltiesAndInterestFormProvider,
                                                                       val controllerComponents: MessagesControllerComponents,
                                                                       sdltCalculationService: SdltCalculationService,
                                                                       sessionRepository: SessionRepository,
                                                                       navigator: Navigator,
                                                                       view: AmountWithPenaltiesView
                                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with LoggingUtil {

  private val form = formProvider()
  private val postAction: Mode => Call = mode =>
    controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdSdltCalculatedPenaltiesAndInterestController.onSubmit(mode)
  private val sectionKey: String = "taxCalculation.penaltiesAndInterest.leasehold-tax-calculated.title"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>
      sdltCalculationService.whenInFlow(LeaseholdTaxCalculated) {
        val preparedForm = request.userAnswers.get(LeaseholdTaxCalculatedPenaltiesAndInterestPage).fold(form)(form.fill)
        Ok(view(preparedForm, sectionKey, postAction(mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(LeaseholdTaxCalculated) {
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, sectionKey, postAction(mode)))),
            {
              yesOrNoSelected =>
                for {
                  updatedAnswers <- Future.fromTry {
                    request.userAnswers.set(
                      LeaseholdTaxCalculatedPenaltiesAndInterestPage,
                      yesOrNoSelected
                    )
                  }
                  _ <- sessionRepository.set(updatedAnswers)
                } yield {
                  Redirect(navigator.nextPage(LeaseholdTaxCalculatedPenaltiesAndInterestPage, mode, userAnswers = updatedAnswers))
                }

            }
          )
      }
  }

}