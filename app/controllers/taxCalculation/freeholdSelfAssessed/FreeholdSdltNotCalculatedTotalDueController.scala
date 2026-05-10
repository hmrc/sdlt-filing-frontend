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

import controllers.actions.*
import forms.taxCalculation.TotalAmountToPayFormProvider
import models.taxCalculation.*
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.taxCalculation.freeholdSelfAssessed.*
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.taxCalculation.CalculatedTotalDueHelper
import views.html.taxCalculation.freeholdSelfAssessed.TotalAmountDueView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FreeholdSdltNotCalculatedTotalDueController @Inject()(
                                                             override val messagesApi: MessagesApi,
                                                             identify: IdentifierAction,
                                                             getData: DataRetrievalAction,
                                                             requireData: DataRequiredAction,
                                                             formProvider: TotalAmountToPayFormProvider,
                                                             sdltCalculationService: SdltCalculationService,
                                                             sessionRepository: SessionRepository,
                                                             navigator: Navigator,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             view: TotalAmountDueView
                                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {


  lazy val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sdltCalculationService.validateAsyncFlow(FreeholdSelfAssessed) {
      sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).flatMap {
        case Right(result) =>
          CalculatedTotalDueHelper.getEffectiveDate(request.userAnswers) match {
            case Right(effectiveDateOfTransaction) =>
              val freeHoldSelfAssessedTotalAmountDue = 
                CalculatedTotalDueHelper
                  .createFreeHoldSelfAssessedTotalAmountDue(result.totalTax, effectiveDateOfTransaction)
              for {
                updatedUserAnswers <- Future.fromTry(request.userAnswers.set(FreeholdSelfAssessedTotalAmountDueSummaryPage, freeHoldSelfAssessedTotalAmountDue))
                _ <- sessionRepository.set(updatedUserAnswers)
              } yield {
                val preparedForm = request.userAnswers.get(TotalAmountDuePage) match {
                  case None => form
                  case Some(value) => form.fill(value.amount)
                }
                CalculatedTotalDueHelper.getSummaryListRows(updatedUserAnswers) match {
                  case Some(summaryListRow) => Ok(view(preparedForm, summaryListRow, mode))
                  case None =>
                    logger.warn(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onSubmit] Cannot retrieve FreeholdSelfAssessedTotalAmountDueSummaryPage for summary List row  from userAnswers")
                    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                }
              }
            case Left(error) => error match {
              case InvalidEffectiveDateOfTransactionError =>
                logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] effective date parsing failed invalid date:${error.message}")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

              case MissingEffectiveDateOfTransactionError =>
                logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] missing effective date of transaction from userAnswers:${error.message}")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
          }
        case Left(error: MissingDataError) => error match {
          case MissingFullReturnError =>
            logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] sdltc reported missing full return: ${error.message}")
            Future.successful(Redirect(controllers.routes.NoReturnReferenceController.onPageLoad()))
          case _ =>
            logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] sdltc reported missing data: ${error.message}")
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
        }
      }
    }
  }


  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sdltCalculationService.validateAsyncFlow(FreeholdSelfAssessed) {
      val summaryListRow = CalculatedTotalDueHelper.getSummaryListRows(request.userAnswers)
      summaryListRow match {
        case Some(summaryListRow) =>
          form.bindFromRequest()
            .fold(
              formWithErrors =>
                logger.warn(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onSubmit] error with the user total amount due value")
                Future.successful(BadRequest(view(formWithErrors, summaryListRow, mode))),
              value =>
                val totalAmountDue = CalculatedTotalDueHelper.totalAmountDue(value)
                for {
                  updateUserAnswers <- Future.fromTry(request.userAnswers.set(TotalAmountDuePage, totalAmountDue))
                  _ <- sessionRepository.set(updateUserAnswers)
                } yield {
                  logger.info(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onSubmit] user answers saved redirecting")
                  Redirect(navigator.nextPage(FreeholdSelfAssessedTotalAmountDueSummaryPage, mode, updateUserAnswers))
                }
            )
        case None =>
          logger.warn(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onSubmit] Cannot retrieve FreeholdSelfAssessedTotalAmountDueSummaryPage for summary List row  from userAnswers")
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
    }
  }


}



