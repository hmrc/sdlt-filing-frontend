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

package controllers.submission

import controllers.actions.*
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.submission.DeclarationView

import javax.inject.Inject
import scala.concurrent.Future

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DeclarationView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      // val userAnswers = request.userAnswers // TODO sprint 17 - DTR-5721 - route to DS-2
      val declaration = Option("DS-1")
      declaration match {
            case Some(value) =>  Ok(view(value, mode))
            case None =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      // val userAnswers = request.userAnswers // TODO sprint 17 - DTR-5721 - route to DS-2
      val declaration = Option("DS-1")
      declaration match {
        case Some(value) => Future.successful(Redirect(controllers.submission.routes.DeclarationController.onPageLoad())) // TODO sprint 17 - route to DS-4
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
