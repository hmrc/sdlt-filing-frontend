/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject()(config: ServicesConfig, configuration: Configuration) {
  val ftbStartDate: LocalDate = LocalDate.parse(configuration.get[String]("ftb-limit.startDate"))
  val ftbEndDate: LocalDate = LocalDate.parse(configuration.get[String]("ftb-limit.endDate"))
  val highValue: Int = configuration.get[Int]("ftb-limit.highValue")
  val lowValue: Int = configuration.get[Int]("ftb-limit.lowValue")
  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInMinutes")
}
