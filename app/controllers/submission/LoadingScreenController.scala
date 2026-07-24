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

import com.google.inject.Inject
import connectors.StampDutyLandTaxConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, ResubmissionCheckAction}
import models.GetReturnByRefRequest
import pages.submission.SubmissionFailedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.submission.LoadingScreenView

import scala.concurrent.{ExecutionContext, Future}

class LoadingScreenController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         resubmissionCheck: ResubmissionCheckAction,
                                         connector: StampDutyLandTaxConnector,
                                         view: LoadingScreenView,
                                         val controllerComponents: MessagesControllerComponents
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val SucceededStatuses = Set("SUBMITTED", "SUBMITTED_NO_RECEIPT")
  private val FailedStatuses    = Set("DEPARTMENTAL_ERROR", "FATAL_ERROR")
  private val Acknowledged      = Set("ACCEPTED")
  private val Resubmitable      = Set("STARTED")

  private val TerminalStatuses: Set[String] =
    SucceededStatuses ++ Acknowledged ++ Resubmitable ++ FailedStatuses

  private def isInProgress(status: Option[String]): Boolean =
    !status.exists(TerminalStatuses.contains)

  def show: Action[AnyContent] = (identify andThen getData andThen requireData andThen resubmissionCheck).async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      if (request.userAnswers.get(SubmissionFailedPage).contains(true)) {
        //TODO Change this to resubmit
        Future.successful(Redirect(controllers.submission.routes.SubmissionFailedController.onPageLoad()))
      } else {
        latestStatus(request.userAnswers.storn, request.userAnswers.returnId).map {
          case Some(s) if SucceededStatuses.contains(s) =>
            Redirect(controllers.submission.routes.SubmissionCompleteController.onPageLoad())
          case Some(s) if Acknowledged.contains(s) =>
            Redirect(controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad())
          case Some(s) if Resubmitable.contains(s) =>
            Redirect(controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad()) // TODO confirm page
          case Some(s) if FailedStatuses.contains(s) =>
            Redirect(controllers.submission.routes.SubmissionFailedController.onPageLoad())
          case _ =>
            Ok(view(controllers.submission.routes.LoadingScreenController.query))
        }
      }
  }

  def query: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      if (request.userAnswers.get(SubmissionFailedPage).contains(true)) {
        Future.successful(Ok)
      } else {
        latestStatus(request.userAnswers.storn, request.userAnswers.returnId).map { status =>
          if (isInProgress(status)) NoContent else Ok
        }
      }
  }

  private def latestStatus(storn: String, returnId: Option[String])
                          (implicit hc: HeaderCarrier, request: Request[_]): Future[Option[String]] =
    returnId match {
      case Some(ref) =>
        connector.getFullReturn(GetReturnByRefRequest(ref, storn))
          .map(_.submission.flatMap(_.submissionStatus).map(_.toUpperCase))
          .recover { case _ => None }
      case None =>
        Future.successful(None)
    }
}