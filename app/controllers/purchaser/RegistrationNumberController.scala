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
import forms.purchaser.RegistrationNumberFormProvider
import models.purchaser.{NameOfPurchaser, PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, RegistrationNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.RegistrationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: RegistrationNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: RegistrationNumberView,
                                        purchaserService: PurchaserService
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val purchaserFullName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
       val isPurchaserVATRegistered: Option[PurchaserConfirmIdentity] = request.userAnswers.get(PurchaserConfirmIdentityPage)
      
        purchaserService.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
        purchaserType = WhoIsMakingThePurchase.Company,
        userAnswers = request.userAnswers,
        continueRoute = {
          purchaserFullName match {
            case Some(purchaserFullName) if isPurchaserVATRegistered.contains(PurchaserConfirmIdentity.VatRegistrationNumber) =>
              val preparedForm = request.userAnswers.get(RegistrationNumberPage) match {
                case None => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, mode, purchaserFullName))
            case _ => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))
          }
        }
      )
  }
  
  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val purchaserFullName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)

      purchaserFullName match  {
        case Some(purchaserFullName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserFullName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RegistrationNumberPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(RegistrationNumberPage, mode, updatedAnswers))
          )
        case _ =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))
        }
       }
  }

