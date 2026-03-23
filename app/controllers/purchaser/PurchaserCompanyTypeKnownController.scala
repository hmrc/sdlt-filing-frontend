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
import forms.purchaser.PurchaserCompanyTypeKnownFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.PurchaserTypeOfCompanyPage
import pages.purchaser.{NameOfPurchaserPage, PurchaserCompanyTypeKnownPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserCompanyTypeKnownView

import scala.util.Success
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCompanyTypeKnownController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: PurchaserCompanyTypeKnownFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: PurchaserCompanyTypeKnownView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))
        case Some(purchaser) =>
          val preparedForm = request.userAnswers.get(PurchaserCompanyTypeKnownPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, purchaser.fullName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))
        case Some(purchaser) =>

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaser.fullName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserCompanyTypeKnownPage, value))
                removeCompanyType <- Future.fromTry {
                  if !value then updatedAnswers.remove(PurchaserTypeOfCompanyPage)
                  else Success(updatedAnswers)
                }
                _ <- sessionRepository.set(removeCompanyType)
              } yield (value, mode) match {

                case (true, NormalMode) => Redirect(navigator.nextPage(PurchaserCompanyTypeKnownPage, mode, removeCompanyType))
                case (false, NormalMode) => Redirect(controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(mode))
                case (_,_) => Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())

              }
          )
      }
  }
}
