/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.SharedOwnershipFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.SharedOwnershipPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import views.html.scalabuild.SharedOwnershipView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SharedOwnershipController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           view: SharedOwnershipView,
                                           formProvider: SharedOwnershipFormProvider,
                                           sessionRepository: SessionRepository,
                                           navigator: Navigator,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           identify: IdentifierAction
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(SharedOwnershipPage).fold(form)(value => form.fill(value))
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value => for {
          updatedAnswers <- Future
            .fromTry(request.userAnswers.set(SharedOwnershipPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(SharedOwnershipPage, updatedAnswers))
      )

  }
}