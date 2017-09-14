package config

import java.io.File

import com.kenshoo.play.metrics.MetricsFilter
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Mode.Mode
import play.api._
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.config.{ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters.{RecoveryFilter, CacheControlFilter}
import uk.gov.hmrc.play.filters.frontend.HeadersFilter
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport



object FrontendGlobal
  extends GlobalSettings
  with GraphiteConfig
  with ShowErrorPage
  with RunMode
  with MicroserviceFilterSupport{

  lazy val appName = Play.current.configuration.getString("appName").getOrElse("APP NAME NOT SET")

 protected lazy val defaultFrontendFilters: Seq[EssentialFilter] = Seq(
    HeadersFilter,
    LoggingFilter,
    CacheControlFilter.fromConfig("caching.allowedContentTypes"),
    RecoveryFilter)

  def frontendFilters: Seq[EssentialFilter] = defaultFrontendFilters

  override def onStart(app: Application) {
    Logger.info(s"Starting frontend : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = super.onRouteRequest(request).orElse {
    Some(request.path).filter(_.endsWith("/")).flatMap(p => super.onRouteRequest(request.copy(path = p.dropRight(1))))
  }

  override def doFilter(a: EssentialAction): EssentialAction =
    Filters(super.doFilter(a), frontendFilters: _* )


  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}
