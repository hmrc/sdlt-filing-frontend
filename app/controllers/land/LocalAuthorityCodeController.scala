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

package controllers.land

import controllers.actions.*
import forms.land.LocalAuthorityCodeFormProvider
import models.Mode
import navigation.Navigator
import pages.land.{LandAddressPage, LocalAuthorityCodePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LocalAuthorityCodeView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocalAuthorityCodeController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: LocalAuthorityCodeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: LocalAuthorityCodeView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers = request.userAnswers
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

      val transactionDates: Option[(Option[String], Option[String])] = for {
        fr <- userAnswers.fullReturn
        tr <- fr.transaction
      } yield (tr.effectiveDate, tr.contractDate)

      val effectiveTransactionDate: Option[LocalDate] =
        transactionDates.flatMap { case (effOpt, _) => effOpt.map(LocalDate.parse(_, formatter)) }

      val contractEffDate: Option[LocalDate] =
        transactionDates.flatMap { case (_, conOpt) => conOpt.map(LocalDate.parse(_, formatter)) }

      val landPostcode: String =
        request.userAnswers.get(LandAddressPage).flatMap(_.postcode)
          .orElse {
            for {
              fr    <- request.userAnswers.fullReturn
              lands <- fr.land
              pc    <- lands.flatMap(_.postcode).headOption
            } yield pc
          }.getOrElse("")
          

      val form = formProvider(effectiveTransactionDate, contractEffDate, landPostcode)
      val preparedForm = request.userAnswers.get(LocalAuthorityCodePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

      val transactionDates: Option[(Option[String], Option[String])] = for {
        fr <- userAnswers.fullReturn
        tr <- fr.transaction
      } yield (tr.effectiveDate, tr.contractDate)

      val effectiveTransactionDate: Option[LocalDate] =
        transactionDates.flatMap { case (effOpt, _) => effOpt.map(LocalDate.parse(_, formatter)) }

      val contractEffDate: Option[LocalDate] =
        transactionDates.flatMap { case (_, conOpt) => conOpt.map(LocalDate.parse(_, formatter)) }

      val landPostcode: String =
        request.userAnswers.get(LandAddressPage).flatMap(_.postcode)
          .orElse {
            for {
              fr    <- request.userAnswers.fullReturn
              lands <- fr.land
              pc    <- lands.flatMap(_.postcode).headOption
            } yield pc
          }.getOrElse("")

      val form = formProvider(effectiveTransactionDate, contractEffDate, landPostcode)
      
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(LocalAuthorityCodePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(LocalAuthorityCodePage, mode, updatedAnswers))
      )
  }
}
