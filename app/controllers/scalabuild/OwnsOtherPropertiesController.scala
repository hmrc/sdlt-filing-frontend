/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.OwnsOtherPropertiesFormProvider
import pages.scalabuild.OwnsOtherPropertiesPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.OwnsOtherPropertiesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OwnsOtherPropertiesController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: OwnsOtherPropertiesView,
    formProvider: OwnsOtherPropertiesFormProvider,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form: Form[Boolean] = formProvider()
    val preparedForm = request.userAnswers.get(OwnsOtherPropertiesPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val form: Form[Boolean] = formProvider()
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.set(OwnsOtherPropertiesPage, value)
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.scalabuild.routes.OwnsOtherPropertiesController.onPageLoad().url)
      )
  }
}
