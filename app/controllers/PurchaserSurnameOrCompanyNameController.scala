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

package controllers

import controllers.actions.*
import forms.PurchaserSurnameOrCompanyNameFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PurchaserSurnameOrCompanyNameView

import scala.concurrent.{ExecutionContext, Future}

class PurchaserSurnameOrCompanyNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PurchaserSurnameOrCompanyNameFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PurchaserSurnameOrCompanyNameView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(PurchaserSurnameOrCompanyNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val individualOrBusiness: String = request.userAnswers.get(PurchaserIsIndividualPage) match {
        case Some(value) => if(value.toString == "Individual") "Individual" else "Business"
        case _ => ""
      }

      Ok(view(preparedForm, mode, individualOrBusiness))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val individualOrBusiness: String = request.userAnswers.get(PurchaserIsIndividualPage) match {
        case Some(value) => if(value.toString == "Individual") "Individual" else "Business"
        case _ => ""
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, individualOrBusiness))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserSurnameOrCompanyNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PurchaserSurnameOrCompanyNamePage, mode, updatedAnswers))
      )
  }
}
