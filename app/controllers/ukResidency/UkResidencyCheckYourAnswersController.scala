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

package controllers.ukResidency

import controllers.actions.*
import models.UserAnswers
import models.land.LandTypeOfProperty
import pages.ukResidency.NonUkResidentPurchaserPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukResidency.{CloseCompanySummary, CrownEmploymentReliefSummary, NonUkResidentPurchaserSummary}
import viewmodels.govuk.summarylist.*
import views.html.ukResidency.UkResidencyCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkResidencyCheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: UkResidencyCheckYourAnswersView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val propertyType = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.headOption)
        .flatMap(_.propertyType)
        .flatMap(LandTypeOfProperty.enumerable.withName)

      propertyType match {
        case Some(LandTypeOfProperty.Residential | LandTypeOfProperty.Additional) =>
          for {
            result <- sessionRepository.get(request.userAnswers.id)
          } yield {
            val isDataEmpty = result.exists(_.data.value.isEmpty)
            if (isDataEmpty) {
              Redirect(controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad())
            } else {
              Ok(view(buildSummaryList(request.userAnswers)))
            }
          }
        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
  }

  private def buildSummaryList(userAnswers: UserAnswers)(implicit request: RequestHeader) = {
    val isNonUkResident = userAnswers.get(NonUkResidentPurchaserPage).contains(true)

    SummaryListViewModel(
      rows = Seq(
        NonUkResidentPurchaserSummary.row(userAnswers),
        CloseCompanySummary.row(userAnswers),
        Option.when(isNonUkResident)(CrownEmploymentReliefSummary.row(userAnswers))
      ).flatten
    )
  }
}
