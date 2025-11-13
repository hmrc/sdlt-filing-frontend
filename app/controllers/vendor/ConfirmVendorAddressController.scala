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
import forms.vendor.ConfirmVendorAddressFormProvider
import models.vendor.ConfirmVendorAddress
import models.{GetReturnByRefRequest, Mode}
import navigation.Navigator
import pages.vendor.{ConfirmVendorAddressPage, VendorOrBusinessNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.ConfirmVendorAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmVendorAddressController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          sessionRepository: SessionRepository,
                                          navigator: Navigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: ConfirmVendorAddressFormProvider,
                                          backendConnector: StampDutyLandTaxConnector,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: ConfirmVendorAddressView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[ConfirmVendorAddress] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers = request.userAnswers

      userAnswers.get(VendorOrBusinessNamePage) match {
        case None =>
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())

        case Some(vn) =>
          val vendorOrBusinessName = Seq(vn.forename1, vn.forename2, Some(vn.name))
            .flatten.mkString(" ").trim.replaceAll(" +", " ")

          userAnswers.fullReturn match {
            case None =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

            case Some(fullReturn) =>
              val vendors = fullReturn.vendor.getOrElse(Seq.empty)

              if (vendors.isEmpty) {
                Redirect(controllers.vendor.routes.VendorAddressController.redirectToAddressLookupVendor())
              } else {
                val mainId = fullReturn.returnInfo.flatMap(_.mainVendorID).getOrElse("")
                val mainVendor = vendors.find(_.vendorID.contains(mainId)).orElse(vendors.headOption).getOrElse(models.Vendor())
                val line1 = mainVendor.address1
                val line2 = mainVendor.address2
                val line3 = mainVendor.address3
                val line4 = mainVendor.address4
                val postcode = mainVendor.postcode
                val preparedForm = userAnswers.get(ConfirmVendorAddressPage).fold(form)(form.fill)

                Ok(view(preparedForm, vendorOrBusinessName, line1, line2, line3, line4, postcode, mode))
              }
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers = request.userAnswers

      userAnswers.get(VendorOrBusinessNamePage) match {
        case None =>
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())

        case Some(vn) =>
          val vendorOrBusinessName = Seq(vn.forename1, vn.forename2, Some(vn.name))
            .flatten.mkString(" ").trim.replaceAll(" +", " ")

          userAnswers.fullReturn match {
            case None =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

            case Some(fullReturn) =>
              val mainId = fullReturn.returnInfo.flatMap(_.mainVendorID).getOrElse("")
              val mainVendor = fullReturn.vendor.getOrElse(Seq.empty).find(_.vendorID.contains(mainId))
                .orElse(fullReturn.vendor.flatMap(_.headOption))
                .getOrElse(models.Vendor())
              val line1 = mainVendor.address1
              val line2 = mainVendor.address2
              val line3 = mainVendor.address3
              val line4 = mainVendor.address4
              val postcode = mainVendor.postcode

              form.bindFromRequest().fold(
                formWithErrors =>
                  BadRequest(view(formWithErrors, vendorOrBusinessName, line1, line2, line3, line4, postcode, mode)),
                value =>
                  val updatedAnswers = userAnswers.set(ConfirmVendorAddressPage, value).get
                  sessionRepository.set(updatedAnswers)

                  value match {
                    case ConfirmVendorAddress.Yes => Redirect(navigator.nextPage(ConfirmVendorAddressPage, mode, updatedAnswers))
                    case ConfirmVendorAddress.No => Redirect(controllers.vendor.routes.VendorAddressController.redirectToAddressLookupVendor())
                  }
              )
          }
      }
  }
}