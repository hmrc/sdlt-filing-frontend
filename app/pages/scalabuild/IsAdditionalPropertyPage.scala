/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import data.Dates.{JULY2020_RESIDENTIAL_DATE, JUNE2021_RESIDENTIAL_DATE, NOV2017_RESIDENTIAL_DATE}
import models.scalabuild.PageConstants.propertyDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath
import utils.CalculationUtils.DateHelper

import scala.util.Try

case object IsAdditionalPropertyPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ propertyDetails \ toString

  override def toString: String = "twoOrMoreProperties"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {

    val valueDateTuple = for {
      date <- userAnswers.get(EffectiveDatePage)
      withinDateBounderies: Boolean = date.onOrAfter(NOV2017_RESIDENTIAL_DATE) && date
        .isBefore(JULY2020_RESIDENTIAL_DATE) | date.isAfter(JUNE2021_RESIDENTIAL_DATE)
      addProp <- value
    } yield (addProp, withinDateBounderies)
    valueDateTuple match {
      case Some((false, false)) =>
        for {
          replaceMainRes <- userAnswers.remove(ReplaceMainResidencePage)
          ownedOther <- replaceMainRes.remove(OwnsOtherPropertiesPage)
          main <- ownedOther.remove(MainResidencePage)
          sharedOwnership <- main.remove(SharedOwnershipPage)
          currentValue <- sharedOwnership.remove(CurrentValuePage)
          allUnusedPathsRemoved <- currentValue.remove(MarketValuePage)
        } yield allUnusedPathsRemoved
      case Some((false, true)) =>
        for {
          replaceMainRes <- userAnswers.remove(ReplaceMainResidencePage)
        } yield replaceMainRes
      case Some((true, false)) =>
        for {
          mainRes <- userAnswers.remove(MainResidencePage)
          ownedOther <- mainRes.remove(OwnsOtherPropertiesPage)
          sharedOwnership <- ownedOther.remove(SharedOwnershipPage)
          currentValue <- sharedOwnership.remove(CurrentValuePage)
          allUnusedPathsRemoved <- currentValue.remove(MarketValuePage)
        } yield allUnusedPathsRemoved
      case Some((true, true)) =>
        for {
          mainRes <- userAnswers.remove(MainResidencePage)
          ownedOther <- mainRes.remove(OwnsOtherPropertiesPage)
          sharedOwnership <- ownedOther.remove(SharedOwnershipPage)
          currentValue <- sharedOwnership.remove(CurrentValuePage)
          allUnusedPathsRemoved <- currentValue.remove(MarketValuePage)
        } yield allUnusedPathsRemoved
      case _ => super.cleanup(value, userAnswers)
    }
  }
}
