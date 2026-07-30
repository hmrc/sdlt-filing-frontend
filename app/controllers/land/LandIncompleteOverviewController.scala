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

package controllers.land

import controllers.actions.*
import models.{Land}
import pages.land.LandOverviewRemovePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.land.PopulateLandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.LandTaskList
import views.html.land.LandIncompleteOverviewView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandIncompleteOverviewController @Inject() (
                                                   override val messagesApi:  MessagesApi,
                                                   identify:                  IdentifierAction,
                                                   getData:                   DataRetrievalAction,
                                                   requireData:               DataRequiredAction,
                                                   statusCheck:               CheckSubmissionStatusAction,
                                                   sessionRepository:         SessionRepository,
                                                   populateLandService:       PopulateLandService,
                                                   val controllerComponents:  MessagesControllerComponents,
                                                   view:                      LandIncompleteOverviewView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val incompleteLands: Seq[Land] = request.userAnswers.fullReturn
        .map(fullReturn => LandTaskList.incompleteLands(fullReturn))
        .getOrElse(Seq.empty)

      if (incompleteLands.isEmpty) {
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))
      } else {
        Future.successful(Ok(view(
          lands       = incompleteLands,
          continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url
        )))
      }
    }

  def updateLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val maybeLand = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.find(_.landID.contains(landId)))

      maybeLand match {
        case Some(land) =>
          for {
            updatedAnswers <- Future.fromTry(populateLandService.populateLandInSession(land, request.userAnswers))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removeLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(LandOverviewRemovePage, landId))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.land.routes.RemoveLandController.onPageLoad())
    }
}