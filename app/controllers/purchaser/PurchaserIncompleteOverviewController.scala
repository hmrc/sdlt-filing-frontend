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
import models.Purchaser
import models.purchaser.PurchaserAndCompanyId
import pages.purchaser.PurchaserOverviewRemovePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PopulatePurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.PurchaserTaskList
import views.html.purchaser.PurchaserIncompleteOverviewView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaserIncompleteOverviewController @Inject() (
                                                        override val messagesApi:  MessagesApi,
                                                        identify:                  IdentifierAction,
                                                        getData:                   DataRetrievalAction,
                                                        requireData:               DataRequiredAction,
                                                        statusCheck:               CheckSubmissionStatusAction,
                                                        sessionRepository:         SessionRepository,
                                                        populatePurchaserService:  PopulatePurchaserService,
                                                        val controllerComponents:  MessagesControllerComponents,
                                                        view:                      PurchaserIncompleteOverviewView
                                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val incompletePurchasers: Seq[Purchaser] = request.userAnswers.fullReturn
        .map(fullReturn => PurchaserTaskList.incompletePurchasers(fullReturn))
        .getOrElse(Seq.empty)

      if (incompletePurchasers.isEmpty) {
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))
      } else {
        Future.successful(Ok(view(
          purchasers  = incompletePurchasers,
          continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url
        )))
      }
    }

  def updatePurchaser(purchaserId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val maybePurchaser = request.userAnswers.fullReturn
        .flatMap(_.purchaser)
        .flatMap(_.find(_.purchaserID.contains(purchaserId)))

      maybePurchaser match {
        case Some(purchaser) =>
          for {
            updatedAnswers <- Future.fromTry(populatePurchaserService.populatePurchaserInSession(purchaser, purchaserId, request.userAnswers))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removePurchaser(purchaserId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val companyDetailsID: Option[String] =
        request.userAnswers.fullReturn.flatMap(_.companyDetails.map(_.companyDetailsID)).flatten

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserId, companyDetailsID)))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.purchaser.routes.PurchaserRemoveController.onPageLoad())
    }
}