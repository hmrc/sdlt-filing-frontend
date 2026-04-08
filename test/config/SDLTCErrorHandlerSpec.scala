/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import base.ScalaSpecBase
import org.jsoup.Jsoup
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Content
import views.html.scalabuild.error_template

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

class SDLTCErrorHandlerSpec extends AnyWordSpec with ScalaSpecBase with Injecting {

  val app: Application = application()
  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val appConf: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  def contentAsString(of: Content): String = of.body

  "internalServerErrorTemplate" must {

    "retrieve the correct messages" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val errorTemplate: error_template = inject[error_template]
      val startAgainTemplate = inject[views.html.scalabuild.JourneyRecoveryStartAgainView]
      val ec = inject[ExecutionContext]
      val errorHandler = new SDLTCErrorHandler(mcc.messagesApi, errorTemplate, startAgainTemplate, appConf, ec)
      val futureResult = errorHandler.internalServerErrorTemplate
      val htmlContent = Await.result(futureResult, 5.seconds)
      val document = Jsoup.parse(contentAsString(htmlContent))
      document.title() should be("Sorry, there is a problem with the service - 500")
      document.getElementsByTag("h1").text() should be("Sorry, there is a problem with the service")
      document.select(".govuk-grid-column-two-thirds p").first().text() should be("Try again later.")
    }
  }
}