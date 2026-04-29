/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.taxCalculation

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.taxCalculation.PremiumPayableFormProvider
import navigation.scalabuild.Navigator
import pages.scalabuild.taxCalculation.PremiumPayablePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import views.html.scalabuild.taxCalculation.PremiumPayableView

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PremiumPayableController @Inject() (
                                          val controllerComponents: MessagesControllerComponents,
                                          navigator: Navigator,
                                          view: PremiumPayableView,
                                          formProvider: PremiumPayableFormProvider,
                                          sessionRepository: SessionRepository,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          identify: IdentifierAction
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form: Form[String] = formProvider()
    val preparedForm = request.userAnswers.get(PremiumPayablePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val form: Form[String] = formProvider()
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.set(PremiumPayablePage, value)
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PremiumPayablePage, updatedAnswers))
      )
  }
}

