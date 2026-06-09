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
import forms.purchaser.PurchaserRemoveFormProvider
import models.Mode
import pages.purchaser.NameOfPurchaserPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.purchaser.PurchaserRemoveService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaserRemoveController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PurchaserRemoveFormProvider,
                                       purchaserRemoveService: PurchaserRemoveService,
                                       val controllerComponents: MessagesControllerComponents
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode))

        case Some(purchaser) =>
          val purchaserName = purchaser.fullName
          val form = formProvider(purchaserName)

          purchaserRemoveService.purchaserRemoveView(form, mode) match {
            case Right(html) => Ok(html)
            case Left(result) => result
          }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(
            Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode))
          )

        case Some(purchaser) =>
          val purchaserName = purchaser.fullName
          val form = formProvider(purchaserName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                purchaserRemoveService.purchaserRemoveView(formWithErrors, mode).fold(identity, BadRequest(_))
              ),
            value =>
              purchaserRemoveService.handleRemoval(value, userAnswers = request.userAnswers)
          )
      }
  }
}