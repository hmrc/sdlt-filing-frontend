/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.scalabuild

import config.scalabuild.FrontendAppConfig

import java.time.LocalDate
import javax.inject.Inject

class FtbLimitService @Inject()(appConfig: FrontendAppConfig){
  lazy val highThreshold: Int = appConfig.highValue
  lazy val lowThreshold: Int = appConfig.lowValue

  def ftbLimit(effectiveDate: LocalDate): Int = {
    if (!effectiveDate.isBefore(appConfig.ftbStartDate) && effectiveDate.isBefore(appConfig.ftbEndDate))
      appConfig.highValue
    else
      appConfig.lowValue
  }

}
