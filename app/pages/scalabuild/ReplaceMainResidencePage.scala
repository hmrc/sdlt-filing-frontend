/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.propertyDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object ReplaceMainResidencePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ propertyDetails \ toString

  override def toString: String = "replaceMainResidence"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value
      .map {
        case false =>
          for {
            mainRes <- userAnswers.remove(MainResidencePage)
            sharedOwnership <- mainRes.remove(SharedOwnershipPage)
            currentValue <- sharedOwnership.remove(CurrentValuePage)
            allUnusedAnswersRemoved <- currentValue.remove(MarketValuePage)
          } yield allUnusedAnswersRemoved

        case _ => super.cleanup(value, userAnswers)
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }

}
