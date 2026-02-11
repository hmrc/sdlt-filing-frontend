/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.{ContractPost201603FormProvider, ExchangeContractsFormProvider}
import navigation.scalabuild.Navigator
import pages.scalabuild.{ContractPost201603Page, ExchangeContractsPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ExchangeContractsPreAndPostView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExchangeContractsPreAndPostController @Inject()(
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ExchangeContractsPreAndPostView,
                                                       exchangeContractsFormProvider: ExchangeContractsFormProvider,
                                                       contractPost201603FormProvider: ContractPost201603FormProvider,
                                                       sessionRepository: SessionRepository,
                                                       navigator: Navigator,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       identify: IdentifierAction
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val exchangeContractsForm = exchangeContractsFormProvider()
  val contractPostForm = contractPost201603FormProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val (preparedExchangeContractsForm, preparedContractPostForm) = (request.userAnswers.get(ExchangeContractsPage), request.userAnswers.get(ContractPost201603Page)) match {
      case (Some(exchangeContractsValue), Some(contractPostValue)) => (exchangeContractsForm.fill(exchangeContractsValue), contractPostForm.fill(contractPostValue))
      case (Some(exchangeContractsValue), None) => (exchangeContractsForm.fill(exchangeContractsValue), contractPostForm)
      case (None, Some(contractPostValue)) => (exchangeContractsForm, contractPostForm.fill(contractPostValue))
      case (None, None) => (exchangeContractsForm, contractPostForm)
    }
    Ok(view(preparedExchangeContractsForm, preparedContractPostForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    exchangeContractsForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
        { case true =>
          contractPostForm
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
              contractPostValue => for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers
                    .setTwo(ExchangeContractsPage, true, ContractPost201603Page, contractPostValue)
                )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ExchangeContractsPage, updatedAnswers)))
        case false =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ExchangeContractsPage, false))
            _ <- sessionRepository.set(updatedAnswers)
          }
          yield
            Redirect(navigator.nextPage(ExchangeContractsPage, updatedAnswers))
        }
      )
  }
}