/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


@Singleton
class FrontendAppConfig @Inject()(config: ServicesConfig) {

  private def loadConfig(key: String) = config.getString(key)

  private val contactFormServiceIdentifier = "SDLTC"
  private lazy val contactHost = config.getString("contact-frontend.host")

  private lazy val assetsUrl     = config.getString("assets.url")
  private lazy val assetsVersion = config.getString("assets.version")
  lazy val assetsPrefix: String  = assetsUrl + assetsVersion

  lazy val analyticsToken: String = loadConfig("google-analytics.token")
  lazy val analyticsHost: String = loadConfig("google-analytics.host")
  lazy val optimizelyId: String = loadConfig("optimizely.projectId")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=SDLT_results&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=115"
  lazy val feedbackSurveyUrl: String = loadConfig(s"feedback-survey-frontend.url")
  lazy val googleTagManagerId = loadConfig(s"google-tag-manager.id")

}
