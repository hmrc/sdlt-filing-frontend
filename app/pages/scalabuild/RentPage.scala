/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.leaseDetails
import models.scalabuild.{RentPeriods, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object RentPage extends QuestionPage[RentPeriods] {

  override def path: JsPath = JsPath \ leaseDetails \ toString

  override def toString: String = "rentDetails"
  override def cleanup(value: Option[RentPeriods], userAnswers: UserAnswers): Try[UserAnswers] = {
    val oDeleteAnswers = for {
      premium <- userAnswers.get(PremiumPage)
      premiumLessThanThreshold = premium < 150000
      rents <- value
      highestRentLessThan2000 = rents.rents.max < 2000
    } yield (premiumLessThanThreshold && highestRentLessThan2000)

    oDeleteAnswers.map(deleteAnswers =>
      if (!deleteAnswers)  {
        for {
          contractPre <- userAnswers.remove(ExchangeContractsPage)
          variedPost <- contractPre.remove(ContractPost201603Page)
          relevantRent <- variedPost.remove(RelevantRentPage)
        } yield relevantRent
      } else Try(userAnswers)
    ) .getOrElse(super.cleanup(value, userAnswers))
  }
}