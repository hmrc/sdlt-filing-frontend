/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.{ContractPost201603FormProvider, ExchangeContractsFormProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ExchangeContractsPreAndPostView

import javax.inject.Inject
import scala.concurrent.Future

class ExchangeContractsPreAndPostController @Inject()(
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ExchangeContractsPreAndPostView,
                                                       exchangeContractsFormProvider: ExchangeContractsFormProvider,
                                                       contractPost201603FormProvider: ContractPost201603FormProvider,
                                                     ) extends FrontendBaseController {
  val exchangeContractsForm = exchangeContractsFormProvider()
  val contractPostForm = contractPost201603FormProvider()
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(exchangeContractsForm, contractPostForm))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    exchangeContractsForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
        { case true =>
            contractPostForm
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
                _ => Future.successful(Redirect(controllers.scalabuild.routes.ExchangeContractsPreAndPostController.onPageLoad().url)))
        case false => Future.successful(Redirect(controllers.scalabuild.routes.ExchangeContractsPreAndPostController.onPageLoad().url))
        }
      )
  }
}