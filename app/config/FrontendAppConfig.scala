/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package config

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


@Singleton
class FrontendAppConfig @Inject()(config: ServicesConfig) {

  private def loadConfig(key: String) = config.getString(key)

  val contactFormServiceIdentifier = "SDLTC"
  private lazy val contactHost = config.getString("contact-frontend.host")

  private lazy val assetsUrl     = config.getString("assets.url")
  private lazy val assetsVersion = config.getString("assets.version")
  lazy val assetsPrefix: String  = assetsUrl + assetsVersion

  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val feedbackSurveyUrl: String = loadConfig(s"feedback-survey-frontend.url")

  lazy val cookies: String = loadConfig("urls.footer.cookies")
  lazy val accessibilityStatement: String = loadConfig("urls.footer.accessibility_statement")
  lazy val privacy: String = loadConfig("urls.footer.privacy_policy")
  lazy val termsConditions: String = loadConfig("urls.footer.terms_and_conditions")
  lazy val govukHelp: String = loadConfig("urls.footer.help_page")

}
