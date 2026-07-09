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

import controllers.actions.*
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.submission.SubmissionState
import viewmodels.submission.summary.*
import views.html.submission.CompletedSdltReturnView

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.util.Try

class CompletedSdltReturnController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: CompletedSdltReturnView,
                                       purchaserService: PurchaserService
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.fullReturn.flatMap { fullReturn =>
          fullReturn.submission.flatMap { submission =>
            implicit val messages: Messages = messagesApi.preferred(request)
            implicit val lang: Lang = messages.lang
            SubmissionState.parse(submission.submissionStatus).map { submissionState =>
              val submissionDateTime = submission.submittedDate.flatMap(dateStr =>
                Try {
                  val dateTime = ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                  val submissionDate = dateTime.format(dateTimeFormat())
                  val submissionTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                  (submissionDate, submissionTime)
                }.toOption
              )
              Ok(view(
                submissionState = submissionState,
                submissionDate = submissionDateTime.map(_._1),
                submissionTime = submissionDateTime.map(_._2),
                purchaserName = purchaserService.mainPurchaserName(request.userAnswers).map(_.fullName),
                utrn = submission.UTRN,
                submissionRecieptRef = submission.submissionReceipt,
                summaries = Summaries.from(fullReturn)
              ))
            }
          }
      }.getOrElse(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
  }
}
