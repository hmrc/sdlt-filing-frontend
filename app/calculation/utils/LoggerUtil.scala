/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.utils

import play.api.Logging

// $COVERAGE-OFF$

object LoggerUtil extends Logging {

  def logInfo(content: String): Unit = logger.info(content)
  def logDebug(content: String): Unit = logger.debug(content)
  def logWarn(content: String): Unit = logger.warn(content)
  def logError(content: String): Unit = logger.error(content)
}

// $COVERAGE-ON$