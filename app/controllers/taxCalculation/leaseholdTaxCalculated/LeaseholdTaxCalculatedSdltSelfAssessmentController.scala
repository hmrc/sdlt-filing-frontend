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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.taxCalculation.TaxCalculationErrorRecovery
import forms.taxCalculation.SdltSelfAssessmentFormProvider
import models.Mode
import models.taxCalculation.TaxCalculationFlow.LeaseholdTaxCalculated
import navigation.Navigator
import pages.taxCalculation.leaseholdTaxCalculated.LeaseholdTaxCalculatedSelfAssessedAmountPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.shared.SdltSelfAssessmentView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeaseholdTaxCalculatedSdltSelfAssessmentController @Inject()(
                                                                   override val messagesApi: MessagesApi,
                                                                   identify: IdentifierAction,
                                                                   getData: DataRetrievalAction,
                                                                   requireData: DataRequiredAction,
                                                                   sdltCalculationService: SdltCalculationService,
                                                                   sessionRepository: SessionRepository,
                                                                   navigator: Navigator,
                                                                   formProvider: SdltSelfAssessmentFormProvider,
                                                                   val controllerComponents: MessagesControllerComponents,
                                                                   view: SdltSelfAssessmentView
                                                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  private val form: Form[String] = formProvider()

  private val sectionKey: String = "site.taxCalculation.leaseholdSdltCalculated.section"

  private def postAction(mode: Mode): Call =
    routes.LeaseholdTaxCalculatedSdltSelfAssessmentController.onSubmit(mode)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(LeaseholdTaxCalculated) {
        sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).map {
          case Right(result) =>
            val prepared = request.userAnswers.get(LeaseholdTaxCalculatedSelfAssessedAmountPage)
              .fold(
                form.fill(result.totalTax.toString)
              )(
                form.fill
              )
            Ok(view(prepared, postAction(mode), sectionKey))
          case Left(err) =>
            logger.warn(s"[LeaseholdTaxCalculatedSdltSelfAssessmentController][onPageLoad] sdltc failed: ${err.message}")
            Redirect(errorHandler(err))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(LeaseholdTaxCalculated) {
        sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).flatMap {
          case Right(_) =>
            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, postAction(mode), sectionKey))),
              value =>
                for {
                  updated <- Future.fromTry(request.userAnswers.set(LeaseholdTaxCalculatedSelfAssessedAmountPage, value))
                  _       <- sessionRepository.set(updated)
                } yield Redirect(navigator.nextPage(LeaseholdTaxCalculatedSelfAssessedAmountPage, mode, updated))
            )
          case Left(err) =>
            logger.warn(s"[LeaseholdTaxCalculatedSdltSelfAssessmentController][onSubmit] sdltc failed: ${err.message}")
            Future.successful(Redirect(errorHandler(err)))
        }
      }
  }
}
