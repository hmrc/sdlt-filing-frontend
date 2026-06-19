/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{AbstractModule, Provides}
import controllers.actions.*
import services.crossflow._
import services.crossflow.errors._
import services.pdf.{ClasspathPdfTemplateLoader, PdfTemplateLoader}

import java.time.{Clock, ZoneOffset}
import javax.inject.Singleton

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    bind(classOf[PdfTemplateLoader]).to(classOf[ClasspathPdfTemplateLoader])
  }

  @Provides
  @Singleton
  def crossFlowRules(): Set[CrossFlowRule] = {
    F23Rules.all ++ F25Rules.all ++ F28Rules.all ++ F30Rules.all
  }

  @Provides
  @Singleton
  def landRules(): Set[LandRule] = {
    F17Rules.all ++ F18Rules.all ++ F24Rules.all ++ F30RulesLand.all
  }
}