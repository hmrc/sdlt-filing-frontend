package demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class DemoControllerSpec extends UnitSpec {

  val testController = new DemoController
  val actorSystem = ActorSystem("actorSystem")
  implicit val materializer = ActorMaterializer()(actorSystem)

  "respond" should {
    "respond when sent some JSON" in {
      val json = Json.parse(
        """
          |{
          |  "testData":"testing"
          |}
        """.stripMargin)
      val fakeRequest = FakeRequest("POST", "/test-response").withJsonBody(json)
      val result = await(testController.respond(fakeRequest))

      status(result) shouldBe OK
      bodyOf(result) shouldBe """response confirmed: Json received = {"testData":"testing"}"""
    }
    "error when sent no JSON" in {
      val fakeRequest = FakeRequest("POST", "/test-response")
      val result = await(testController.respond(fakeRequest))

      status(result) shouldBe BAD_REQUEST
      bodyOf(result) shouldBe "No Json"
    }
  }

}
