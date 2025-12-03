/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package base
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

trait ScalaSpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures {

implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(): GuiceApplicationBuilder ={
    new GuiceApplicationBuilder().configure("play.http.router" -> "scalabuild.Routes")
}
}
