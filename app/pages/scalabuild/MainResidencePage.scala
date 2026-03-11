/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object MainResidencePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "mainResidence"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value
      .map {
        case false =>
          for {
            replaceMain <- userAnswers.remove(ReplaceMainResidencePage)
            sharedOwnership <- replaceMain.remove(SharedOwnershipPage)
            currentValue <- sharedOwnership.remove(CurrentValuePage)
            allUnusedAnswersRemoved <- currentValue.remove(MarketValuePage)
          } yield allUnusedAnswersRemoved

        case _ => super.cleanup(value, userAnswers)
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }

}
