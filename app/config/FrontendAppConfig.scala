package config

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.AssetsConfig

@Singleton
class FrontendAppConfig @Inject()(env: Environment, val runModeConfiguration: Configuration) {

  val mode: Mode = env.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactFormServiceIdentifier = "SDLTC"
  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")

  implicit lazy val assetsConfig =  new AssetsConfig(runModeConfiguration) {
    override val assetsPrefix = loadConfig("assets.url") + loadConfig("assets.version")
  }

  lazy val analyticsToken: String = loadConfig("google-analytics.token")
  lazy val analyticsHost: String = loadConfig("google-analytics.host")
  lazy val optimizelyId: String = loadConfig("optimizely.projectId")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=SDLT_results&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=115"
  lazy val feedbackSurveyUrl: String = loadConfig(s"feedback-survey-frontend.url")
}
