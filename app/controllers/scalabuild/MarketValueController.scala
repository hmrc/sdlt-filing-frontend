/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.MarketValueFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import models.scalabuild.MarketValue
import pages.scalabuild.{EffectiveDatePage, MarketValuePage, PremiumPage}
import play.api.i18n.I18nSupport
import repositories.SessionRepository
import services.scalabuild.FtbLimitService
import views.html.scalabuild.MarketValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MarketValueController @Inject()(
                                       val controllerComponents: MessagesControllerComponents,
                                       view: MarketValueView,
                                       formProvider: MarketValueFormProvider,
                                       service: FtbLimitService,
                                       sessionRepository: SessionRepository,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       identify: IdentifierAction
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => result,
        effectiveDate => {
          val maxValue = BigDecimal(service.ftbLimit(effectiveDate))
          val form: Form[MarketValue] = formProvider(maxValue)
          val preparedForm = for {
            marketValueChoice <- request.userAnswers.get(MarketValuePage)
            premium <- request.userAnswers.get(PremiumPage)
          } yield MarketValue.fromUserAnswers(marketValueChoice, premium)
          Ok(view(preparedForm.fold(form)(form.fill)))
        }
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(EffectiveDatePage).toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
      .fold(
        result => Future.successful(result),
        effectiveDate => {
          val maxValue = BigDecimal(service.ftbLimit(effectiveDate))
          val form: Form[MarketValue] = formProvider(maxValue)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors))),
              marketValue =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(MarketValuePage, marketValue.value))
                  updatedAnswersWithPremium <- Future.fromTry(updatedAnswers.set(PremiumPage, marketValue.premium))
                  _ <- sessionRepository.set(updatedAnswersWithPremium)
                }
                yield Redirect(controllers.scalabuild.routes.MarketValueController.onPageLoad().url)
            )
        }
      )
  }
}