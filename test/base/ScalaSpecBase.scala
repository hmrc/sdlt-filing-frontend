/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package base
import config.FrontendAppConfig
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import play.api.inject.bind
import java.time.{Clock, LocalDate, ZoneId}

trait ScalaSpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with GuiceOneAppPerSuite{

implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder().configure("play.http.router" -> "scalabuild.Routes")
  }

  protected def applicationBuilderWithDate(date: LocalDate): GuiceApplicationBuilder = {
    val fixedClock = Clock.fixed(date.atStartOfDay(ZoneId.of("Europe/London")).toInstant, ZoneId.of("Europe/London"))
    applicationBuilder().overrides(
      bind[Clock].toInstance(fixedClock)
    )
  }

  lazy val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
}
