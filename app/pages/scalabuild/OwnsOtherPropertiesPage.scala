/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object OwnsOtherPropertiesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ownedOtherProperties"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case false =>
        for {
          replaceMain <- userAnswers.remove(ReplaceMainResidencePage)
        } yield replaceMain

      case true =>
        for {
          replaceMain <- userAnswers.remove(ReplaceMainResidencePage)
          main <- replaceMain.remove(MainResidencePage)
          sharedOwnership <- main.remove(SharedOwnershipPage)
          currentValue <- sharedOwnership.remove(CurrentValuePage)
          allUnusedPathsRemoved <- currentValue.remove(MarketValuePage)
        } yield allUnusedPathsRemoved
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}