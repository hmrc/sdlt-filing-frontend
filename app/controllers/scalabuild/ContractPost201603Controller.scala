/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.ContractPost201603FormProvider
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ContractPost201603View
import pages.scalabuild.ContractPost201603Page
import play.api.i18n.I18nSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContractPost201603Controller @Inject()(
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ContractPost201603View,
                                              formProvider: ContractPost201603FormProvider,
                                              sessionRepository: SessionRepository,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              identify: IdentifierAction
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController  with I18nSupport {
  val form = formProvider()
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData)  { implicit request =>
    val preparedForm = request.userAnswers.get(ContractPost201603Page).fold(form)(value => form.fill(value))
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ContractPost201603Page, value))
            _ <- sessionRepository.set(updatedAnswers)
          }
            yield Redirect(controllers.scalabuild.routes.ContractPost201603Controller.onPageLoad().url)
      )
  }
}