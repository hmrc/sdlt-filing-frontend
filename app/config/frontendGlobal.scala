package config


import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Mode.Mode
import play.api._
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters._

trait RunModeConfig {
  def runModeConfiguration: Configuration = Play.current.configuration
  def mode: Mode = Play.current.mode
}

object FrontendGlobal
  extends DefaultFrontendGlobal {

  override val auditConnector      = FrontendAuditConnector
  override val loggingFilter       = LoggingFilter
  override val frontendAuditFilter = AuditFilter

  override def onStart(app: Application) {
    super.onStart(app)
    applicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")

  override def defaultFrontendFilters = Seq(
    HeadersFilter,
    loggingFilter,
    CacheControlFilter.fromConfig("caching.allowedContentTypes"),
    frontendAuditFilter,
    metricsFilter
  )
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs: Config = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with AppName with MicroserviceFilterSupport {
  override lazy val maskedFormFields = Seq("password")

  override lazy val applicationPort = None

  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}