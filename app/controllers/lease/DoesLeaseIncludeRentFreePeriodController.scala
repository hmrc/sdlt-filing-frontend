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

package controllers.lease

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import forms.lease.DoesLeaseIncludeRentFreePeriodFormProvider
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import models.Mode
import navigation.Navigator
import pages.lease.DoesLeaseIncludeRentFreePeriodPage
import repositories.SessionRepository
import views.html.lease.DoesLeaseIncludeRentFreePeriodView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}



class DoesLeaseIncludeRentFreePeriodController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: DoesLeaseIncludeRentFreePeriodFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: DoesLeaseIncludeRentFreePeriodView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(DoesLeaseIncludeRentFreePeriodPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DoesLeaseIncludeRentFreePeriodPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            if(value) {
              Redirect(navigator.nextPage(DoesLeaseIncludeRentFreePeriodPage, mode, updatedAnswers))
            }else {
              //TO`DO: navigate to the le-6 Annual starting rent page
              Redirect(controllers.lease.routes.DoesLeaseIncludeRentFreePeriodController.onPageLoad(mode))
            }
          }
      )
  }
}
