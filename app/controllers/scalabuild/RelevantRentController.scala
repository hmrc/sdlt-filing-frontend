/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.RelevantRentFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.RelevantRentPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.RelevantRentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RelevantRentController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        view: RelevantRentView,
                                        formProvider: RelevantRentFormProvider,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        identify: IdentifierAction
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(RelevantRentPage).fold(form)(value => form.fill(value))
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value => for {
          updatedAnswers <- Future
            .fromTry(request.userAnswers.set(RelevantRentPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(RelevantRentPage, updatedAnswers))
      )
  }
}