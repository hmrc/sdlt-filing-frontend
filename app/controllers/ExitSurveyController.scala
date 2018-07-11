package controllers

import javax.inject.Inject
import config.FrontendAuditConnector
import models.ExitSurveyModel
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.audit.AuditExtensions._


class ExitSurveyController @Inject()(auditConnector: FrontendAuditConnector) extends FrontendController {

  val onSubmit: Action[AnyContent] = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        val model = json.validate[ExitSurveyModel]

        model.fold(
          invalid = { fieldErrors =>
            BadRequest(s"Invalid Json received: $fieldErrors")
          },
          valid = {
            case ExitSurveyModel(None, None) => Ok("Empty Survey received")
            case ExitSurveyModel(_, _) =>
              sendAuditEvent(model.get)
              Ok("Completed Survey")
          }
        )

      case None =>
        Logger.error("No JSON received.")
        BadRequest("No Json received")
    }
  }

  def sendAuditEvent(model: ExitSurveyModel)(implicit headerCarrier: HeaderCarrier): Unit = {
    val data = DataEvent(
      auditSource = "sdltc-frontend",
      auditType = "SDLTC-ExitSurvey",
      detail = Map("radioFeedback" -> model.radioFeedback.getOrElse(""), "textFeedback" -> model.textFeedback.getOrElse("")),
      tags = implicitly[HeaderCarrier].toAuditTags("", "N/A")
    )

    auditConnector.sendEvent(data).onFailure {
      case e: Throwable => Logger.error(s"[ExitSurveyController][post] ${e.getMessage}", e)
    }
  }
}

