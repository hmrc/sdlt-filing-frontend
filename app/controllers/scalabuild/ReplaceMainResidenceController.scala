/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.ReplaceMainResidenceFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.ReplaceMainResidencePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ReplaceMainResidenceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReplaceMainResidenceController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: ReplaceMainResidenceView,
    formProvider: ReplaceMainResidenceFormProvider,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form: Form[Boolean] = formProvider()
    val preparedForm = request.userAnswers.get(ReplaceMainResidencePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.body
    val form: Form[Boolean] = formProvider()
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.set(ReplaceMainResidencePage, value)
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ReplaceMainResidencePage, request.userAnswers))
      )
  }
}
