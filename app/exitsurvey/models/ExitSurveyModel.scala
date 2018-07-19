package exitsurvey.models

import play.api.libs.json.Json

case class ExitSurveyModel (radioFeedback: Option[String] = None,
                            textFeedback: Option[String] = None
                           )

object ExitSurveyModel {
  implicit val formats = Json.format[ExitSurveyModel]
}