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

package controllers.ukResidency

import controllers.actions.*
import forms.ukResidency.CrownEmploymentReliefFormProvider
import models.land.LandTypeOfProperty
import models.Mode
import navigation.Navigator
import pages.ukResidency.CrownEmploymentReliefPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukResidency.CrownEmploymentReliefView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CrownEmploymentReliefController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CrownEmploymentReliefFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CrownEmploymentReliefView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CrownEmploymentReliefPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val propertyType = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.headOption)
        .flatMap(_.propertyType)
        .flatMap(LandTypeOfProperty.enumerable.withName)

      propertyType match {
        case Some(LandTypeOfProperty.Residential | LandTypeOfProperty.Additional) =>
          Ok(view(preparedForm, mode))

        //TODO - DTR-2511 - SPRINT 12 - update to UK residency check your answers
        case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CrownEmploymentReliefPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CrownEmploymentReliefPage, mode, updatedAnswers))
      )
  }
}
