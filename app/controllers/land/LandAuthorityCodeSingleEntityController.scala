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
import models.CheckMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.{CrossFlowFailure, Pages}
import services.crossflow.fields.CrossFlowValidationService
import services.land.PopulateLandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LandAuthorityCodeSingleEntityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandAuthorityCodeSingleEntityController @Inject() (
                                                          override val messagesApi: MessagesApi,
                                                          identify:                 IdentifierAction,
                                                          getData:                  DataRetrievalAction,
                                                          requireData:              DataRequiredAction,
                                                          sessionRepository:        SessionRepository,
                                                          populateLandService:      PopulateLandService,
                                                          crossFlow:                CrossFlowValidationService,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view:                     LandAuthorityCodeSingleEntityView
                                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(landId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeLand = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.find(_.landID.contains(landId)))

      val maybeFailure = maybeLand.flatMap { land =>
        crossFlow.landFailuresGrouped(request.userAnswers)
          .find(_._1.landID == land.landID)
          .flatMap(_._2.headOption)
      }

      (maybeLand, maybeFailure) match {

        case (Some(land), Some(failure)) =>

          for {
            updatedAnswers <- Future.fromTry(populateLandService.populateLandInSession(land, request.userAnswers))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val heading     = s"crossflow.land.${failure.ruleId}.heading"
            val bodyKey     = s"${failure.messageKey}"
            val ctaKey      = ctaKeyFor(failure)
            val continueUrl = continueUrlFor(failure)
            Ok(view(heading, bodyKey, ctaKey, continueUrl))
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))
      }
  }

  private def continueUrlFor(failure: CrossFlowFailure): String = {
    val targetsPostcode = failure.targets.exists(_.page == Pages.LandPostcode)
    if (targetsPostcode)
      controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(CheckMode).url
    else
      controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url
  }

  private def ctaKeyFor(failure: CrossFlowFailure): String = {
    val targetsPostcode = failure.targets.exists(_.page == Pages.LandPostcode)
    if (targetsPostcode) "crossflow.land.cta.enterDifferentPostcode"
    else                 "crossflow.land.cta.enterDifferentCode"
  }
}