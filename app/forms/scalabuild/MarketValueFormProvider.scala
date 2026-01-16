/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.{MarketValue, MarketValueChoice}
import models.scalabuild.MarketValueChoice.{PayInStages, PayUpfront}
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

class MarketValueFormProvider extends Mappings {

  def apply(maxValue: BigDecimal): Form[MarketValue] = {
    Form(
      mapping(
        "value" -> enumerable[MarketValueChoice]("marketValue.error.required"),
        "paySDLTUpfront" -> mandatoryIfEqual(
                              fieldName = "value",
                              value = PayUpfront.toString,
                              currency(
                                "marketValue.error.required",
                                "marketValue.error.nonNumeric",
                                "marketValue.error.nonNumeric"
                              ).verifying(maxValueContraints(maxValue))
                           ),
        "marketPropValue" -> mandatoryIfEqual(
                              fieldName = "value",
                              value = PayInStages.toString,
                              currency(
                                "marketValue.error.required",
                                "marketValue.error.nonNumeric",
                                "marketValue.error.nonNumeric"
                              ).verifying(maxValueContraints(maxValue))
                          )
        )(MarketValue.apply)(MarketValue.unapply)
    )
  }

  private def maxValueContraints(max: BigDecimal) =
    inRange(BigDecimal(0), max, "marketValue.error.maxValue")
}
