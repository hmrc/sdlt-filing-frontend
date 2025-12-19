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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.IsPurchaserActingAsTrusteeFormProvider
import models.purchaser.NameOfPurchaser
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{IsPurchaserActingAsTrusteePage, NameOfPurchaserPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.IsPurchaserActingAsTrusteeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsPurchaserActingAsTrusteeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: IsPurchaserActingAsTrusteeFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: IsPurchaserActingAsTrusteeView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case Some(nameOfPurchaser) =>
          val preparedForm = request.userAnswers.get(IsPurchaserActingAsTrusteePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, nameOfPurchaser.fullName))
      }
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))

        case Some(nameOfPurchaser) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, nameOfPurchaser.fullName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsPurchaserActingAsTrusteePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsPurchaserActingAsTrusteePage, mode, updatedAnswers))
          )
      }
  }
}
