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

import controllers.actions.*
import forms.land.LandOverviewFormProvider
import models.{GetReturnByRefRequest, Mode, NormalMode, UserAnswers}
import pages.land.LandOverviewRemovePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.FullReturnService
import services.land.PopulateLandService
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LandPaginationHelper
import views.html.land.LandOverviewView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LandOverviewController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         fullReturnService: FullReturnService,
                                         formProvider: LandOverviewFormProvider,
                                         populateLandService: PopulateLandService,
                                         landPaginationHelper: LandPaginationHelper,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LandOverviewView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode, paginationIndex: Int = 1): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val postAction: Call = controllers.land.routes.LandBeforeYouStartController.onPageLoad()
      val effectiveReturnId = request.userAnswers.returnId

      effectiveReturnId.fold(
        Future.successful(Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()))
      ) { id =>
        fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = request.userAnswers.storn))
          .flatMap { fullReturn =>
            val userAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn), storn = request.userAnswers.storn)
            sessionRepository.set(userAnswers).map { _ =>

              val landList = fullReturn.land.getOrElse(Seq.empty)
              val errorCalc: Boolean = landList.length >= 99

              landList match {
                case Nil => Ok(view(None, None, None, postAction, form, NormalMode, errorCalc))
                case lands =>
                  landPaginationHelper.generateLandSummary(paginationIndex, lands, userAnswers)
                    .fold(
                      Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
                    ) { summary =>
                      val numberOfPages: Int = landPaginationHelper.getNumberOfPages(lands)
                      val pagination: Option[Pagination] = landPaginationHelper.generatePagination(paginationIndex, numberOfPages)
                      val paginationText: Option[String] = landPaginationHelper.getPaginationInfoText(paginationIndex, lands)
                      
                      Ok(view(Some(summary), pagination, paginationText, postAction, form, NormalMode, errorCalc))
                    }
              }
            }
          } recover {
          case ex =>
            logger.error("[LandOverviewController][onPageLoad] Unexpected failure", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val postAction: Call = controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(NormalMode)
      val landList = request.userAnswers.fullReturn.flatMap(_.land).getOrElse(Seq.empty)
      val errorCalc: Boolean = landList.length >= 99

      form.bindFromRequest().fold(
        formWithErrors => {
          landList match {
            case Nil =>
              Future.successful(BadRequest(view(None, None, None, postAction, formWithErrors, mode, errorCalc)))
            case lands =>
              val summary = landPaginationHelper.generateLandSummary(1, lands, request.userAnswers)
              val numberOfPages = landPaginationHelper.getNumberOfPages(lands)
              val pagination = landPaginationHelper.generatePagination(1, numberOfPages)
              val paginationText = landPaginationHelper.getPaginationInfoText(1, lands)

              Future.successful(BadRequest(view(summary, pagination, paginationText, postAction, formWithErrors, mode, errorCalc)))
          }
        },
        value =>
          if (value) {
            Future.successful(Redirect(controllers.land.routes.LandBeforeYouStartController.onPageLoad()))
          } else {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      )
  }

  def changeLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val maybeLand = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.find(_.landID.contains(landId)))

      maybeLand match {
        case Some(land) =>
          for {
            updatedAnswers <- Future.fromTry(populateLandService.populateLandInSession(land, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removeLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(LandOverviewRemovePage, landId))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.land.routes.RemoveLandController.onPageLoad())
    }
}
