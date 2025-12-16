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

package controllers.vendor

import controllers.actions.*
import forms.vendor.VendorOverviewFormProvider
import models.{GetReturnByRefRequest, Mode, NormalMode, UserAnswers}
import pages.vendor.VendorOverviewRemovePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.FullReturnService
import services.vendor.PopulateVendorService
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.VendorPaginationHelper
import views.html.vendor.VendorOverview

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VendorOverviewController @Inject()(
                                          val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          fullReturnService: FullReturnService,
                                          sessionRepository: SessionRepository,
                                          view: VendorOverview,
                                          formProvider: VendorOverviewFormProvider,
                                          populateVendorService: PopulateVendorService
                                        )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with VendorPaginationHelper with I18nSupport with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, paginationIndex: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val postAction: Call = controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad()
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

              vendorList match {
                case Nil => Ok(view(None, None, None, postAction, form, NormalMode, errorCalc))
                case vendors =>
                  generateVendorSummary(paginationIndex, vendors)
                    .fold(
                      Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
                    ) { summary =>
                      val numberOfPages: Int = getNumberOfPages(vendors)
                      val pagination: Option[Pagination] = generatePagination(paginationIndex, numberOfPages)
                      val paginationText: Option[String] = getPaginationInfoText(paginationIndex, vendors)

                      Ok(view(Some(summary), pagination, paginationText, postAction, form, NormalMode, errorCalc))
                    }
              }
            }
          } recover {
          case ex =>
            logger.error("[VendorOverviewController][onPageLoad] Unexpected failure", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val postAction: Call = controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(NormalMode)
      val vendorList = request.userAnswers.fullReturn.flatMap(_.vendor).getOrElse(Seq.empty)
      val purchaserList = request.userAnswers.fullReturn.flatMap(_.purchaser).getOrElse(Seq.empty)
      val errorCalc: Boolean = (vendorList.length + purchaserList.length) > 99

      form.bindFromRequest().fold(
        formWithErrors => {
          vendorList match {
            case Nil =>
              Future.successful(BadRequest(view(None, None, None, postAction, formWithErrors, mode, errorCalc)))
            case vendors =>
              val summary = generateVendorSummary(1, vendors)
              val numberOfPages = getNumberOfPages(vendors)
              val pagination = generatePagination(1, numberOfPages)
              val paginationText = getPaginationInfoText(1, vendors)

              Future.successful(BadRequest(view(summary, pagination, paginationText, postAction, formWithErrors, mode, errorCalc)))
          }
        },
        value =>
          if (value) {
            Future.successful(Redirect(routes.VendorBeforeYouStartController.onPageLoad()))
          } else {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      )
    }

  def changeVendor(vendorId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val maybeVendor = request.userAnswers.fullReturn
        .flatMap(_.vendor)
        .flatMap(_.find(_.vendorResourceRef.contains(vendorId)))

      maybeVendor match {
        case Some(vendor) =>
          for {
            updatedAnswers <- Future.fromTry(populateVendorService.populateVendorInSession(vendor, vendorId, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removeVendor(vendorId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorOverviewRemovePage, vendorId))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.vendor.routes.RemoveVendorController.onPageLoad())
    }


}



