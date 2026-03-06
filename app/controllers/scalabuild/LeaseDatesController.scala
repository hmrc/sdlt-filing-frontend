/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.LeaseDatesFormProvider
import models.scalabuild.LeaseDates
import navigation.scalabuild.Navigator
import pages.scalabuild.{EffectiveDatePage, LeaseDatesPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.scalabuild.LeaseTermService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.LeaseDatesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeaseDatesController @Inject()(
                                      val controllerComponents: MessagesControllerComponents,
                                      view: LeaseDatesView,
                                      formProvider: LeaseDatesFormProvider,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                      leaseTermService: LeaseTermService,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      identify: IdentifierAction
                                    )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => result,
        effectiveDate => {
          val form = formProvider(effectiveDate)
          val preparedForm = request.userAnswers.get(LeaseDatesPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm))
        }
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => Future.successful(result),
        effectiveDate => {
          val form: Form[LeaseDates] = formProvider(effectiveDate)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseDatesPage, value))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield {
                  Redirect(navigator.nextPage(LeaseDatesPage, updatedAnswers)) }
            )
        }
      )
  }
// todo: Implement for check mode

//  private def calculateLeaseTerm(userAnswers: UserAnswers): Option[LeaseTerm] = {
//    for {
//      effectiveDate <- userAnswers.get(EffectiveDatePage)
//      leaseDates <- userAnswers.get(LeaseDatesPage)
//    } yield leaseTermService.calculateTermOfLease(
//      effectiveDate = effectiveDate,
//      leaseStart = leaseDates.startDate,
//      leaseEnd = leaseDates.endDate
//    )
//  }
}
