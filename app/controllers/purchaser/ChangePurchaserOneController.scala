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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.ChangePurchaserOneFormProvider
import models.{Mode, Purchaser}
import navigation.Navigator
import pages.purchaser.ChangePurchaserOnePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.ChangePurchaserOneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangePurchaserOneController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: ChangePurchaserOneFormProvider,
                                       purchaserService: PurchaserService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ChangePurchaserOneView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val (maybePurchaserOne, otherPurchasers, otherPurchasersWithNames) = purchaserService.splitPurchasers(request.userAnswers)
      maybePurchaserOne match {

        case Some(purchaserOne) =>
          val preparedForm = request.userAnswers.get(ChangePurchaserOnePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          val purchaserOneName = purchaserService.mainPurchaserName(request.userAnswers).map(_.fullName).getOrElse("")

          Ok(view(preparedForm, mode, purchaserOne, purchaserOneName, otherPurchasersWithNames))

        case None => Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val (maybePurchaserOne, otherPurchasers, otherPurchasersWithNames) = purchaserService.splitPurchasers(request.userAnswers)
      val purchaserOneName = purchaserService.mainPurchaserName(request.userAnswers).map(_.fullName).getOrElse("")
      maybePurchaserOne match {
        case Some(purchaserOne) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserOne, purchaserOneName, otherPurchasersWithNames))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ChangePurchaserOnePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ChangePurchaserOnePage, mode, updatedAnswers))
          )

        case None => Future.successful(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad()))
      }
  }
}
