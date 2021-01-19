/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package journey.controllers

import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.mockito.Mockito._

class IndexControllerSpec extends UnitSpec with MockitoSugar with GuiceOneServerPerSuite {

  class Setup {
    val mockComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val injectedViewInstance = app.injector.instanceOf[journey.views.html.index]
    implicit val mockConfig = mock[FrontendAppConfig]

    val controller = new IndexController(mockComponents,injectedViewInstance, mockConfig)
  }

  "Sending a GET request to IndexController" should {
    "return a 200" in new Setup {

        when(mockConfig.analyticsToken) thenReturn ""
        when(mockConfig.analyticsHost) thenReturn ""

        val result = controller.showIndex(FakeRequest())
        status(result) shouldBe OK
      }
  }
}
