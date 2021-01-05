/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import play.api.mvc.MessagesControllerComponents
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.play.test.UnitSpec
import org.jsoup.Jsoup
import play.twirl.api.Content
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import views.html.error_template

class SDLTCErrorHandlerSpec extends UnitSpec with GuiceOneAppPerSuite  with Injecting {


  implicit val mcc: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  implicit val appConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]

  def contentAsString(of: Content): String = of.body

  "internalServerErrorTemplate" must {

    "retrieve the correct messages" in {
      implicit val request = FakeRequest()
      val errorTemplate: error_template = inject[error_template]
      val errorHandler = new SDLTCErrorHandler(mcc.messagesApi, errorTemplate, appConfig)
      val result = errorHandler.internalServerErrorTemplate
      val document = Jsoup.parse(contentAsString(result))

      document.title() should be("Sorry, there is a problem with the service - 500")
      document.getElementsByTag("h1").text() should be("Sorry, there is a problem with the service")
      document.select("#content p").first().text() should be("Try again later.")
    }
  }
}