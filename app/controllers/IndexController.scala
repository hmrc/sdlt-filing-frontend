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
import connectors.RateLimitedAllowListConnector
import controllers.actions.IdentifierAction
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject() (
                                  val controllerComponents: MessagesControllerComponents,
                                  identify: IdentifierAction,
                                  sessionRepository: SessionRepository,
                                  config: FrontendAppConfig,
                                  rateLimitedAllowListConnector: RateLimitedAllowListConnector
                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(returnId: Option[String] = None): Action[AnyContent] = identify.async { implicit request =>
    checkAllowList(config.useRateLimitedAllowList, config.splitterAllowListName, request.storn)
      .flatMap {
        case false =>
          Future.successful(Results.Redirect(config.legacySdltServiceUrl(request)))

        case true =>
          returnId match
            case Some(id) => {
              val userAnswers = UserAnswers(id = request.userId, returnId = Some(id), storn = request.storn)
              sessionRepository.set(userAnswers).map { _ =>
                Results.Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
              }
            }
            case _ =>  {
              val userAnswers = UserAnswers(id = request.userId, returnId = None, storn = request.storn)
                sessionRepository.set(userAnswers).map { _ =>
                Results.Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
              }
            }
      }
  }

  private def checkAllowList(useRateLimitedAllowList: Boolean, allowListName: String, storn: String)(
    implicit hc: HeaderCarrier
  ): Future[Boolean] =
    if (useRateLimitedAllowList) {
      rateLimitedAllowListConnector.checkAllowList(allowListName, storn)
    } else {
      Future.successful(true)
    }
}
