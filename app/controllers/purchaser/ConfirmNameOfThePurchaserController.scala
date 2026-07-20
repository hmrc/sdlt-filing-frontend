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
import forms.purchaser.ConfirmNameOfThePurchaserFormProvider
import models.*
import navigation.Navigator
import pages.purchaser.ConfirmNameOfThePurchaserPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.ConfirmNameOfThePurchaserView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ConfirmNameOfThePurchaserController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     navigator: Navigator,
                                                     requireData: DataRequiredAction,
                                                     statusCheck: CheckSubmissionStatusAction,
                                                     formProvider: ConfirmNameOfThePurchaserFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     purchaserService: PurchaserService,
                                                     view: ConfirmNameOfThePurchaserView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      val mainPurchaserOpt: Option[Purchaser] = purchaserService.getMainPurchaser(request.userAnswers)

      mainPurchaserOpt match {
        case Some(purchaser) if purchaser.address1.isEmpty
          && (purchaser.surname.isDefined || purchaser.companyName.isDefined) =>
          val isCompany = purchaser.companyName.isDefined
          val name = purchaser.companyName.orElse(purchaser.surname).getOrElse("")
          val form = formProvider(name, isCompany)
          val preparedForm = request.userAnswers.get(ConfirmNameOfThePurchaserPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, mode, name, isCompany)))

        case _ =>
          Future.successful(Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      val mainPurchaserOpt: Option[Purchaser] = purchaserService.getMainPurchaser(request.userAnswers)

      mainPurchaserOpt match {
        case Some(purchaser) if purchaser.address1.isEmpty
          && (purchaser.surname.isDefined || purchaser.companyName.isDefined) =>
          val name = purchaser.companyName.orElse(purchaser.surname).getOrElse("")
          val isCompany = purchaser.companyName.isDefined
          val form = formProvider(name, isCompany)


          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, name, isCompany))),
            value =>
              purchaserService.populatePurchaserNameInSession(
                purchaserCheck = value,
                userAnswers = request.userAnswers
              ) match {
                case Success(updatedAnswers) =>
                  sessionRepository.set(updatedAnswers).map { _ =>
                    if (value) {
                      Redirect(navigator.nextPage(ConfirmNameOfThePurchaserPage, mode, updatedAnswers))
                    } else {
                      Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode))
                    }
                  }
                case Failure(exception) =>
                  Future.successful(InternalServerError("Failed to update session"))
              }
          )

        case _ =>
          Future.successful(Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)))
      }
  }
}
