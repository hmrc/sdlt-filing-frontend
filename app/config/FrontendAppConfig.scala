/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import play.api.Configuration

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate


@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) {
  val ftbStartDate: LocalDate = LocalDate.parse(configuration.get[String]("ftb-limit.startDate"))
  val ftbEndDate: LocalDate = LocalDate.parse(configuration.get[String]("ftb-limit.endDate"))
  val highValue: Int = configuration.get[Int]("ftb-limit.highValue")
  val lowValue: Int = configuration.get[Int]("ftb-limit.lowValue")
}
