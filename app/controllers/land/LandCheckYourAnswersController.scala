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
import models.{CheckMode, Land, ReturnVersionUpdateRequest, UserAnswers}
import pages.land.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsSuccess}
import play.api.mvc.*
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.crossflow.{CrossFlowFailure, Pages}
import services.crossflow.fields.CrossFlowValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.land.*
import viewmodels.checkAnswers.summary.SummaryRowResult
import views.html.land.LandCheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandCheckYourAnswersController @Inject() (
                                                 override val messagesApi: MessagesApi,
                                                 identify:                 IdentifierAction,
                                                 getData:                  DataRetrievalAction,
                                                 requireData:              DataRequiredAction,
                                                 sessionRepository:        SessionRepository,
                                                 backendConnector:         StampDutyLandTaxConnector,
                                                 checkAnswersService:      CheckAnswersService,
                                                 crossFlow:                CrossFlowValidationService,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view:                     LandCheckYourAnswersView
                                               )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isReturnIdEmpty = result.exists(_.returnId.isEmpty)
        val isDataEmpty     = result.exists(_.data.value.isEmpty)
        val landDataEmpty   = result.exists(ua => (ua.data \ "landCurrent").asOpt[JsObject].forall(_.values.isEmpty))

        (isReturnIdEmpty, isDataEmpty, result, landDataEmpty) match {
          case (true, _, _, _) =>
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad())

          case (_, true, _, _) =>
            Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())

          case (_, _, Some(_), true) =>
            Redirect(controllers.land.routes.LandBeforeYouStartController.onPageLoad())

          case (_, _, Some(_), false) =>
            val rowResults = buildSummaryList(request.userAnswers)

            checkAnswersService.redirectOrRender(rowResults) match {
              case Left(call)         => Redirect(call)
              case Right(summaryList) => Ok(view(summaryList))
            }

          case (_, _, None, _) =>
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        }
      }
  }

  private def failuresForSessionLand(ua: UserAnswers): Seq[CrossFlowFailure] = {
      val authorityCodeFailures = crossFlow.failuresForPage(Pages.LandAuthorityCode, ua)
      val postcodeFailures = crossFlow.failuresForPage(Pages.LandPostcode, ua)
      val propertyTypeFailures = crossFlow.failuresForPage(Pages.LandPropertyType, ua)
      (authorityCodeFailures ++ postcodeFailures ++ propertyTypeFailures).distinct
    }

  private def redirectFor(failure: CrossFlowFailure): Call = {
    val targets = failure.targets.map(_.page).toSet

    if      (targets.contains(Pages.LandPostcode))      controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(CheckMode)
    else if (targets.contains(Pages.LandPropertyType))  controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode)
    else                                                controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode)
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    sessionRepository.get(request.userAnswers.id).flatMap {
      case Some(userAnswers) =>
        (userAnswers.data \ "landCurrent").validate[LandSessionQuestions] match {
          case JsSuccess(_, _) if landSessionValidation(request.userAnswers) =>
            request.userAnswers.get(LandOverviewPage).map { _ =>
              updateLand(userAnswers)
            }.getOrElse(createLand(userAnswers))

          case _ =>
            Future.successful(
              Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
            )
        }

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def landSessionValidation(userAnswers: UserAnswers): Boolean = {
    val isTitleNumberPresent =
      if titleCheck(userAnswers) then userAnswers.get(LandTitleNumberPage).isDefined
      else true

    val isNlpgUprnPresent =
      if nlpgCheck(userAnswers) then userAnswers.get(LandNlpgUprnPage).isDefined
      else true

    val isAgriculturalPresent =
      if agriculturalCheck(userAnswers) then userAnswers.get(DoYouKnowTheAreaOfLandPage).isDefined
      else true

    val isAreaOfLandPresent =
      if knowsArea(userAnswers) then userAnswers.get(AreaOfLandPage).isDefined
      else true

    isTitleNumberPresent && isNlpgUprnPresent && isAreaOfLandPresent && isAgriculturalPresent
  }

  private def knowsArea(userAnswers: UserAnswers): Boolean =
    propertyTypeCheck(userAnswers) && agriculturalCheck(userAnswers) && knowAreaCheck(userAnswers)

  private def buildSummaryList(userAnswers: UserAnswers)(implicit request: RequestHeader): Seq[SummaryRowResult] = {
    val isMixedOrNonResidential = propertyTypeCheck(userAnswers)
    val isAgricultural          = propertyTypeCheck(userAnswers) && agriculturalCheck(userAnswers)
    val knowsArea               = propertyTypeCheck(userAnswers) && knowAreaCheck(userAnswers) && agriculturalCheck(userAnswers)

    val crossFlowMissing: Seq[SummaryRowResult] =
      failuresForSessionLand(userAnswers).map(f => SummaryRowResult.Missing(redirectFor(f)))

    val landRows: Seq[SummaryRowResult] = Seq(
      Some(LandTypeOfPropertySummary.row(userAnswers)),
      Some(LandInterestTransferredOrCreatedSummary.row(userAnswers)),
      Some(LandAddressSummary.row(userAnswers)),
      Some(LocalAuthorityCodeSummary.row(userAnswers)),
      Some(LandRegisteredHmRegistrySummary.row(userAnswers)),
      Option.when(titleCheck(userAnswers))(LandTitleNumberSummary.row(userAnswers)),
      Some(LandAddNlpgUprnSummary.row(userAnswers)),
      Option.when(nlpgCheck(userAnswers))(LandNlpgUprnSummary.row(userAnswers)),
      Some(LandSendingPlanByPostSummary.row(userAnswers)),
      Some(LandMineralsOrMineralRightsSummary.row(userAnswers)),
      Option.when(isMixedOrNonResidential)(AgriculturalOrDevelopmentalLandSummary.row(userAnswers)),
      Option.when(isAgricultural)(DoYouKnowTheAreaOfLandSummary.row(userAnswers)),
      Option.when(knowsArea)(AreaOfLandSummary.row(userAnswers))
    ).flatten

    crossFlowMissing ++ landRows
  }

  private def updateLand(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      land                       <- Land.from(userAnswers)
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      updateReturnVersionReturn  <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
      updateLandRequest          <- UpdateLandRequest.from(userAnswers, land) if updateReturnVersionReturn.newVersion.isDefined
      updateLandReturn           <- backendConnector.updateLand(updateLandRequest) if updateReturnVersionReturn.newVersion.isDefined
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
      land              <- Land.from(userAnswers)
      createLandRequest <- CreateLandRequest.from(userAnswers, land)
      createLandReturn  <- backendConnector.createLand(createLandRequest)
    } yield {
      if (createLandReturn.landId.nonEmpty) {
        Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
          .flashing("landCreated" -> createLandRequest.addressLine1)
      } else {
        Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def titleCheck(userAnswers: UserAnswers): Boolean =
    userAnswers.get(LandRegisteredHmRegistryPage).contains(true)

  private def nlpgCheck(userAnswers: UserAnswers): Boolean =
    userAnswers.get(LandAddNlpgUprnPage).contains(true)

  private def agriculturalCheck(userAnswers: UserAnswers): Boolean =
    userAnswers.get(AgriculturalOrDevelopmentalLandPage).contains(true)

  private def knowAreaCheck(userAnswers: UserAnswers): Boolean =
    userAnswers.get(DoYouKnowTheAreaOfLandPage).contains(true)

  private def propertyTypeCheck(userAnswers: UserAnswers): Boolean =
    userAnswers.get(LandTypeOfPropertyPage).exists {
      case LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential => true
      case _                                                            => false
    }
}