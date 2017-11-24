package journey.controllers

import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class IndexControllerSpec extends UnitSpec with MockFactory with WithFakeApplication{

  class Setup {
    val controller = new IndexController {
    }
  }

  "Sending a GET request to IndexController" should {
    "return a 200" in new Setup {

        val result = controller.showIndex(FakeRequest())
        status(result) shouldBe OK
      }
  }
}