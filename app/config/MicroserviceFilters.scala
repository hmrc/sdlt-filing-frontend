/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import javax.inject.{Inject, Singleton}
import com.kenshoo.play.metrics.MetricsFilter
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.play.bootstrap.filters.{AuditFilter, CacheControlFilter, LoggingFilter, MDCFilter}

@Singleton
class MicroserviceFilters @Inject()(
                                     metricsFilter: MetricsFilter,
                                     auditFilter: AuditFilter,
                                     loggingFilter: LoggingFilter,
                                     cacheFilter: CacheControlFilter,
                                     mdcFilter: MDCFilter
                                   ) extends DefaultHttpFilters(metricsFilter, auditFilter, loggingFilter, cacheFilter, mdcFilter)
