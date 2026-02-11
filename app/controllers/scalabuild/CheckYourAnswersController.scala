/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.UserAnswers
import models.scalabuild.requests.DataRequest
import pages.scalabuild.RequestGroup
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
    view: CheckYourAnswersView,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request: DataRequest[AnyContent] =>
      request.userAnswers
        .get(RequestGroup)
        .fold {
          logger.error("Couldn't make RequestFromMongo out of requestGroup")
          Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad())
        } { _ =>
          val list = toSummaryList((request.userAnswers))
          Ok(view(list))
        }

  }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      Future(
        Redirect(controllers.scalabuild.routes.ResultController.onPageLoad())
      )
    }

  private def toSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(
      rows = StartAgainActionSummaryRow.row() +: Seq(
        HoldingSummary.row(userAnswers, withAction = true),
        PropertySummary.row(userAnswers, withAction = true),
        EffectiveDateSummary.row(userAnswers, withAction = true),
        IsAdditionalPropertySummary.row(userAnswers, withAction = true),
        MainResidenceSummary.row(userAnswers, withAction = true),
        NonUkResidentSummary.row(userAnswers, withAction = true),
        OwnsOtherPropertiesSummary.row(userAnswers, withAction = true),
        PurchasePriceSummary.row(userAnswers, withAction = true),
        PurchaserSummary.row(userAnswers, withAction = true)
      ).flatten
    )
  }
}
