/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.UserAnswers
import models.scalabuild.requests.DataRequest
import pages.scalabuild.{CurrentValuePage, EffectiveDatePage, HoldingPage}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.scalabuild.FtbLimitService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.scalabuild.checkanswerssummary._
import viewmodels.scalabuild.govuk.summarylist.SummaryListViewModel
import views.html.scalabuild.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    service: FtbLimitService,
    view: CheckYourAnswersView,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request: DataRequest[AnyContent] =>
      val list = toSummaryList(request.userAnswers, withAction = true)
      Ok(view(list))
  }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      Future(
        Redirect(controllers.scalabuild.routes.ResultController.onPageLoad())
      )
    }

  private def toSummaryList(userAnswers: UserAnswers, withAction: Boolean)(implicit messages: Messages): SummaryList = {
    val premiumOrPriceSummaryRow = userAnswers.get(HoldingPage) match {
      case Some(Freehold)  => PurchasePriceSummary.row(userAnswers, withAction = withAction)
      case Some(Leasehold) => PremiumSummary.row(userAnswers, withAction = withAction)
      case None            => None
    }

    val threshold: Option[(Int, Boolean)] =
      for {
        effectiveDate <- userAnswers.get(EffectiveDatePage)
        currentValue <- userAnswers.get(CurrentValuePage)
        threshold = service.ftbLimit(effectiveDate)
      } yield (threshold, currentValue)

    SummaryListViewModel(
      rows = StartAgainActionSummaryRow.row() +: Seq(
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
