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
import forms.land.LandTypeOfPropertyFormProvider
import models.Mode
import models.land.LandTypeOfProperty.{Additional, Residential}
import navigation.Navigator
import pages.land.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.Pages
import services.crossflow.fields.{CrossFlowFormSupport, CrossFlowValidationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LandTypeOfPropertyView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandTypeOfPropertyController @Inject()(
                                              override val messagesApi:  MessagesApi,
                                              sessionRepository:         SessionRepository,
                                              navigator:                 Navigator,
                                              identify:                  IdentifierAction,
                                              getData:                   DataRetrievalAction,
                                              requireData:               DataRequiredAction,
                                              statusCheck:               CheckSubmissionStatusAction,
                                              formProvider:              LandTypeOfPropertyFormProvider,
                                              crossFlow:                 CrossFlowValidationService,
                                              val controllerComponents:  MessagesControllerComponents,
                                              view:                      LandTypeOfPropertyView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      val preparedForm = request.userAnswers.get(LandTypeOfPropertyPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      CrossFlowFormSupport.bindFromRequestWithCrossFlow(form, Pages.LandPropertyType, crossFlow) { value =>
        val withValue = request.userAnswers.set(LandTypeOfPropertyPage, value).get

        val cleaned =
          if (value == Residential || value == Additional)
            withValue
              .remove(AgriculturalOrDevelopmentalLandPage)
              .flatMap(_.remove(DoYouKnowTheAreaOfLandPage))
              .flatMap(_.remove(AreaOfLandPage))
              .flatMap(_.remove(LandSelectMeasurementUnitPage))
              .getOrElse(withValue)
          else
            withValue

        cleaned
      } match {

        case Left(formWithErrors) =>
          Future.successful(BadRequest(view(formWithErrors, mode)))

        case Right((value, updatedAnswers)) =>
          for {
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(LandTypeOfPropertyPage, mode, updatedAnswers))
      }
  }
}