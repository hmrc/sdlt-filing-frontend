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
import models.ukResidency.{CreateResidencyRequest, UpdateResidencyRequest}
import models.{ReturnVersionUpdateRequest, UserAnswers}
import pages.ukResidency.{CloseCompanyPage, CrownEmploymentReliefPage, NonUkResidentPurchaserPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsObject
import play.api.mvc.*
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.UpdateTaxCalcService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil
import utils.PropertyTypeHelper.isResidentialProperty
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.ukResidency.{CloseCompanySummary, CrownEmploymentReliefSummary, NonUkResidentPurchaserSummary}
import views.html.ukResidency.UkResidencyCheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

@Singleton
class UkResidencyCheckYourAnswersController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       statusCheck: CheckSubmissionStatusAction,
                                                       sessionRepository: SessionRepository,
                                                       checkAnswersService: CheckAnswersService,
                                                       backendConnector: StampDutyLandTaxConnector,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: UkResidencyCheckYourAnswersView,
                                                       updateTaxCalcService: UpdateTaxCalcService
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with LoggingUtil {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      request.userAnswers.fullReturn match {
        case Some(fullReturn) if isResidentialProperty(fullReturn) =>
          sessionRepository.get(request.userAnswers.id).flatMap(handleSessionResult)
        case _ =>
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  private def handleSessionResult(result: Option[UserAnswers])(implicit request: Request[_]): Future[Result] =
    result match {
      case Some(userAnswers) if userAnswers.returnId.isEmpty   => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Some(userAnswers) if (userAnswers.data \ "ukResidencyCurrent").asOpt[JsObject].forall(_.values.isEmpty) => populateFromResidency(userAnswers)
      case Some(userAnswers)                                   => Future.successful(renderOrRedirect(userAnswers))
      case None                                                => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
    }

  private def populateFromResidency(userAnswers: UserAnswers)(implicit request: Request[_]): Future[Result] =
    userAnswers.fullReturn.flatMap(_.residency) match {
      case None =>
        Future.successful(Redirect(controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad()))
      case Some(residency) =>
        val isCompany: Boolean = userAnswers.fullReturn
          .flatMap(_.purchaser)
          .getOrElse(Seq.empty)
          .exists(_.isCompany.exists(_.equalsIgnoreCase("yes")))

        val isNonUkResident: Boolean = residency.isNonUkResidents.exists(_.equalsIgnoreCase("yes"))

        val populatedResult = for {
          ua1  <- residency.isNonUkResidents match {
            case Some(value) =>
              userAnswers.set(
                NonUkResidentPurchaserPage,
                value.equalsIgnoreCase("yes")
              )
            case None =>
              Success(userAnswers)
          }
          ua2 <- if (isCompany) {
            residency.isCloseCompany match {
              case Some(value) =>
                ua1.set(
                  CloseCompanyPage,
                  value.equalsIgnoreCase("yes")
                )
              case None =>
                Success(ua1)
            }
          } else {
            Success(ua1)
          }
          ua3 <- if (isNonUkResident) {
            residency.isCrownRelief match {
              case Some(value) =>
                ua2.set(
                  CrownEmploymentReliefPage,
                  value.equalsIgnoreCase("yes")
                )
              case None =>
                Success(ua2)
            }
          } else {
            ua2.remove(CrownEmploymentReliefPage)
          }
        } yield ua3

        populatedResult match {
          case Success(populated) =>
            sessionRepository.set(populated).map(_ => renderOrRedirect(populated))

          case Failure(_) =>
            Future.successful(
              Redirect(controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad())
            )
        }
    }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
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
      logger.debug(s"[UkResidencyCheckYourAnswersController][createResidency] create residency request: $createResidencyReturn")
      if (createResidencyReturn.created) {
        infoLog(s"[UkResidencyCheckYourAnswersController][createResidency] residency has been successfully created. ReturnId=${createResidencyRequest.returnResourceRef}")
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        warnLog(s"[UkResidencyCheckYourAnswersController][createResidency] residency has not been created. ReturnId=${createResidencyRequest.returnResourceRef}")
        Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def updateResidency(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      versionResult <-
        backendConnector
          .updateReturnVersion(updateReturnVersionRequest)
          .map(Right(_))
          .recover { case NonFatal(_) =>
            Left(Redirect(controllers.routes.UpdateReturnVersionErrorController.onPageLoad()))
          }
      result <- versionResult match {
        case Left(errorRedirect) =>
          Future.successful(errorRedirect)

        case Right(updateReturnVersionReturn) if updateReturnVersionReturn.newVersion.isDefined =>
          for {
            updateResidencyRequest <- UpdateResidencyRequest.from(userAnswers)
            updateResidencyReturn <- backendConnector.updateResidency(updateResidencyRequest)
            _ <- maybeUpdateResidencyTaxCalc(userAnswers)
          } yield {
            logger.debug(s"[UkResidencyCheckYourAnswersController][updateResidency] update residency request: $updateResidencyReturn")
            if (updateResidencyReturn.updated)
              infoLog(s"[UkResidencyCheckYourAnswersController][updateResidency] residency has been successfully updated. ReturnId=${updateResidencyRequest.returnResourceRef}")
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            else
              warnLog(s"[UkResidencyCheckYourAnswersController][updateResidency] residency has not been updated. ReturnId=${updateResidencyRequest.returnResourceRef}")
              Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad())
          }

        case Right(_) =>
          Future.successful(
            Redirect(controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad())
          )
      }
    } yield result
  }
  private def maybeUpdateResidencyTaxCalc(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] =
    if (updateTaxCalcService.residencyDataMatches(userAnswers)) {
      for {
        req <- updateTaxCalcService.updateTaxCalcRequest(userAnswers)
        updateTaxCalculationReturn <- backendConnector.updateTaxCalculationInfo(req)
      } yield {
        logger.debug(s"[UkResidencyCheckYourAnswersController][maybeUpdateResidencyTaxCalc] update residency tax calculation request: $updateTaxCalculationReturn")
        if (updateTaxCalculationReturn.updated) {
          infoLog(s"[UkResidencyCheckYourAnswersController][maybeUpdateResidencyTaxCalc] residency tax calculation has been successfully updated. ReturnId=${req.returnResourceRef}")
        } else {
          warnLog(s"[UkResidencyCheckYourAnswersController][maybeUpdateResidencyTaxCalc] residency tax calculation has not been updated. ReturnId=${req.returnResourceRef}")
        }
      }
    } else {
      Future.successful(())
    }

  private def renderOrRedirect(ua: UserAnswers)(implicit request: Request[_]): Result =
    checkAnswersService.redirectOrRender(buildSummaryList(ua)) match {
      case Left(call) => Redirect(call)
      case Right(summaryList) => Ok(view(summaryList))
    }

  private def buildSummaryList(userAnswers: UserAnswers)(implicit request: Request[_]): Seq[SummaryRowResult]  =
    Seq(
      Some(NonUkResidentPurchaserSummary.row(userAnswers)),
      CloseCompanySummary.row(userAnswers),
      CrownEmploymentReliefSummary.row(userAnswers)
    ).flatten

}