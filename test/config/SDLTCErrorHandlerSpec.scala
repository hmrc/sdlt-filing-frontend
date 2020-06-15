/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package config

import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.Jsoup
import play.twirl.api.Content

class SDLTCErrorHandlerSpec extends UnitSpec with WithFakeApplication {


  implicit val mcc: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  implicit val appConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]

  def contentAsString(of: Content): String = of.body

  "internalServerErrorTemplate" must {

    "retrieve the correct messages" in {
      implicit val request = FakeRequest()
      val errorHandler = new SDLTCErrorHandler(mcc.messagesApi, appConfig)
      val result = errorHandler.internalServerErrorTemplate
      val document = Jsoup.parse(contentAsString(result))

      document.title() should be("Sorry, there is a problem with the service - 500")
      document.getElementsByTag("h1").text() should be("Sorry, there is a problem with the service")
      document.select("#content p").first().text() should be("Try again later.")
    }
  }
}