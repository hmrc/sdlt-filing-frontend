/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.MainResidenceFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.MainResidencePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.MainResidenceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MainResidenceController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         view: MainResidenceView,
                                         formProvider: MainResidenceFormProvider,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         identify: IdentifierAction
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  val form: Form[Boolean] = formProvider()
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(MainResidencePage).fold(form)(value => form.fill(value))
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MainResidencePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(MainResidencePage, updatedAnswers))
      )
  }
}
