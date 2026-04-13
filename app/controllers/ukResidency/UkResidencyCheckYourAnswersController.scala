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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import models.{ReturnVersionUpdateRequest, UserAnswers}
import models.land.LandTypeOfProperty
import models.ukResidency.{CreateResidencyRequest, UpdateResidencyRequest}
import pages.ukResidency.{CloseCompanyPage, CrownEmploymentReliefPage, NonUkResidentPurchaserPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.land.LandService
import services.purchaser.PurchaserService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukResidency.{CloseCompanySummary, CrownEmploymentReliefSummary, NonUkResidentPurchaserSummary}
import viewmodels.govuk.summarylist.*
import views.html.ukResidency.UkResidencyCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UkResidencyCheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  backendConnector: StampDutyLandTaxConnector,
  landService: LandService,
  purchaserService: PurchaserService,
  val controllerComponents: MessagesControllerComponents,
  view: UkResidencyCheckYourAnswersView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val propertyType = landService.getMainLand(request.userAnswers)
        .flatMap(_.propertyType)
        .flatMap(LandTypeOfProperty.enumerable.withName)

      propertyType match {
        case Some(LandTypeOfProperty.Residential | LandTypeOfProperty.Additional) =>
          sessionRepository.get(request.userAnswers.id).flatMap(handleSessionResult)
        case _ =>
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  private def handleSessionResult(result: Option[UserAnswers])(implicit request: Request[_]): Future[Result] =
    result match {
      case Some(userAnswers) if userAnswers.returnId.isEmpty   => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Some(userAnswers) if userAnswers.data.value.isEmpty => populateFromResidency(userAnswers)
      case Some(userAnswers)                                   => Future.successful(Ok(view(buildSummaryList(userAnswers))))
      case None                                                => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
    }

  private def populateFromResidency(userAnswers: UserAnswers)(implicit request: Request[_]): Future[Result] =
    userAnswers.fullReturn.flatMap(_.residency) match {
      case None =>
        Future.successful(Redirect(controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad()))
      case Some(residency) =>
        val isCompany = purchaserService.getMainPurchaser(userAnswers).flatMap(_.isCompany).exists(_.equalsIgnoreCase("YES"))
        (for {
          ua  <- userAnswers.set(NonUkResidentPurchaserPage, residency.isNonUkResidents.exists(_.equalsIgnoreCase("YES")))
          ua2 <- if (isCompany) ua.set(CloseCompanyPage, residency.isCloseCompany.exists(_.equalsIgnoreCase("YES"))) else Success(ua)
          ua3 <- ua2.set(CrownEmploymentReliefPage, residency.isCrownRelief.exists(_.equalsIgnoreCase("YES")))
        } yield ua3) match {
          case Success(populated) =>
            sessionRepository.set(populated).map(_ => Ok(view(buildSummaryList(populated))))
          case Failure(_) =>
            Future.successful(Redirect(controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad()))
        }
    }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) if userAnswers.returnId.isDefined =>
          userAnswers.get(NonUkResidentPurchaserPage) match {
            case Some(_) =>
              val hasResidencyId = userAnswers.fullReturn.flatMap(_.residency).flatMap(_.residencyID).isDefined
              if (hasResidencyId) updateResidency(userAnswers)
              else createResidency(userAnswers)
            case None =>
              Future.successful(Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad()))
          }
        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  private def createResidency(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      createResidencyRequest <- CreateResidencyRequest.from(userAnswers)
      createResidencyReturn  <- backendConnector.createResidency(createResidencyRequest)
    } yield {
      if (createResidencyReturn.residencyId.nonEmpty) {
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def updateResidency(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      updateReturnVersionReturn  <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
      updateResidencyRequest     <- UpdateResidencyRequest.from(userAnswers) if updateReturnVersionReturn.newVersion.isDefined
      updateResidencyReturn      <- backendConnector.updateResidency(updateResidencyRequest) if updateReturnVersionReturn.newVersion.isDefined
    } yield {
      if (updateResidencyReturn.updated) {
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def buildSummaryList(userAnswers: UserAnswers)(implicit request: RequestHeader) =
    SummaryListViewModel(
      rows = Seq(
        NonUkResidentPurchaserSummary.row(userAnswers),
        CloseCompanySummary.row(userAnswers),
        CrownEmploymentReliefSummary.row(userAnswers)
      ).flatten
    )
}
