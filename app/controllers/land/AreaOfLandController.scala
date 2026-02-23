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
import forms.land.AreaOfLandFormProvider
import models.Mode
import navigation.Navigator
import pages.land.{AreaOfLandPage, LandSelectMeasurementUnitPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.land.LandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.AreaOfLandView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AreaOfLandController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AreaOfLandFormProvider,
                                        landService: LandService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AreaOfLandView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      
      request.userAnswers.get(LandSelectMeasurementUnitPage).map { unitType =>
        val form = formProvider(unitType.toString)
        val preparedForm = request.userAnswers.get(AreaOfLandPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        landService.propertyTypeCheck(request.userAnswers, Ok(view(preparedForm, mode, unitType.toString)))
      }.getOrElse(
        Redirect(controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(mode))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(LandSelectMeasurementUnitPage).map { unitType =>
        formProvider(unitType.toString).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, unitType.toString))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AreaOfLandPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AreaOfLandPage, mode, updatedAnswers))
        )
      }.getOrElse(
        Future.successful(Redirect(controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(mode)))
      )
  }
}
