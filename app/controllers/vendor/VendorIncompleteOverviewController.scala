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

package controllers.vendor

import controllers.actions.*
import models.Vendor
import pages.vendor.VendorOverviewRemovePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendor.PopulateVendorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.VendorTaskList
import views.html.vendor.VendorIncompleteOverviewView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VendorIncompleteOverviewController @Inject() (
                                                     override val messagesApi:  MessagesApi,
                                                     identify:                  IdentifierAction,
                                                     getData:                   DataRetrievalAction,
                                                     requireData:               DataRequiredAction,
                                                     statusCheck:               CheckSubmissionStatusAction,
                                                     sessionRepository:         SessionRepository,
                                                     populateVendorService:     PopulateVendorService,
                                                     val controllerComponents:  MessagesControllerComponents,
                                                     view:                      VendorIncompleteOverviewView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val incompleteVendors: Seq[Vendor] = request.userAnswers.fullReturn
        .map(fullReturn => VendorTaskList.incompleteVendors(fullReturn))
        .getOrElse(Seq.empty)

      if (incompleteVendors.isEmpty) {
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))
      } else {
        Future.successful(Ok(view(
          vendors     = incompleteVendors,
          continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url
        )))
      }
    }

  def updateVendor(vendorId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>

      val maybeVendor = request.userAnswers.fullReturn
        .flatMap(_.vendor)
        .flatMap(_.find(_.vendorID.contains(vendorId)))

      maybeVendor match {
        case Some(vendor) =>
          for {
            updatedAnswers <- Future.fromTry(populateVendorService.populateVendorInSession(vendor, vendorId, request.userAnswers))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removeVendor(vendorId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen statusCheck).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorOverviewRemovePage, vendorId))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.vendor.routes.RemoveVendorController.onPageLoad())
    }
}