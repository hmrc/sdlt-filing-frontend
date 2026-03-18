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

package controllers.land

import controllers.actions.*
import forms.land.LandMineralsOrMineralRightsFormProvider
import models.land.LandTypeOfProperty
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.land.{LandMineralsOrMineralRightsPage, LandTypeOfPropertyPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LandMineralsOrMineralRightsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LandMineralsOrMineralRightsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: LandMineralsOrMineralRightsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LandMineralsOrMineralRightsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(LandTypeOfPropertyPage) match {
        case None =>
          Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))

        case Some(_) =>
          val preparedForm = request.userAnswers.get(LandMineralsOrMineralRightsPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(LandTypeOfPropertyPage) match {
        case None =>
          Future.successful(Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode)))

        case Some(propertyType) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(LandMineralsOrMineralRightsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                propertyType match {
                  case LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential =>
                    Redirect(navigator.nextPage(LandMineralsOrMineralRightsPage, mode, updatedAnswers))
                  case _ =>
                    // TODO DTR-2495: Redirect to CYA page
                  Redirect(controllers.land.routes.LandMineralsOrMineralRightsController.onPageLoad(NormalMode))
                }
              }
          )
      }
  }
}
