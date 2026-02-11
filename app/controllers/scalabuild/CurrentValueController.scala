/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.CurrentValueFormProvider
import models.scalabuild.CurrentValue
import navigation.scalabuild.Navigator
import pages.scalabuild.{CurrentValuePage, EffectiveDatePage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.scalabuild.FtbLimitService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.CurrentValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentValueController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CurrentValueView,
                                        formProvider: CurrentValueFormProvider,
                                        service: FtbLimitService,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        identify: IdentifierAction
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[CurrentValue] = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => result,
        effectiveDate => {
          val ftbLimitValue = service.ftbLimit(effectiveDate)
          val preparedForm = request.userAnswers.get(CurrentValuePage) match {
            case None => form
            case Some(value) => form.fill(CurrentValue.fromBoolean(value))
          }
          Ok(view(preparedForm, ftbLimitValue))
        }
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => Future.successful(result),
        effectiveDate => {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                val ftbLimitValue = service.ftbLimit(effectiveDate)
                Future.successful(BadRequest(view(formWithErrors, ftbLimitValue)))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(CurrentValuePage, value.asBoolean))
                  _ <- sessionRepository.set(updatedAnswers)
                }
                yield Redirect(navigator.nextPage(CurrentValuePage, updatedAnswers))
            )
        }
      )
  }

}