/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package config

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.play.bootstrap.filters.MicroserviceFilters
import uk.gov.hmrc.play.bootstrap.filters.frontend.HeadersFilter

class SDLTCFilters @Inject()(defaultFilters: MicroserviceFilters,
                             headersFilter: HeadersFilter) extends DefaultHttpFilters(defaultFilters.filters.+:(headersFilter) :_*)