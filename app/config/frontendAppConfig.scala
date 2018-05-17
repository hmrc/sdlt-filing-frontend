package config

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.{ServicesConfig, AssetsConfig}

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val optimizelyId: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val assetsConfig: AssetsConfig
  val urBannerLink: String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost = configuration.getString(s"$env.contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "SDLTC"

  override implicit lazy val assetsConfig =  new AssetsConfig {
    override lazy val assetsUrl = loadConfig(s"$env.assets.url")
    override lazy val assetsVersion = loadConfig(s"$env.assets.version")
    override lazy val assetsPrefix = loadConfig(s"$env.assets.url") + loadConfig(s"$env.assets.version")
  }

  override lazy val analyticsToken = loadConfig(s"$env.google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"$env.google-analytics.host")
  override lazy val optimizelyId = loadConfig(s"$env.optimizely.projectId")
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  override lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=SDLT_results&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=115"
}
