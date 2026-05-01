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

package controllers.taxCalculation.leaseholdSelfAssessed

import controllers.actions.*
import forms.taxCalculation.TaxDueOnNpvFormProvider
import models.Mode
import navigation.Navigator
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedTaxDueOnNpvView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeaseholdSelfAssessedTaxDueOnNpvController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: TaxDueOnNpvFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LeaseholdSelfAssessedTaxDueOnNpvView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val npv = request.userAnswers.fullReturn.flatMap(_.lease.flatMap(_.netPresentValue))

      npv match {
        case Some(npv) =>
          val preparedForm = request.userAnswers.get(LeaseholdSelfAssessedNpvTaxPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, npv, mode))
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }


  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val npv = request.userAnswers.fullReturn.flatMap(_.lease.flatMap(_.netPresentValue))

      npv match {
        case Some(npv) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, npv, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseholdSelfAssessedNpvTaxPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(LeaseholdSelfAssessedTotalAmountDuePage, mode, updatedAnswers))
            }
          )
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
