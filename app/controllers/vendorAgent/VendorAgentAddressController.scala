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

package controllers.vendorAgent

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.{Mode, NormalMode}
import models.address.AddressLookupJourneyIdentifier.vendorAgentQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import pages.vendorAgent.{AgentNamePage, VendorAgentAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentAddressController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              addressLookupService: AddressLookupService,
                                              sessionRepository: SessionRepository
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def redirectToAddressLookupVendorAgent(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId = vendorAgentQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>
          val vendorAgentName = userAnswers.get(AgentNamePage)

          vendorAgentName match {
            case Some(name) =>
              val callback = if (changeRoute.isDefined) {
                controllers.vendorAgent.routes.VendorAgentAddressController.addressLookupCallbackChangeVendorAgent()
              } else {
                controllers.vendorAgent.routes.VendorAgentAddressController.addressLookupCallbackVendorAgent()
              }

              addressLookupService.getJourneyUrl(
                journeyId,
                callback,
                useUkMode = true,
                mandatoryFieldsConfigModel = addressConfig,
                optName = Some(name)
              ).map(Redirect)

            case None =>
              Future.successful(Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(mode)))
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def addressLookupCallbackVendorAgent(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, VendorAgentAddressPage)
      } yield if (updated) {
        Redirect(controllers.vendorAgent.routes.AddVendorAgentContactDetailsController.onPageLoad(NormalMode))
      }
      else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def addressLookupCallbackChangeVendorAgent(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, VendorAgentAddressPage)
      } yield if (updated) {
        Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
