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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import models.land.{CreateLandRequest, LandSessionQuestions, LandTypeOfProperty, UpdateLandRequest}
import models.{Land, ReturnVersionUpdateRequest, UserAnswers}
import pages.land.{AgriculturalOrDevelopmentalLandPage, DoYouKnowTheAreaOfLandPage, LandAddNlpgUprnPage, LandOverviewPage, LandRegisteredHmRegistryPage,
  LandTypeOfPropertyPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.land.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.land.LandCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LandCheckYourAnswersController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction,
                                                          sessionRepository: SessionRepository,
                                                          backendConnector: StampDutyLandTaxConnector,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: LandCheckYourAnswersView
                                                        )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isDataEmpty = result.exists(_.data.value.isEmpty)

        if (isDataEmpty) {
          Redirect(controllers.land.routes.LandBeforeYouStartController.onPageLoad())
        } else {
          val summaryList = SummaryListViewModel(
            rows = Seq(
              Some(LandTypeOfPropertySummary.row(request.userAnswers)),
              Some(LandInterestTransferredOrCreatedSummary.row(request.userAnswers)),
              Some(LandAddressSummary.row(request.userAnswers)),
              Some(LocalAuthorityCodeSummary.row(request.userAnswers)),
              Some(LandRegisteredHmRegistrySummary.row(request.userAnswers)),
              if (titleCheck(request.userAnswers)) LandTitleNumberSummary.row(request.userAnswers) else None,
              Some(LandAddNlpgUprnSummary.row(request.userAnswers)),
              if (nlpgCheck(request.userAnswers)) LandNlpgUprnSummary.row(request.userAnswers) else None,
              Some(LandSendingPlanByPostSummary.row(request.userAnswers)),
              Some(LandMineralsOrMineralRightsSummary.row(request.userAnswers)),
              if (propertyTypeCheck(request.userAnswers)) AgriculturalOrDevelopmentalLandSummary.row(request.userAnswers) else None,
              if (propertyTypeCheck(request.userAnswers) && agriculturalCheck(request.userAnswers)) DoYouKnowTheAreaOfLandSummary.row(request.userAnswers) else None,
              if (propertyTypeCheck(request.userAnswers) && knowAreaCheck(request.userAnswers) && agriculturalCheck(request.userAnswers)) {AreaOfLandSummary.row(request.userAnswers)} else None
            ).flatten
          )

          Ok(view(summaryList))
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    sessionRepository.get(request.userAnswers.id).flatMap {
      case Some(userAnswers) =>
        (userAnswers.data \ "landCurrent").validate[LandSessionQuestions] match {
          case JsSuccess(sessionData, _) =>
            request.userAnswers.get(LandOverviewPage).map { landId =>
              updateLand(userAnswers)
            }.getOrElse(createLand(userAnswers))

          case JsError(_) =>
            Future.successful(
              Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
            )
        }

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def updateLand(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      land <- Land.from(userAnswers)
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      updateReturnVersionReturn <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
      updateLandRequest <- UpdateLandRequest.from(userAnswers, land) if updateReturnVersionReturn.newVersion.isDefined
      updateLandReturn <- backendConnector.updateLand(updateLandRequest) if updateReturnVersionReturn.newVersion.isDefined
    } yield {
      if (updateLandReturn.updated) {
        Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
          .flashing("landUpdated" -> updateLandRequest.addressLine1)
      } else {
        Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def createLand(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      land <- Land.from(userAnswers)
      createLandRequest <- CreateLandRequest.from(userAnswers, land)
      createLandReturn <- backendConnector.createLand(createLandRequest)
    } yield {
      if (createLandReturn.landId.nonEmpty) {
        Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
          .flashing("landCreated" -> createLandRequest.addressLine1)
      } else {
        Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def titleCheck(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(LandRegisteredHmRegistryPage) match {
      case Some(true) => true
      case _ => false
    }
  }

  private def nlpgCheck(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(LandAddNlpgUprnPage) match {
      case Some(true) => true
      case _ => false
    }
  }

  private def agriculturalCheck(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(AgriculturalOrDevelopmentalLandPage) match {
      case Some(true) => true
      case _ => false
    }
  }

  private def knowAreaCheck(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(DoYouKnowTheAreaOfLandPage) match {
      case Some(true) => true
      case _ => false
    }
  }

  private def propertyTypeCheck(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(LandTypeOfPropertyPage) match {
      case Some(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential) => true
      case _ => false
    }
  }
}
