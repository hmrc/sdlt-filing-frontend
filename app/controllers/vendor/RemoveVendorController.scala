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
import pages.vendor.VendorOverviewRemovePage
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
      request.userAnswers.get(VendorOverviewRemovePage).map { vendorResourceRef =>

        val maybeVendor = request.userAnswers.fullReturn.flatMap(_.vendor.flatMap(_.find(_.vendorResourceRef.contains(vendorResourceRef))))
        val vendorFullName: Option[String] = maybeVendor.flatMap(vendor => vendor.name.map { name =>
            Seq(vendor.forename1, vendor.forename2, Some(name))
              .flatten.mkString(" ").trim.replaceAll(" +", " ")
        })

        Ok(view(form, vendorFullName))
      }.getOrElse(
        Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(VendorOverviewRemovePage).map { vendorResourceRef =>

        val maybeVendor = request.userAnswers.fullReturn.flatMap(_.vendor.flatMap(_.find(_.vendorResourceRef.contains(vendorResourceRef))))
        val vendorFullName: Option[String] = maybeVendor.flatMap(vendor => vendor.name.map { name =>
          Seq(vendor.forename1, vendor.forename2, Some(name))
            .flatten.mkString(" ").trim.replaceAll(" +", " ")
        })

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, vendorFullName))),
          value =>
            if (value) {
              (for {
                updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                deleteVendorRequest <- DeleteVendorRequest.from(request.userAnswers, vendorResourceRef) if returnVersion.updated
                deleteVendorReturn <- backendConnector.deleteVendor(deleteVendorRequest) if returnVersion.updated
              } yield {
                Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad()).flashing("vendorDeleted" -> vendorFullName.getOrElse(""))
              }).recover {
                case _ =>
                  Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
              }
            }
            else {
              Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad()))
            }
        )
      }.getOrElse(
        Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad()))
      )
  }
}
