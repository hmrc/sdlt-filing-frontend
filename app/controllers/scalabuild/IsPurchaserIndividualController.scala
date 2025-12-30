/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.IsPurchaserIndividualFormProvider
import pages.scalabuild.IsPurchaserIndividualPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.IsPurchaserIndividualView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IsPurchaserIndividualController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: IsPurchaserIndividualView,
    formProvider: IsPurchaserIndividualFormProvider,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form: Form[Boolean] = formProvider()
    val preparedForm = request.userAnswers.get(IsPurchaserIndividualPage) match {
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
              request.userAnswers.set(IsPurchaserIndividualPage, value)
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.scalabuild.routes.AdditionalPropAndReplaceController.onPageLoad().url)
      )
  }
}
