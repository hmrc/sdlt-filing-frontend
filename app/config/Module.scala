/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package config
import com.google.inject.AbstractModule

import java.time.{Clock, ZoneId}

class Module extends AbstractModule{
  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.system(ZoneId.of("Europe/London")))
  }
}
