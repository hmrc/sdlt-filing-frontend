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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.address.AddressLookupJourneyIdentifier.purchaserQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import models.{Mode, NormalMode}
import pages.purchaser.{NameOfPurchaserPage, PurchaserAddressPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserAddressController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            addressLookupService: AddressLookupService,
                                            sessionRepository: SessionRepository
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def redirectToAddressLookupPurchaser(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId = purchaserQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>
          val fullName = userAnswers.get(NameOfPurchaserPage).map(_.fullName)

          fullName match {
            case Some(name) =>
              val callback = if (changeRoute.isDefined) {
                controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackChangePurchaser()
              } else {
                controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackPurchaser()
              }

              addressLookupService.getJourneyUrl(
                journeyId,
                callback,
                useUkMode = true,
                mandatoryFieldsConfigModel = addressConfig,
                optName = Some(name)
              ).map(Redirect)

            case None =>
              Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }


  def addressLookupCallbackPurchaser(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PurchaserAddressPage)
      } yield if (updated) {
        Redirect(controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode))
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def addressLookupCallbackChangePurchaser(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PurchaserAddressPage)
      } yield if (updated) {
        //change this when we have the check your answers page
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
