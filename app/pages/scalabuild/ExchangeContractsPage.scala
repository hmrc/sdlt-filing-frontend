/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import models.scalabuild.PageConstants.relevantRentDetails
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import scala.util.Try

case object ExchangeContractsPage extends QuestionPage[Boolean]{

  override def path: JsPath = JsPath \ relevantRentDetails \ toString

  override def toString: String = "exchangedContractsBeforeMar16"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case false => userAnswers.remove(ContractPost201603Page)
      case _ => super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
  }

}