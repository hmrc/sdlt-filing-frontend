/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.{PrintDisplayTable, UserAnswers}
import pages.scalabuild.{CurrentValuePage, EffectiveDatePage, HoldingPage, RequestGroup}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.scalabuild.{FtbLimitService, ResultService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.scalabuild.checkanswerssummary._
import viewmodels.scalabuild.govuk.summarylist.SummaryListViewModel
import views.html.scalabuild.PrintView

import javax.inject.{Inject, Singleton}

@Singleton
class PrintController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    service: FtbLimitService,
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

  private def toSummaryList(userAnswers: UserAnswers, withAction: Boolean)(implicit messages: Messages) = {
    val premiumOrPriceSummaryRow = userAnswers.get(HoldingPage) match {
      case Some(Freehold) => PurchasePriceSummary.row(userAnswers, withAction = withAction)
      case Some(Leasehold) => PremiumSummary.row(userAnswers, withAction = withAction)
      case None        => None
    }
    val threshold: Option[(Int, Boolean)] =
      for {
        effectiveDate <- userAnswers.get(EffectiveDatePage)
        currentValue <- userAnswers.get(CurrentValuePage)
        threshold = service.ftbLimit(effectiveDate)
      } yield (threshold, currentValue)

    SummaryListViewModel(
      rows = Seq(
        HoldingSummary.row(userAnswers, withAction = withAction),
        PropertySummary.row(userAnswers, withAction = withAction),
        EffectiveDateSummary.row(userAnswers, withAction = withAction),
        NonUkResidentSummary.row(userAnswers, withAction = withAction),
        IsPurchaserIndividualSummary.row(userAnswers, withAction = withAction),
        IsAdditionalPropertySummary.row(userAnswers, withAction = withAction),
        OwnsOtherPropertiesSummary.row(userAnswers, withAction = withAction),
        MainResidenceSummary.row(userAnswers, withAction = withAction),
        ReplaceMainResidenceSummary.row(userAnswers, withAction = withAction),
        SharedOwnershipSummary.row(userAnswers, withAction = withAction),
        CurrentValueSummary.row(userAnswers, withAction = withAction, threshold),
        PaySdltSummary.row(userAnswers, withAction = withAction),
        LeaseStartDateSummary.row(userAnswers, withAction = withAction),
        LeaseEndDateSummary.row(userAnswers, withAction = withAction),
        LeaseTermSummary.row(userAnswers),
        premiumOrPriceSummaryRow,
        Year1RentSummary.row(userAnswers, withAction = withAction),
        Year2RentSummary.row(userAnswers, withAction = withAction),
        Year3RentSummary.row(userAnswers, withAction = withAction),
        Year4RentSummary.row(userAnswers, withAction = withAction),
        Year5RentSummary.row(userAnswers, withAction = withAction),
        HighestRentSummary.row(userAnswers),
        ExchangeContractPreMarch2016Summary.row(userAnswers, withAction = withAction),
        ContractPostMarch2016Summary.row(userAnswers, withAction = withAction),
        RelevantRentSummary.row(userAnswers, withAction = withAction)
      ).flatten
    )
  }
}
