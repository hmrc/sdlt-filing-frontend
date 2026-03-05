/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.propertyDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object IsPurchaserIndividualPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ propertyDetails \toString

  override def toString: String = "individual"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case false =>
        for {
          replaceMain <- userAnswers.remove(ReplaceMainResidencePage)
          main <- replaceMain.remove(MainResidencePage)
          sharedOwnership <- main.remove(SharedOwnershipPage)
          twoOrMore <- sharedOwnership.remove(IsAdditionalPropertyPage)
          ownedOther <- twoOrMore.remove(OwnsOtherPropertiesPage)
        } yield ownedOther

      case _ => super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}