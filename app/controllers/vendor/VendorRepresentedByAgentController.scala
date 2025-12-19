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
import forms.vendor.VendorRepresentedByAgentFormProvider
import models.*
import navigation.Navigator
import pages.vendor.{VendorOrCompanyNamePage, VendorRepresentedByAgentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendor.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.VendorRepresentedByAgentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorRepresentedByAgentController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: VendorRepresentedByAgentFormProvider,
                                         agentChecksService: AgentChecksService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: VendorRepresentedByAgentView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(VendorOrCompanyNamePage).map(_.fullName) match {
        case None =>
          Redirect(controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(mode))
        
        case Some(vendorName) =>
          val preparedForm = request.userAnswers.get(VendorRepresentedByAgentPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val continueRoute = Ok(view(preparedForm, mode, vendorName))
          agentChecksService.checkMainVendorAgentRepresentedByAgent(request.userAnswers, continueRoute)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(VendorOrCompanyNamePage).map(_.fullName) match {
        case None =>
          Future.successful(Redirect(controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(mode)))
        
        case Some(vendorName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, vendorName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorRepresentedByAgentPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value) {
                  Redirect(navigator.nextPage(VendorRepresentedByAgentPage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())
                }
              }
          )
      }
  }
}
