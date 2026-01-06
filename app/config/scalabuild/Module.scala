/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package config.scalabuild

import com.google.inject.AbstractModule
import controllers.scalabuild.actions._

import java.time.{Clock, ZoneId}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[IdentifierAction]).to(classOf[SessionIdentifierAction]).asEagerSingleton()
    bind(classOf[Clock]).toInstance(Clock.system(ZoneId.of("Europe/London")))
  }
}
