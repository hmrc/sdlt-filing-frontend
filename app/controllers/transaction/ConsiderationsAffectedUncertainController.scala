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

package controllers.transaction

import controllers.actions.*
import forms.transaction.ConsiderationsAffectedUncertainFormProvider
import models.land.LandTypeOfProperty.{Mixed, NonResidential}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.transaction.{ConsiderationsAffectedUncertainPage, TransactionDeferringPaymentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.land.LandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.ConsiderationsAffectedUncertainView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class ConsiderationsAffectedUncertainController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       statusCheck: CheckSubmissionStatusAction,
                                       formProvider: ConsiderationsAffectedUncertainFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       landService: LandService,
                                       view: ConsiderationsAffectedUncertainView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ConsiderationsAffectedUncertainPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ConsiderationsAffectedUncertainPage, value))
            finalAnswers <- Future.fromTry {
              if !value then updatedAnswers.remove(TransactionDeferringPaymentPage)
              else Success(updatedAnswers)
            }
            _ <- sessionRepository.set(finalAnswers)
          } yield {
            if (!value && mode == NormalMode) {
              landService.getMainLand(request.userAnswers).flatMap(_.propertyType) match {
                case Some(Mixed.toString | NonResidential.toString) =>
                  Redirect(controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(mode))
                case _ =>
                  Redirect(controllers.transaction.routes.SaleOfBusinessController.onPageLoad(mode))
              }
            } else {
              Redirect(navigator.nextPage(ConsiderationsAffectedUncertainPage, mode, updatedAnswers))
            }
          }
      )
  }
}
