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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.vendor.RemoveVendorFormProvider
import models.vendor.DeleteVendorRequest
import models.ReturnVersionUpdateRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.RemoveVendorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveVendorController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemoveVendorFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveVendorView,
                                         backendConnector: StampDutyLandTaxConnector
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      // TODO DTR-1028 get the vendorId from the vendorCurrent.removeVendorId
      val tempVendorId = Some("VEN001")
      tempVendorId.map { vendorId =>

        val vendor = request.userAnswers.fullReturn.flatMap(_.vendor.flatMap(_.find(_.vendorID.contains(vendorId))))
        val vendorName: Option[String] = vendor.flatMap(_.name)

        Ok(view(form, vendorName))
      }.getOrElse(
        // TODO DTR-1028 redirect to overview
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      // TODO DTR-1028 get the vendorId from the vendorCurrent.removeVendorId
      val tempVendorId = Some("VEN001")
      tempVendorId.map { vendorId =>

        val vendor = request.userAnswers.fullReturn.flatMap(_.vendor.flatMap(_.find(_.vendorID.contains(vendorId))))
        val vendorResourceRef = vendor.flatMap(_.vendorResourceRef).getOrElse("")
        val vendorName: Option[String] = vendor.flatMap(_.name)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, vendorName))),
          value =>
            if (value) {
              (for {
                updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                deleteVendorRequest <- DeleteVendorRequest.from(request.userAnswers, vendorResourceRef) if returnVersion.updated
                deleteVendorReturn <- backendConnector.deleteVendor(deleteVendorRequest) if returnVersion.updated
              } yield {
                  // TODO DTR-1028 redirect to overview
                  Redirect(controllers.routes.ReturnTaskListController.onPageLoad()).flashing("vendorDeleted" -> "true")
              }).recover {
                case _ =>
                  // TODO DTR-1028 redirect to overview
                  Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
              }
            }
            else {
              // TODO DTR-1028 redirect to overview
              Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
            }
        )
      }.getOrElse(
        // TODO DTR-1028 redirect to overview
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      )
  }
}
