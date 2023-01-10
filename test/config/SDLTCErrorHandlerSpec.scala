/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Content
import views.html.error_template

class SDLTCErrorHandlerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {


  implicit val mcc: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  implicit val appConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]

  def contentAsString(of: Content): String = of.body

  "internalServerErrorTemplate" must {

    "retrieve the correct messages" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val errorTemplate: error_template = inject[error_template]
      val errorHandler = new SDLTCErrorHandler(mcc.messagesApi, errorTemplate, appConfig)
      val result = errorHandler.internalServerErrorTemplate
      val document = Jsoup.parse(contentAsString(result))

      document.title() should be("Sorry, there is a problem with the service - 500")
      document.getElementsByTag("h1").text() should be("Sorry, there is a problem with the service")
      document.select(".govuk-grid-column-two-thirds p").first().text() should be("Try again later.")
    }
  }
}