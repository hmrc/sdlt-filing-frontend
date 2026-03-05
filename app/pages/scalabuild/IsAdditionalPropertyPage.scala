/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.propertyDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object IsAdditionalPropertyPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ propertyDetails \toString

  override def toString: String = "twoOrMoreProperties"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value
      .map {
        case false =>
          // todo if (for ftb dates?) case here when implementing check mode
          for {
            replaceMainRes <- userAnswers.remove(ReplaceMainResidencePage)
            mainRes <- replaceMainRes.remove(MainResidencePage)
            ownedOther <- mainRes.remove(OwnsOtherPropertiesPage)
          } yield {
            ownedOther
          }
        case true =>
          for {
            mainRes <- userAnswers.remove(MainResidencePage)
            ownedOther <- mainRes.remove(OwnsOtherPropertiesPage)
          } yield {
            ownedOther
          }
        case _ => super.cleanup(value, userAnswers)
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }
}