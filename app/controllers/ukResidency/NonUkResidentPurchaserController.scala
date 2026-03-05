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
import models.{GetReturnByRefRequest, Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.WhoIsMakingThePurchasePage
import pages.ukResidency.NonUkResidentPurchaserPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.FullReturnService
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
                                                  fullReturnService: FullReturnService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: NonUkResidentPurchaserView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    val effectiveReturnId = request.userAnswers.returnId

    effectiveReturnId.fold(
      Future.successful(Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()))
    ) { id =>
      fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = request.userAnswers.storn))
        .flatMap { fullReturn =>

          val propertyType = fullReturn.land.flatMap(_.headOption).flatMap(
            land =>
              LandTypeOfProperty.enumerable.withName(land.propertyType.getOrElse(""))
          )

          propertyType match {

            case Some(LandTypeOfProperty.Residential | LandTypeOfProperty.Additional) =>

              val preparedForm =
                request.userAnswers.get(NonUkResidentPurchaserPage)
                  .fold(form)(form.fill)

              Future.successful(Ok(view(preparedForm, mode)))

            case Some(_) =>
              Future.successful(Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode)))

            case None =>
              Future.successful(Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode)))
          }
        }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val individualOrCompany: String = request.userAnswers.get(WhoIsMakingThePurchasePage) match {
        case Some(value) => if (value.toString == "Individual") "Individual" else "Company"
        case _ => ""
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NonUkResidentPurchaserPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield value match {
            case true if individualOrCompany == "Company" => Redirect(navigator.nextPage(NonUkResidentPurchaserPage, mode, updatedAnswers))
            case true if individualOrCompany == "Individual" => Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) //TODO - DTR-3002 - Sprint 10 - Connect to CrownEmploymentRelief page
            case _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO - DTR-2511 - SPRINT-12 - change to residency CYA page
          }
      )
  }
}
