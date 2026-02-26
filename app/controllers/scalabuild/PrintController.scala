/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.{PrintDisplayTable, UserAnswers}
import pages.scalabuild.{HoldingPage, RequestGroup}
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
                  summaryList = toSummaryList(userAnswers, withAction = false),
                  resultsTables = value
                )
              )
            )
        }
    }

  }

  private def toSummaryList(ua: UserAnswers, withAction: Boolean)(implicit messages: Messages) = {
    val premiumOrPriceSummaryRow = ua.get(HoldingPage) match {
      case Some(Freehold) => PurchasePriceSummary.row(ua, withAction = withAction)
      case Some(Leasehold) => PremiumSummary.row(ua, withAction = withAction)
      case None        => None
    }

    SummaryListViewModel(
      rows = Seq(
        HoldingSummary.row(ua, withAction = withAction),
        PropertySummary.row(ua, withAction = withAction),
        EffectiveDateSummary.row(ua, withAction = withAction),
        IsAdditionalPropertySummary.row(ua, withAction = withAction),
        MainResidenceSummary.row(ua, withAction = withAction),
        NonUkResidentSummary.row(ua, withAction = withAction),
        OwnsOtherPropertiesSummary.row(ua, withAction = withAction),
        premiumOrPriceSummaryRow,
        IsPurchaserIndividualSummary.row(ua, withAction = withAction),
        ReplaceMainResidenceSummary.row(ua, withAction = withAction),
        SharedOwnershipSummary.row(ua, withAction = withAction),
        CurrentValueSummary.row(ua, withAction = withAction),
        PaySdltSummary.row(ua, withAction = withAction),
        LeaseStartDateSummary.row(ua, withAction = withAction),
        LeaseEndDateSummary.row(ua, withAction = withAction),
        LeaseTermSummary.row(ua),
        Year1RentSummary.row(ua, withAction = withAction),
        Year2RentSummary.row(ua, withAction = withAction),
        Year3RentSummary.row(ua, withAction = withAction),
        Year4RentSummary.row(ua, withAction = withAction),
        Year5RentSummary.row(ua, withAction = withAction),
        HighestRentSummary.row(ua),
        ExchangeContractPreMarch2016Summary.row(ua, withAction = withAction),
        ContractPostMarch2016Summary.row(ua, withAction = withAction),
        RelevantRentSummary.row(ua, withAction = withAction)
      ).flatten
    )
  }
}
