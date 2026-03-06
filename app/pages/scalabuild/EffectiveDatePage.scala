/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import data.Dates.{END_OF_MARCH_2016, MAR2021_RESIDENTIAL_DATE}
import models.scalabuild.UserAnswers
import play.api.libs.json.JsPath
import utils.CalculationUtils.DateHelper

import java.time.LocalDate
import scala.util.Try

case object EffectiveDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "effectiveDate"

  override def cleanup(value: Option[LocalDate], userAnswers: UserAnswers): Try[UserAnswers] = {

    value
      .map { date: LocalDate =>
        val beforeApril2016 = if (date.onOrBefore(END_OF_MARCH_2016)) true else false
        val beforeApril2021 = if (date.onOrBefore(MAR2021_RESIDENTIAL_DATE)) true else false
        if (beforeApril2016) {
          for {
            nonUk <- userAnswers.remove(NonUkResidentPage)
            individual <- nonUk.remove(IsPurchaserIndividualPage)
            propertyDetails <- individual.remove(PropertyDetailsPage)
            addProp <- propertyDetails.remove(IsAdditionalPropertyPage)
            replaceMain <- addProp.remove(ReplaceMainResidencePage)
          } yield replaceMain
        } else if (beforeApril2021) {
          userAnswers.remove(NonUkResidentPage)
        } else {
          super.cleanup(value, userAnswers)
        }
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }
}
