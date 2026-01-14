/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package base
import config.scalabuild.FrontendAppConfig
import models.scalabuild.UserAnswers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{FakeRequest, Injecting}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.play.PlayMongoModule

import java.time.{Clock, LocalDate, ZoneId}
import scala.concurrent.Future

trait ScalaSpecBase extends TryValues with OptionValues with ScalaFutures with MockitoSugar with Matchers {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  private val sessionRepositoryStub: SessionRepository =
    mock[SessionRepository]

  when(sessionRepositoryStub.get(anyString))
    .thenReturn(Future.successful(None))

  when(sessionRepositoryStub.set(any[UserAnswers]))
    .thenReturn(Future.successful(true))

  protected def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "scalabuild.Routes")
      .disable[PlayMongoModule]
      .overrides(
        bind[SessionRepository].toInstance(sessionRepositoryStub)
      )
  }

  protected def applicationBuilderWithDate(date: LocalDate): GuiceApplicationBuilder = {
    val fixedClock = Clock.fixed(date.atStartOfDay(ZoneId.of("Europe/London")).toInstant, ZoneId.of("Europe/London"))
    applicationBuilder().overrides(
      bind[Clock].toInstance(fixedClock),
    )
  }

  def application(): Application =
    applicationBuilder().build()

  lazy val appConfig: FrontendAppConfig = application().injector.instanceOf[FrontendAppConfig]
}
