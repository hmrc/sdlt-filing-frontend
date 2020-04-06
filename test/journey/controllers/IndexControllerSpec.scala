/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package journey.controllers

import config.FrontendAppConfig
import org.scalamock.scalatest.MockFactory
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class IndexControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication{

  class Setup {
    val mockComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    implicit val mockConfig = mock[FrontendAppConfig]
    val controller = new IndexController(mockComponents, mockConfig)
  }

  "Sending a GET request to IndexController" should {
    "return a 200" in new Setup {

        val result = controller.showIndex(FakeRequest())
        status(result) shouldBe OK
      }
  }
}