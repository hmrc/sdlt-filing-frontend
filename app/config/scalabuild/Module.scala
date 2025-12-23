/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package config.scalabuild

import com.google.inject.AbstractModule
import controllers.scalabuild.actions._

import java.time.Clock

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[IdentifierAction]).to(classOf[SessionIdentifierAction]).asEagerSingleton()
    // lan todo: uncomment when removing the old Module
    //    bind(classOf[Clock]).toInstance(java.time.Clock.systemDefaultZone)
  }
}
