/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.relevantRentDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object ContractPost201603Page extends QuestionPage[Boolean]{

  override def path: JsPath = JsPath \ relevantRentDetails \ toString

  override def toString: String = "contractChangedSinceMar16"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value
      .map {
        case true =>
          for {
            relRent <- userAnswers.remove(RelevantRentPage)
          } yield {
            relRent
          }
        case _ => super.cleanup(value, userAnswers)
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }
}