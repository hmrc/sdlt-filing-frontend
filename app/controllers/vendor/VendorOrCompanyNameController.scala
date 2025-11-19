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
import forms.vendor.VendorOrCompanyNameFormProvider
import models.Mode
import models.vendor.VendorName
import navigation.Navigator
import pages.vendor.{VendorOrCompanyNamePage, WhoIsTheVendorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.VendorOrCompanyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorOrCompanyNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: VendorOrCompanyNameFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: VendorOrCompanyNameView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      
      val vendorOrCompany: String = request.userAnswers.get(WhoIsTheVendorPage) match {
        case Some(value) => if (value.toString == "Individual") "Individual" else "Company"
        case _ => ""
      }
      
      val preparedForm = request.userAnswers.get(VendorOrCompanyNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, vendorOrCompany))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val vendorOrCompany: String = request.userAnswers.get(WhoIsTheVendorPage) match {
        case Some(value) => if (value.toString == "Individual") "Individual" else "Company"
        case _ => ""
      }
      
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, vendorOrCompany))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorOrCompanyNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(navigator.nextPage(VendorOrCompanyNamePage, mode, updatedAnswers))
          }
      )
  }
}
