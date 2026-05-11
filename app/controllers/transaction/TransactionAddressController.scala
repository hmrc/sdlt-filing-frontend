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

package controllers.transaction

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.Mode
import models.address.AddressLookupJourneyIdentifier.transactionQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import pages.transaction.TransactionAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       sessionRepository: SessionRepository,
                                       addressLookupService: AddressLookupService
                                     ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def redirectToAddressLookupTransaction(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId = transactionQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>

          val callback = if (changeRoute.isDefined) {
            controllers.transaction.routes.TransactionAddressController.addressLookupCallbackChangeTransaction()
          } else {
            controllers.transaction.routes.TransactionAddressController.addressLookupCallbackTransaction()
          }

          addressLookupService.getJourneyUrl(
            journeyId,
            callback,
            useUkMode = true,
            mandatoryFieldsConfigModel = addressConfig
          ).map(Redirect)

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }


  def addressLookupCallbackTransaction(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, TransactionAddressPage)
      } yield if (updated) {
        Redirect(controllers.transaction.routes.TransactionExercisingAnOptionController.onPageLoad(mode))
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def addressLookupCallbackChangeTransaction(id: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, TransactionAddressPage)
      } yield if (updated) {
        Redirect(controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad())
      } else {
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
