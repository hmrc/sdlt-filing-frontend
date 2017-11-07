package demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class DemoControllerSpec extends UnitSpec {

  val testController = new DemoController
  val actorSystem = ActorSystem("actorSystem")
  implicit val materializer = ActorMaterializer()(actorSystem)

  "respond" should {
    "respond" in {
      val fakeRequest = FakeRequest("GET", "/test-response")
      val result = await(testController.respond(fakeRequest))

      status(result) shouldBe OK
      bodyOf(result) shouldBe "response confirmed"
    }
  }

}
