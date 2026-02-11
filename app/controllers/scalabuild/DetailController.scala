/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.scalabuild.RequestGroup
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.scalabuild.DetailSummary
import views.html.scalabuild.DetailView

import javax.inject.{Inject, Singleton}

@Singleton
class DetailController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: DetailView,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction,
    calculationService: CalculationService
) extends FrontendBaseController
    with I18nSupport {
  def onPageLoad(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val taxCalculationResults = request.userAnswers
      .get(RequestGroup)
      .map(mongoRequest => calculationService.calculateTax(mongoRequest.toRequest).result(index).taxCalcs)
      .toRight(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))

    taxCalculationResults match {
      case Left(_) => Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad())
      case Right(value) =>
        val caption = value.map(_.detailHeading.getOrElse("")).head
        val detailFooter = value.map(_.detailFooter.getOrElse("")).head
        val taxDue = value.map(_.taxDue).head
        val slices = value.flatMap(_.slices.getOrElse(Seq.empty))
        val rows = DetailSummary.tableRows(slices)
        Ok(view(caption, detailFooter, taxDue, rows))
    }

  }

}
