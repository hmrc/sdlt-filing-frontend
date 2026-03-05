/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import data.Dates.END_OF_MARCH_2016
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.{Success, Try}

case object EffectiveDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "effectiveDate"

  override def cleanup(value: Option[LocalDate], userAnswers: UserAnswers): Try[UserAnswers] = {

    value.map { date: LocalDate =>
      val afterApril2016 = if (date.isAfter(END_OF_MARCH_2016)) true else false
          if (afterApril2016) {
            Success(userAnswers)
          } else {
            for {
              individual <- userAnswers.remove(IsPurchaserIndividualPage)
              propertyDetails <- individual.remove(PropertyDetailsPage)
              addProp <- propertyDetails.remove(IsAdditionalPropertyPage)
              replaceMain <- addProp.remove(ReplaceMainResidencePage)
            } yield replaceMain
          }
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}

