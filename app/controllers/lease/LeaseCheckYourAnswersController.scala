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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import models.{Lease, ReturnVersionUpdateRequest, UserAnswers}
import models.lease.{CreateLeaseRequest, LeaseSessionQuestions, UpdateLeaseRequest}
import models.prelimQuestions.TransactionType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsSuccess}
import play.api.mvc.*
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.lease.*
import services.taxCalculation.UpdateTaxCalcService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.lease.*
import viewmodels.checkAnswers.summary.SummaryRowResult
import views.html.lease.LeaseCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class LeaseCheckYourAnswersController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction,
                                                          sessionRepository: SessionRepository,
                                                          backendConnector: StampDutyLandTaxConnector,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: LeaseCheckYourAnswersView,
                                                          leaseService: LeaseService,
                                                          populateLeaseService: PopulateLeaseService,
                                                          checkAnswersService: CheckAnswersService,
                                                          updateTaxCalcService: UpdateTaxCalcService
                                                        )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      leaseService.leaseFlowValidationCheck(request.userAnswers) match {
        case Some(redirect) => Future.successful(Redirect(redirect))
        case None =>
          sessionRepository.get(request.userAnswers.id).flatMap {
            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

            case Some(userAnswers) =>
              if (userAnswers.returnId.isEmpty) {
                Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
              } else {
                val isLeaseDataEmpty =
                  (userAnswers.data \ "leaseCurrent").asOpt[JsObject].forall(_.values.isEmpty)

                if (isLeaseDataEmpty) {
                  populateFromLease(userAnswers)
                } else {
                  Future.successful(renderOrRedirect(userAnswers))
                }
              }
          }
      }
  }
  
  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    sessionRepository.get(request.userAnswers.id).flatMap {
      case Some(userAnswers) =>
        (userAnswers.data \ "leaseCurrent").validate[LeaseSessionQuestions] match {
          case JsSuccess(sessionData, _) =>
            val hasLeaseId = userAnswers.fullReturn.flatMap(_.lease).flatMap(_.leaseID).isDefined
            if (hasLeaseId) {
              updateLease(userAnswers)
            }
            else {
              createLease(userAnswers)
            }
          case _ =>
            Future.successful(
              Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
            )
        }
      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def createLease(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      lease <- Lease.from(userAnswers)
      createLeaseRequest <- CreateLeaseRequest.from(userAnswers, lease)
      createLeaseReturn <- backendConnector.createLease(createLeaseRequest)
    } yield {
      if (createLeaseReturn.created) {
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def updateLease(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      lease <- Lease.from(userAnswers)
      updateRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      version <- backendConnector.updateReturnVersion(updateRequest)
      result <-
        if (version.newVersion.isDefined) {
          for {
            updateLeaseRequest <- UpdateLeaseRequest.from(userAnswers, lease)
            updateLeaseReturn <- backendConnector.updateLease(updateLeaseRequest)
            _ <- maybeUpdateLeaseTaxCalc(userAnswers)
          } yield
            if (updateLeaseReturn.updated) {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            } else {
              Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
            }
        } else {
          Future.successful(
            Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
          )
        }
    } yield result
  }

  private def maybeUpdateLeaseTaxCalc(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] =
    if (updateTaxCalcService.leaseDataMatches(userAnswers)) {
      for {
        req <- updateTaxCalcService.updateTaxCalcRequest(userAnswers)
        _ <- backendConnector.updateTaxCalculationInfo(req)
      } yield ()
    } else {
      Future.successful(())
    }


  private def populateFromLease(userAnswers: UserAnswers)(implicit request: Request[_]): Future[Result] =
    userAnswers.fullReturn.flatMap(_.lease) match {
      case None =>
        Future.successful(Redirect(controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad()))
      case Some(lease) =>
        populateLeaseService.populateLeaseInSession(lease, userAnswers) match {
          case Success(populated) =>
            sessionRepository.set(populated).map(_ => renderOrRedirect(populated))
          case Failure(_) =>
            Future.successful(Redirect(controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad()))
        }
    }

  private def renderOrRedirect(ua: UserAnswers)(implicit request: Request[_]): Result =
    checkAnswersService.redirectOrRender(buildRowResults(ua)) match {
      case Left(call) => Redirect(call)
      case Right(summaryList) => Ok(view(summaryList))
    }

  private def buildRowResults(ua: UserAnswers)(implicit request: Request[_]): Seq[SummaryRowResult] = {
    Seq(
      Some(TypeOfLeaseSummary.row(ua)),
      Some(LeaseStartDateSummary.row(ua)),
      Some(LeaseEndDateSummary.row(ua)),
      Some(DoesLeaseIncludeRentFreePeriodSummary.row(ua)),
      LeaseEnterRentFreePeriodSummary.row(ua),
      Some(AnnualStartingRentSummary.row(ua)),
      Some(LeaseStartingRentEndDateSummary.row(ua)),
      Some(LaterRentSummary.row(ua)),
      Some(LeaseThousandPoundsThresholdSummary.row(ua)),
      Some(LeaseIsVatPayableSummary.row(ua)),
      EnterAnnualRentVatSummary.row(ua),
      if (isGrantOfLease(ua)) Some(LeaseEnterTotalPremiumPayableSummary.row(ua)) else None,
      if (isGrantOfLease(ua)) Some(LeaseNetPresentValueSummary.row(ua)) else None
    ).flatten
  }

  private def isGrantOfLease(ua: UserAnswers): Boolean = {
    leaseService.transactionType(ua).contains(TransactionType.GrantOfLease)
  }
}