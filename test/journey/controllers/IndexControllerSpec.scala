/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package journey.controllers

import base.BaseSpec
import config.FrontendAppConfig
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest

class IndexControllerSpec extends BaseSpec with MockitoSugar with GuiceOneServerPerSuite {

  class Setup {
    val mockComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val injectedViewInstance = app.injector.instanceOf[journey.views.html.index]
    implicit val mockConfig = mock[FrontendAppConfig]

    val controller = new IndexController(mockComponents,injectedViewInstance, mockConfig)
  }

  "Sending a GET request to IndexController" must {
    "return a 200" in new Setup {

        val result = controller.showIndex(FakeRequest())
        status(result) shouldBe OK
      }
  }
}
