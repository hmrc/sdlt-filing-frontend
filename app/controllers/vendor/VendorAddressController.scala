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

package controllers.vendor

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.address.AddressLookupJourneyIdentifier.vendorQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import models.{Mode, NormalMode, Vendor}
import pages.vendor.VendorAddressPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.libs.json.{JsError, JsSuccess}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAddressController @Inject()(
                                  val controllerComponents: MessagesControllerComponents,
                                  identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  requireData: DataRequiredAction,
                                  addressLookupService: AddressLookupService,
                                  sessionRepository: SessionRepository
                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def redirectToAddressLookupVendor(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId = vendorQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>
          val vendorName = (userAnswers.data \ "vendorCurrent" \ "vendorOrCompanyName" \ "name").asOpt[String]
          vendorName match {
            case Some(name) =>
              val callback = if (changeRoute.isDefined) {
                controllers.vendor.routes.VendorAddressController.addressLookupCallbackChangeVendor()
              } else {
                controllers.vendor.routes.VendorAddressController.addressLookupCallbackVendor()
              }

              addressLookupService.getJourneyUrl(
                journeyId,
                callback,
                useUkMode = true,
                mandatoryFieldsConfigModel = addressConfig,
                optName = Some(name)
              ).map(Redirect)

            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }


  def addressLookupCallbackVendor(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
        for {
          address <- addressLookupService.getAddressById(id)
          updated <- addressLookupService.saveAddressDetails(address, VendorAddressPage)
        } yield if(updated) {
          Redirect(controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode))
        } else {
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def addressLookupCallbackChangeVendor(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, VendorAddressPage)
      } yield if(updated) {
        //change this when we have the check your answers page
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
