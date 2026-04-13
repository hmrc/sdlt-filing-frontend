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
import forms.transaction.TotalConsiderationOfTransactionFormProvider
import models.{Mode, UserAnswers}
import models.prelimQuestions.TransactionType
import navigation.Navigator
import pages.transaction.{TotalConsiderationOfTransactionPage, TypeOfTransactionPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.TotalConsiderationOfTransactionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalConsiderationOfTransactionController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: TotalConsiderationOfTransactionFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: TotalConsiderationOfTransactionView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val form = buildForm(request.userAnswers)

      val preparedForm = request.userAnswers.get(TotalConsiderationOfTransactionPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = buildForm(request.userAnswers)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TotalConsiderationOfTransactionPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TotalConsiderationOfTransactionPage, mode, updatedAnswers))
      )
  }

  private def buildForm(userAnswers: UserAnswers): Form[String] = {
    val vat: Option[BigDecimal] = None // TODO DTR-2942 tr-5a replace with VAT included in total consideration
    val isTransactionNonLeased = !userAnswers.get(TypeOfTransactionPage).contains(TransactionType.GrantOfLease)
    val linkedTransactionConsideration: Option[BigDecimal] = None // TODO DTR-2956 tr -7a replace with total consideration of linked transaction

    def validateVatIncludedInConsideration(totalConsideration: String): Boolean =
      !vat.exists(_ > BigDecimal(totalConsideration))

    def validateTotalConsideration(totalConsideration: String): Boolean = {
      val totalConNum = BigDecimal(totalConsideration)
      if (isTransactionNonLeased) {
        if totalConNum < 0 then false
        else if linkedTransactionConsideration.exists(totalConNum > _) then false
        else true
      } else {
        true
      }
    }

    formProvider(validateVatIncludedInConsideration, validateTotalConsideration)
  }
}
