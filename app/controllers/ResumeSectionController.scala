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

import controllers.actions.*
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.transaction.PopulateTransactionService
import services.lease.PopulateLeaseService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ResumeSectionController @Inject() (
                                          override val messagesApi:  MessagesApi,
                                          identify:                  IdentifierAction,
                                          getData:                   DataRetrievalAction,
                                          requireData:               DataRequiredAction,
                                          statusCheck:               CheckSubmissionStatusAction,
                                          sessionRepository:         SessionRepository,
                                          populateTransactionService:PopulateTransactionService,
                                          populateLeaseService: PopulateLeaseService,
                                          val controllerComponents:  MessagesControllerComponents
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def resume(section: String, id: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val userAnswers = request.userAnswers
      val fullReturn  = userAnswers.fullReturn

      val handled: Option[(Try[UserAnswers], Call)] = section.toLowerCase match {

        case "transaction" =>
          fullReturn.flatMap(_.transaction).map(transaction =>
            populateTransactionService.populateTransactionInSession(transaction, userAnswers) ->
              controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad())

        case "lease" =>
          fullReturn.flatMap(_.lease).map(lease =>
            populateLeaseService.populateLeaseInSession(lease, userAnswers) ->
              controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
          
        case _ => None
      }

      handled match {
        case Some((tryAnswers, redirect)) =>
          for {
            updatedAnswers <- Future.fromTry(tryAnswers)
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(redirect)

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}