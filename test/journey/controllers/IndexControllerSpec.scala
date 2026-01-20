/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package journey.controllers

import config.FrontendAppConfig
import journey.views.html.index
import play.api.http.Status._
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import base.ScalaSpecBase
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.test.Helpers.{defaultAwaitTimeout, status}

import scala.concurrent.Future

class IndexControllerSpec extends AnyWordSpec with ScalaSpecBase {

  class Setup {
    val app: Application = application()
    val mockComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
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
