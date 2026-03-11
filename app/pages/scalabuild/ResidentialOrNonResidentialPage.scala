/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import models.scalabuild.PropertyType.NonResidential
import models.scalabuild.{PropertyType, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try


case object ResidentialOrNonResidentialPage extends QuestionPage[PropertyType] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "propertyType"

  override def cleanup(value: Option[PropertyType], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case NonResidential =>
        for {
          nonUk <- userAnswers.remove(NonUkResidentPage)
          mainRes <- nonUk.remove(MainResidencePage)
          replaceMain <- mainRes.remove(ReplaceMainResidencePage)
          ownsOtherProp <- replaceMain.remove(OwnsOtherPropertiesPage)
          sharedOwnership <- ownsOtherProp.remove(SharedOwnershipPage)
          marketValue <- sharedOwnership.remove(MarketValuePage)
          individual <- marketValue.remove(IsPurchaserIndividualPage)
          propertyDetails <- individual.remove(PropertyDetailsPage)
          unusedJourneyFieldsRemoved <- propertyDetails.remove(CurrentValuePage)
        } yield unusedJourneyFieldsRemoved
      case _ => super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}
