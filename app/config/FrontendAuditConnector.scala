package config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Play}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig

@Singleton
class FrontendAuditConnector @Inject()() extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")

  override protected def appNameConfiguration: Configuration = Play.current.configuration
}