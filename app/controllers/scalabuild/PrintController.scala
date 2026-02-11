/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.{PrintDisplayTable, UserAnswers}
import pages.scalabuild.RequestGroup
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.scalabuild.ResultService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.scalabuild.checkanswerssummary._
import viewmodels.scalabuild.govuk.summarylist.SummaryListViewModel
import views.html.scalabuild.PrintView

import javax.inject.{Inject, Singleton}

@Singleton
class PrintController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: PrintView,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction,
    resultService: ResultService
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers

    userAnswers.get(RequestGroup).fold(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad())) {
      requestFromMongo =>
        resultService.getResultDisplayTableList(userAnswers, requestFromMongo) match {
          case Left(error) =>
            logger.error(s"$error")
            Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad())
          case Right(value) =>
            Ok(
              view(
                PrintDisplayTable(
                  summaryList = toSummaryList(userAnswers),
                  resultsTables = value
                )
              )
            )
        }
    }

  }

  private def toSummaryList(ua: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(
      rows = Seq(
        HoldingSummary.row(ua, withAction = false),
        PropertySummary.row(ua, withAction = false),
        EffectiveDateSummary.row(ua, withAction = false),
        IsAdditionalPropertySummary.row(ua, withAction = false),
        MainResidenceSummary.row(ua, withAction = false),
        NonUkResidentSummary.row(ua, withAction = false),
        OwnsOtherPropertiesSummary.row(ua, withAction = false),
        PurchasePriceSummary.row(ua, withAction = false),
        PurchaserSummary.row(ua, withAction = false)
      ).flatten
    )
  }
}
