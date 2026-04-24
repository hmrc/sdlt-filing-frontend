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

package controllers.transaction

import controllers.actions.*
import forms.transaction.TransactionUseOfLandOrPropertyFormProvider
import models.Mode
import models.transaction.TransactionUseOfLandOrPropertyAnswers
import navigation.Navigator
import pages.transaction.TransactionUseOfLandOrPropertyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.TransactionUseOfLandOrPropertyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionUseOfLandOrPropertyController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: TransactionUseOfLandOrPropertyFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: TransactionUseOfLandOrPropertyView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val mainLandId = request.userAnswers.fullReturn.flatMap(_.returnInfo).flatMap(_.mainLandID)
      val typeOfProperty = request.userAnswers.fullReturn.flatMap(_.land).flatMap(_.find(l => l.landID == mainLandId)).flatMap(_.propertyType)

      typeOfProperty match {
        case Some("02") | Some("03") => // Mixed or Non-residential
          val preparedForm = request.userAnswers.get(TransactionUseOfLandOrPropertyPage) match {
            case None => form
            case Some(answersObject) => form.fill(TransactionUseOfLandOrPropertyAnswers.toSet(answersObject))
          }

          Ok(view(preparedForm, mode))
          
        case _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO DTR-3446: Redirect to Is this transaction part of the sale of a business? - tr-12
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        selectedValues =>
          val answers = TransactionUseOfLandOrPropertyAnswers.fromSet(selectedValues)

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TransactionUseOfLandOrPropertyPage, answers))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(navigator.nextPage(TransactionUseOfLandOrPropertyPage, mode, updatedAnswers))
          }
      )
  }
}
