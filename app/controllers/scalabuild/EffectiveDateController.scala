/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.EffectiveDateFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.{EffectiveDatePage, ResidentialOrNonResidentialPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.EffectiveDateView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EffectiveDateController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: EffectiveDateView,
    formProvider: EffectiveDateFormProvider,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(ResidentialOrNonResidentialPage)
      .fold(Future.successful(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))) {
        PropertyType =>
          val form = formProvider(PropertyType)
          val dateFromForm = request.userAnswers.get(EffectiveDatePage)
          val preparedForm = dateFromForm match {
            case None        => form
            case Some(value) => form.fillAndValidate(value)
          }
          Future.successful(Ok(view(preparedForm)))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(ResidentialOrNonResidentialPage)
      .fold(Future.successful(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))) {
        PropertyType =>
        val form: Form[LocalDate] = formProvider(PropertyType)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future
              .fromTry(request.userAnswers.set(EffectiveDatePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EffectiveDatePage, updatedAnswers))
      )}
  }
}
