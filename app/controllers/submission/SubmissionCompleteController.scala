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

package controllers.submission

import config.CurrencyFormatter.BigDecimalToCurrency
import controllers.actions.*
import models.{GetReturnByRefRequest, UserAnswers}
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.FullReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.dateTimeFormat
import views.html.submission.SubmissionCompleteView

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionCompleteController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       fullReturnService:        FullReturnService,
                                       sessionRepository: SessionRepository,
                                       view: SubmissionCompleteView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val effectiveReturnId = request.userAnswers.returnId

      effectiveReturnId.fold(
        Future.successful(Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()))
      ) { id =>
        fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = request.userAnswers.storn))
          .flatMap { fullReturn =>
            val userAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn), storn = request.userAnswers.storn)
            sessionRepository.set(userAnswers).map { _ =>
              val utrn = fullReturn.submission.flatMap(_.UTRN)
              val submissionRequestDate = fullReturn.submission.flatMap(_.submissionRequestDate)
              val maybeEmail = fullReturn.submission.flatMap(_.email)
              val totalTaxDue: Option[String] =
                List(
                  fullReturn.taxCalculation.flatMap(_.taxDue),
                  fullReturn.taxCalculation.flatMap(_.taxDuePremium),
                  fullReturn.taxCalculation.flatMap(_.taxDueNPV)
                )
                  .flatten
                  .map(s => BigDecimal(s.replace(",", "")))
                  .reduceOption(_ + _)
                  .map(_.toCurrency)

              (utrn, submissionRequestDate, totalTaxDue, maybeEmail) match {
                case (Some(utrn), Some(submissionRequestDate), Some(totalTaxDue), maybeEmail) =>

                  implicit val messages: Messages = messagesApi.preferred(request)
                  implicit val lang: Lang = messages.lang
                  val deadline = ZonedDateTime.parse(
                    submissionRequestDate,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                  ).plusDays(14).format(dateTimeFormat())

                  Ok(view(utrn, deadline, totalTaxDue, maybeEmail))
                case (_, _, _, _) =>
                  Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
              }
            }
          }
      }
  }
}
