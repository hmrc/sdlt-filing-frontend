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

package controllers.purchaserAgent

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.{Mode, NormalMode}
import models.address.AddressLookupJourneyIdentifier.purchaserAgentQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import pages.purchaserAgent.{PurchaserAgentAddressPage, PurchaserAgentNamePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class PurchaserAgentAddressController @Inject()(
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 addressLookupService: AddressLookupService,
                                                 sessionRepository: SessionRepository
                                               )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def redirectToAddressLookupPurchaserAgent(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId = purchaserAgentQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>
          val purchaserAgentName = userAnswers.get(PurchaserAgentNamePage)

          purchaserAgentName match {
            case Some(name) =>
              val callback = if (changeRoute.isDefined) {
                controllers.purchaserAgent.routes.PurchaserAgentAddressController.addressLookupCallbackChangePurchaserAgent()
              } else {
                controllers.purchaserAgent.routes.PurchaserAgentAddressController.addressLookupCallbackPurchaserAgent()
              }

              addressLookupService.getJourneyUrl(
                journeyId,
                callback,
                useUkMode = true,
                mandatoryFieldsConfigModel = addressConfig,
                optName = Some(name)
              ).map(Redirect)
              
            case None =>
              Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(mode)))
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }


  def addressLookupCallbackPurchaserAgent(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PurchaserAgentAddressPage)
      } yield if(updated) {
        // TODO: change this when we have the next page (DTR-1820 pa-2a)
        Redirect(controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode))
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def addressLookupCallbackChangePurchaserAgent(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PurchaserAgentAddressPage)
      } yield if(updated) {
        //TODO DTR-1851: change this when we have the check your answers page
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }
}