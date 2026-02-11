/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.RentFormProvider
import models.scalabuild.{LeaseContext, LeaseContextBuilder, RentPeriods, UserAnswers}
import navigation.scalabuild.Navigator
import pages.scalabuild.{EffectiveDatePage, LeaseDatesPage, LeaseTermPage, RentPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.RentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                leaseContextBuilder: LeaseContextBuilder,
                                view: RentView,
                                formProvider: RentFormProvider,
                                sessionRepository: SessionRepository,
                                navigator: Navigator,
                                getData: DataRetrievalAction,
                                requireData: DataRequiredAction,
                                identify: IdentifierAction
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    leaseContext(request.userAnswers) match {
      case Left(journeyRecovery) => journeyRecovery
      case Right(leaseCtx) =>
        val form: Form[RentPeriods] = formProvider()
        val preparedForm = request.userAnswers.get(RentPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, leaseCtx.periodCount))
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    leaseContext(request.userAnswers) match {
      case Left(journeyRecovery) => Future.successful(journeyRecovery)
      case Right(leaseCtx) =>
        val form: Form[RentPeriods] = formProvider()
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, leaseCtx.periodCount))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RentPage, value))
                userAnswersWithLeaseTerm <- Future.fromTry(updatedAnswers.set(LeaseTermPage, leaseCtx.term))
                _ <- sessionRepository.set(userAnswersWithLeaseTerm)
              } yield {
                Redirect(navigator.nextPage(RentPage, userAnswersWithLeaseTerm)) }
          )
    }
  }

  private def leaseContext(userAnswers: UserAnswers): Either[Result, LeaseContext] = {
    for {
      effectiveDate <- userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      leaseDates <- userAnswers.get(LeaseDatesPage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
    } yield leaseContextBuilder.build(
      effectiveDate = effectiveDate,
      leaseStart = leaseDates.startDate,
      leaseEnd = leaseDates.endDate
    )
  }
}