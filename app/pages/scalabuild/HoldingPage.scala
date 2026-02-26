/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.{HoldingTypes, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object HoldingPage extends QuestionPage[HoldingTypes] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "holdingType"

  override def cleanup(value: Option[HoldingTypes], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case Freehold =>
        for {
          leaseTerm <- userAnswers.remove(LeaseTermPage)
          leaseDates <- leaseTerm.remove(LeaseDatesPage)
          rentPage <- leaseDates.remove(RentPage)
          currentValue <- rentPage.remove(CurrentValuePage)
          marketValue <- currentValue.remove(MarketValuePage)
          premium <- marketValue.remove(RentPage)
        } yield premium

      case Leasehold =>
        for {
          purchasePrice <- userAnswers.remove(PurchasePricePage)
        } yield purchasePrice

      case _ => super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}