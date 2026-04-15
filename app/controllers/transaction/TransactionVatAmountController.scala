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
import forms.transaction.TransactionVatAmountFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.transaction.{TotalConsiderationOfTransactionPage, TransactionVatAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.TransactionVatAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionVatAmountController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: TransactionVatAmountFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: TransactionVatAmountView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val form = buildForm(request.userAnswers)

      val preparedForm = request.userAnswers.get(TransactionVatAmountPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TransactionVatAmountPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TransactionVatAmountPage, mode, updatedAnswers))
      )
  }

  private def buildForm(userAnswers: UserAnswers): Form[String] = {
    val totalConsideration: Option[BigDecimal] = userAnswers.get(TotalConsiderationOfTransactionPage).map(BigDecimal(_))

    def validateVatIncludedInConsideration(vat: String): Boolean =
      !totalConsideration.exists(BigDecimal(vat) > _)

    formProvider(validateVatIncludedInConsideration)
  }
}
