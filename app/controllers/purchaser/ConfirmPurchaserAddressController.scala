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
import forms.purchaser.ConfirmPurchaserAddressFormProvider
import models.address.Address
import models.{Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.purchaser.{ConfirmPurchaserAddressPage, NameOfPurchaserPage, PurchaserAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.land.LandService
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.ConfirmPurchaserAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmPurchaserAddressController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: ConfirmPurchaserAddressFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: ConfirmPurchaserAddressView,
                                                   purchaserService: PurchaserService,
                                                   landService: LandService
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ConfirmPurchaserAddressPage).fold(form)(form.fill)
      val returnInfo = request.userAnswers.fullReturn.flatMap(_.returnInfo)

      request.userAnswers.get(NameOfPurchaserPage) match {
        case Some(purchaserName) =>
          returnInfo match {
            case Some(_) =>
              generateAddress(request.userAnswers) match {
                case Some(address) =>
                  Ok(view(preparedForm, mode, purchaserName.fullName, Some(address.line1), address.line2, address.line3, address.line4, address.postcode))
                case _ =>
                  Redirect(controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser())
              }
            case _ =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val returnInfo = request.userAnswers.fullReturn.flatMap(_.returnInfo)

      request.userAnswers.get(NameOfPurchaserPage) match {
        case Some(purchaserName) =>
          returnInfo match {
            case Some(_) =>
              generateAddress(request.userAnswers) match {
                case Some(address) =>
                  form.bindFromRequest().fold(
                    formWithErrors =>
                      Future.successful(BadRequest(view(formWithErrors, mode, purchaserName.fullName, Some(address.line1),
                        address.line2, address.line3, address.line4, address.postcode))),
                    value =>
                      for {
                        updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmPurchaserAddressPage, value))
                        _ <- sessionRepository.set(updatedAnswers)
                      } yield {
                        val updatedAnswersWithPurchaserAddress = updatedAnswers.set(PurchaserAddressPage, address).get
                        if (value) {
                          sessionRepository.set(updatedAnswersWithPurchaserAddress)
                          Redirect(navigator.nextPage(ConfirmPurchaserAddressPage, mode, updatedAnswersWithPurchaserAddress))
                        } else {
                          sessionRepository.set(updatedAnswersWithPurchaserAddress)
                          Redirect(controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser())
                        }
                      }
                  )
                case _ =>
                  Future.successful(Redirect(controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser()))
              }
            case _ =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))
      }
  }

  private def generateAddress(userAnswers: UserAnswers): Option[Address] =
    (purchaserService.getMainPurchaser(userAnswers), landService.getMainLand(userAnswers)) match {
      case (Some(p), Some(l)) if p.address1.isEmpty
        && (p.surname.isDefined || p.companyName.isDefined) && l.address1.isDefined =>
        Some(Address(l.address1.get, l.address2, l.address3, l.address4, postcode = l.postcode))
      case (Some(p), _) if p.address1.isDefined =>
        Some(Address(p.address1.get, p.address2, p.address3, p.address4, postcode = p.postcode))
      case (_, Some(l)) if l.address1.isDefined =>
        Some(Address(l.address1.get, l.address2, l.address3, l.address4, postcode = l.postcode))
      case _ =>
        None
    }
}