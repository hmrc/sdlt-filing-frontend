/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import config.FrontendAppConfig
import forms.scalabuild.mappings.Mappings
import models.scalabuild.{MarketValue, MarketValueChoice}
import models.scalabuild.MarketValueChoice.{PayInStages, PayUpfront}
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

import javax.inject.Inject


class MarketValueFormProvider @Inject()(appConfig: FrontendAppConfig) extends Mappings {

  def apply(isHigherFtbLimit: Boolean): Form[MarketValue] = {

    val maxValue = if(isHigherFtbLimit) BigDecimal(appConfig.highValue) else BigDecimal(appConfig.lowValue)

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
