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
import forms.land.AgriculturalOrDevelopmentalLandFormProvider
import models.Mode
import navigation.Navigator
import pages.land.AgriculturalOrDevelopmentalLandPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.land.LandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.AgriculturalOrDevelopmentalLandView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgriculturalOrDevelopmentalLandController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AgriculturalOrDevelopmentalLandFormProvider,
                                         landService: LandService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AgriculturalOrDevelopmentalLandView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgriculturalOrDevelopmentalLandPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      landService.propertyTypeCheck(request.userAnswers, Ok(view(preparedForm, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgriculturalOrDevelopmentalLandPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield if (value) {
            Redirect(navigator.nextPage(AgriculturalOrDevelopmentalLandPage, mode, updatedAnswers))
          } else { // TODO: DTR-2495 - redirect to Land CYA - SPRINT 10
            Redirect(controllers.land.routes.LandBeforeYouStartController.onPageLoad())
          }
      )
  }
}
