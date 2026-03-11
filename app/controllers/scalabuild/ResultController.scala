/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.scalabuild.RequestFromMongo
import models.scalabuild.requests.DataRequest
import pages.scalabuild.RequestGroup
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.scalabuild.ResultService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ResultView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ResultController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: ResultView,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction,
    resultService: ResultService
) extends FrontendBaseController
    with I18nSupport {
  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request: DataRequest[AnyContent] =>
      request.userAnswers
        .get(RequestGroup)
        .fold(
          {
            logger.error(s"[ResultController][onPageLoad]Could not make RequestGroup From Mongo data ${request.userAnswers.data}")
            Future.successful(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
          }
        ) { requestFromMongo: RequestFromMongo =>
          resultService.getResultDisplayTableList(request.userAnswers, requestFromMongo) match {
            case Left(error) =>
              logger.error(
                s"[ResultController][onPageLoad] Could not translate userAnswers to a ResultDisplayTable - $error"
              )
              Future.successful(
                Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad())
              )

            case Right(resultDisplayTableList) =>
              Future.successful(Ok(view(resultDisplayTableList)))
          }
        }
    }
}
