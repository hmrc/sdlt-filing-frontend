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

package controllers.lease

import controllers.actions.*
import models.{CheckMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import services.crossflow.{CrossFlowFailure, ReturnSection}
import services.crossflow.fields.CrossFlowValidationService
import services.lease.PopulateLeaseService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil
import views.html.lease.LeaseSingleEntityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class LeaseSingleEntityController @Inject() (
                                              override val messagesApi:  MessagesApi,
                                              identify:                  IdentifierAction,
                                              getData:                   DataRetrievalAction,
                                              requireData:               DataRequiredAction,
                                              statusCheck: CheckSubmissionStatusAction,
                                              sessionRepository:         SessionRepository,
                                              populateLeaseService:      PopulateLeaseService,
                                              crossFlow:                 CrossFlowValidationService,
                                              val controllerComponents:  MessagesControllerComponents,
                                              view:                      LeaseSingleEntityView
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with LoggingUtil {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      populateFromLease(request.userAnswers)
  }

  private def populateFromLease(userAnswers: UserAnswers)(implicit request: Request[_]): Future[Result] =
    userAnswers.fullReturn.flatMap(_.lease) match {

      case None =>
        Future.successful(Redirect(
          controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad()
        ))

      case Some(lease) =>
        populateLeaseService.populateLeaseInSession(lease, userAnswers) match {

          case Success(populated) =>
            sessionRepository.set(populated).map(_ => renderOrRedirect(populated))

          case Failure(ex) =>
            logger.error(s"[LeaseSingleEntity] populate failed: ${ex.getMessage}", ex)
            Future.successful(Redirect(controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad()))
        }
    }

  private def renderOrRedirect(userAnswers: UserAnswers)(implicit request: Request[_]): Result =
    crossFlow.failuresAffecting(ReturnSection.Lease, userAnswers).headOption match {

      case Some(failure) =>
        Ok(view(
          headingKey  = failure.headingKey,
          body        = failure.body,
          ctaKey      = ctaKeyFor(failure),
          continueUrl = controllers.lease.routes.TypeOfLeaseController.onPageLoad(CheckMode).url
        ))

      case None =>
        Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
    }
  

  private def ctaKeyFor(failure: CrossFlowFailure): String =
    failure.ruleId match {
      case "Cf-5a" => "crossflow.lease.Cf-5a.cta"
      case "Cf-5b" => "crossflow.lease.Cf-5b.cta"
      case "Cf-5c" => "crossflow.lease.Cf-5c.cta"
      case _       => "crossflow.lease.cta.changeLeaseType"
    }
}