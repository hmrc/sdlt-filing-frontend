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

package controllers

import config.FrontendAppConfig
import controllers.actions.*
import forms.DeleteReturnFormProvider
import models.{DeleteReturnRequest, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.land.LandService
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FullName
import views.html.DeleteReturnView
import connectors.StampDutyLandTaxConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteReturnController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: DeleteReturnFormProvider,
                                        stampDutyLandTaxConnector: StampDutyLandTaxConnector,
                                        purchaserService: PurchaserService,
                                        landService: LandService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: DeleteReturnView
                                      )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(form, purchaserName(request.userAnswers), landAddress1(request.userAnswers)))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, purchaserName(request.userAnswers), landAddress1(request.userAnswers)))),
        {
          case true =>
            request.userAnswers.returnId match {
              case Some(id) =>
                stampDutyLandTaxConnector
                  .deleteReturn(DeleteReturnRequest(storn = request.userAnswers.storn, returnResourceRef = id))
                  .map(_ => Redirect(appConfig.sdltManagementRedirectUrl))
              case None =>
                Future.successful(Redirect(controllers.routes.NoReturnReferenceController.onPageLoad()))
            }
          case false =>
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
        }
      )
  }

  private def purchaserName(userAnswers: UserAnswers): Option[String] =
    purchaserService.getMainPurchaser(userAnswers).flatMap { purchaser =>
      purchaser.companyName.orElse(
        FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
      )
    }

  private def landAddress1(userAnswers: UserAnswers): Option[String] =
    landService.getMainLand(userAnswers).flatMap(_.address1)
}