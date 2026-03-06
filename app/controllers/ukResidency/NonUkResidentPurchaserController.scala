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
import forms.ukResidency.NonUkResidentPurchaserFormProvider
import models.land.LandTypeOfProperty
import models.Mode
import navigation.Navigator
import pages.ukResidency.NonUkResidentPurchaserPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukResidency.NonUkResidentPurchaserView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonUkResidentPurchaserController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  sessionRepository: SessionRepository,
                                                  navigator: Navigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: NonUkResidentPurchaserFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: NonUkResidentPurchaserView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    request.userAnswers.fullReturn match {

      case Some(fullReturn) =>

        val propertyType = fullReturn.land.flatMap(_.headOption).flatMap(_.propertyType)
          .flatMap(LandTypeOfProperty.enumerable.withName)

        propertyType match {

          case Some(LandTypeOfProperty.Residential | LandTypeOfProperty.Additional) =>

            val preparedForm =
              request.userAnswers.get(NonUkResidentPurchaserPage).fold(form)(form.fill)

            Ok(view(preparedForm, mode))

          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad() // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
            )
        }

      case None =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()) // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val isCompany: Boolean = request.userAnswers.fullReturn.flatMap(_.purchaser)
        .flatMap(_.headOption).flatMap(_.isCompany).contains("YES")

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NonUkResidentPurchaserPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            if (isCompany) {
              Redirect(navigator.nextPage(NonUkResidentPurchaserPage, mode, updatedAnswers))
            } else if (value) {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) //TODO - DTR-3002 - SPRINT 10 - Connect to CrownEmploymentRelief page
            } else {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
            }
          }
      )
  }
}
