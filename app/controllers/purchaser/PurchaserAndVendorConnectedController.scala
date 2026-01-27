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
import forms.purchaser.PurchaserAndVendorConnectedFormProvider
import models.purchaser.PurchaserAndVendorConnected
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{NameOfPurchaserPage, PurchaserAndVendorConnectedPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserAndVendorConnectedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserAndVendorConnectedController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PurchaserAndVendorConnectedFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       purchaserService: PurchaserService,
                                       view: PurchaserAndVendorConnectedView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[PurchaserAndVendorConnected] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val purchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      purchaserName match {

        case Some(purchaserName) =>
          val preparedForm = request.userAnswers.get(PurchaserAndVendorConnectedPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val continueRoute = Ok(view(preparedForm, mode, purchaserName))
          purchaserService.continueIfAddingMainPurchaser(request.userAnswers, continueRoute, mode)

        case None => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val purchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      purchaserName match {
        case Some(purchaserName) =>

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),
            value =>

              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserAndVendorConnectedPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PurchaserAndVendorConnectedPage, mode, updatedAnswers))
          )
        case None => Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))
      }
  }
}