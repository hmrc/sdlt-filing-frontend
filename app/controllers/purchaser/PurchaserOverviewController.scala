/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.PurchaserOverviewFormProvider
import models.purchaser.PurchaserAndCompanyId
import models.{GetReturnByRefRequest, Mode, NormalMode, UserAnswers}
import pages.purchaser.PurchaserOverviewRemovePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.FullReturnService
import services.purchaser.{PopulatePurchaserService, PurchaserService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PurchaserPaginationHelper
import views.html.purchaser.PurchaserOverview

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaserOverviewController @Inject()(val controllerComponents: MessagesControllerComponents,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            fullReturnService: FullReturnService,
                                            sessionRepository: SessionRepository,
                                            view: PurchaserOverview,
                                            formProvider: PurchaserOverviewFormProvider,
                                            populatePurchaserService: PopulatePurchaserService,
                                            purchaserPaginationHelper: PurchaserPaginationHelper,
                                            purchaserService: PurchaserService
                                           )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, paginationIndex: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val postAction: Call = controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad()
      val effectiveReturnId = request.userAnswers.returnId

      effectiveReturnId.fold(
        Future.successful(Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()))
      ) { id =>
        fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = request.userAnswers.storn))
          .flatMap { fullReturn =>
            val userAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn), storn = request.userAnswers.storn)
            sessionRepository.set(userAnswers).map { _ =>

              val vendorList = fullReturn.vendor.getOrElse(Seq.empty)
              val purchaserList = fullReturn.purchaser.getOrElse(Seq.empty)
              val errorCalc: Boolean = (vendorList.length + purchaserList.length) > 99
              val isMainPurchaserComplete = purchaserService.isMainPurchaserComplete(userAnswers)

              purchaserList match {
                case Nil => Ok(view(None, None, None, postAction, form, NormalMode, errorCalc, isMainPurchaserComplete))
                case purchasers =>
                  purchaserPaginationHelper.generatePurchaserSummary(paginationIndex, purchasers, userAnswers)
                    .fold(
                      Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
                    ) { summary =>
                      val numberOfPages: Int = purchaserPaginationHelper.getNumberOfPages(purchasers)
                      val pagination: Option[Pagination] = purchaserPaginationHelper.generatePagination(paginationIndex, numberOfPages)
                      val paginationText: Option[String] = purchaserPaginationHelper.getPaginationInfoText(paginationIndex, purchasers)

                      val mainPurchaserId = purchaserService.getMainPurchaser(userAnswers).flatMap(_.purchaserID)
                      val mainPurchaserName = purchaserService.mainPurchaserName(userAnswers).map(_.fullName)
                      Ok(view(Some(summary), pagination, paginationText, postAction, form,
                        NormalMode, errorCalc, isMainPurchaserComplete, mainPurchaserId, mainPurchaserName))
                    }
              }
            }
          } recover {
          case ex =>
            logger.error("[PurchaserOverviewController][onPageLoad] Unexpected failure", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val postAction: Call = controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)
      val vendorList = request.userAnswers.fullReturn.flatMap(_.vendor).getOrElse(Seq.empty)
      val purchaserList = request.userAnswers.fullReturn.flatMap(_.purchaser).getOrElse(Seq.empty)
      val errorCalc: Boolean = (vendorList.length + purchaserList.length) > 99
      val isMainPurchaserComplete = purchaserService.isMainPurchaserComplete(request.userAnswers)

      form.bindFromRequest().fold(
        formWithErrors => {
          purchaserList match {
            case Nil =>
              Future.successful(BadRequest(view(None, None, None, postAction, formWithErrors, mode, errorCalc, isMainPurchaserComplete)))
            case purchasers =>
              val summary = purchaserPaginationHelper.generatePurchaserSummary(1, purchasers, request.userAnswers)
              val numberOfPages = purchaserPaginationHelper.getNumberOfPages(purchasers)
              val pagination = purchaserPaginationHelper.generatePagination(1, numberOfPages)
              val paginationText = purchaserPaginationHelper.getPaginationInfoText(1, purchasers)

              Future.successful(BadRequest(view(summary, pagination, paginationText, postAction, formWithErrors, mode, errorCalc, isMainPurchaserComplete)))
          }
        },
        value =>
          if (value) {
            Future.successful(Redirect(controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad()))
          } else {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      )
    }

  def changePurchaser(purchaserId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val maybePurchaser = request.userAnswers.fullReturn
        .flatMap(_.purchaser)
        .flatMap(_.find(_.purchaserID.contains(purchaserId)))

      maybePurchaser match {
        case Some(purchaser) =>
          for {
            updatedAnswers <- Future.fromTry(
              populatePurchaserService.populatePurchaserInSession(purchaser,
                purchaserId, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removePurchaser(purchaserId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val maybeCompanyDetailsId: Option[String] = request.userAnswers.fullReturn.flatMap(_.companyDetails.flatMap(_.companyDetailsID))
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserId, maybeCompanyDetailsId)))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.purchaser.routes.PurchaserRemoveController.onPageLoad())
    }
}



