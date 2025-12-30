/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config.scalabuild

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject()(config: ServicesConfig, configuration: Configuration) {
  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInMinutes")
}
