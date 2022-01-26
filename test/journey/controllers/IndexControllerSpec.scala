/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package journey.controllers

import base.BaseSpec
import config.FrontendAppConfig
import journey.views.html.index
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

class IndexControllerSpec extends BaseSpec with MockitoSugar with GuiceOneServerPerSuite {

  class Setup {
    val mockComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val injectedViewInstance: index = app.injector.instanceOf[journey.views.html.index]
    implicit val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val controller = new IndexController(mockComponents,injectedViewInstance, mockConfig)
  }

  "Sending a GET request to IndexController" must {
    "return a 200" in new Setup {

        val result: Future[Result] = controller.showIndex(FakeRequest())
        status(result) shouldBe OK
      }
  }
}
