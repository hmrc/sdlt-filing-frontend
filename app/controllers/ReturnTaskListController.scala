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

package controllers

import config.FrontendAppConfig
import controllers.actions.*

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Results}
import services.FullReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.{PrelimTaskList, VendorTaskList}
import views.html.ReturnTaskListView
import models.{NormalMode, UserAnswers}
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class ReturnTaskListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       fullReturnService: FullReturnService,
                                       getData: DataRetrievalAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnTaskListView,
                                       sessionRepository: SessionRepository
                                     ) (implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(returnId: Option[String] = None): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
 
      val effectiveReturnId = returnId.orElse(request.userAnswers.flatMap(_.returnId))

      effectiveReturnId.fold(
        Future.successful(Redirect(controllers.routes.BeforeStartReturnController.onPageLoad()))
      ) { id =>
        for {
          fullReturn <- fullReturnService.getFullReturn(Some(id))
          userAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn))
          _ <- sessionRepository.set(userAnswers)
        } yield {
          val sections = List(
            Some(PrelimTaskList.build(fullReturn)),
            Some(VendorTaskList.build(fullReturn))
          ).flatten
          Ok(view(sections: _*))
        }
      }
  }
}