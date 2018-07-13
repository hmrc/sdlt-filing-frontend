package journey.controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.FrontendAuditConnector
import play.api.http.Status._
import controllers.ExitSurveyController
import org.mockito.Matchers
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.model
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ExitSurveyControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication{

  val auditConnector = mock[FrontendAuditConnector]
  val testExitSurveyController = new ExitSurveyController(auditConnector)
  val actorSystem = ActorSystem("actorSystem")
  val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  implicit val hc = HeaderCarrier()

  "onSubmit" should{
    "throw a BadRequest/400" when{
      "the json is empty" in{
        val fakeRequest = FakeRequest()
        val result = testExitSurveyController.onSubmit(fakeRequest)
        status(result) shouldBe BAD_REQUEST
        bodyOf(await(result))(materializer) shouldBe "No Json received"
      }

      "the json has the wrong data types" in{
        val incorrectJson: JsValue = Json.parse(
          """
            {
             "radioFeedback": "data",
             "textFeedback": 123
            }""".stripMargin)

        val fakeRequest = FakeRequest().withJsonBody(incorrectJson)
        val result = testExitSurveyController.onSubmit(fakeRequest)
        bodyOf(await(result))(materializer) shouldBe "Invalid Json received: List((/textFeedback,List(ValidationError(List(error.expected.jsstring),WrappedArray()))))"
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return an Ok/200" when{
      "the json doesn't match the model" in{
        val incorrectJson: JsValue = Json.parse(
          """
            {
             "test": "data",
             "test": "data",
             "number": 123
            }""".stripMargin)

        val fakeRequest = FakeRequest().withJsonBody(incorrectJson)
        val result = testExitSurveyController.onSubmit(fakeRequest)
        bodyOf(await(result))(materializer) shouldBe "Empty Survey received"
        status(result) shouldBe OK
      }

      "given valid completed json" in{
        val correctJson: JsValue = Json.parse(
          """
             {
             "radioFeedback": "Satisfied",
             "textFeedback": "This service was perfect, wow, I'm Owen Wilson."
             }""")

        val fakeRequest = FakeRequest().withJsonBody(correctJson)


        when(auditConnector.sendEvent(Matchers.any())(Matchers.any[HeaderCarrier](), Matchers.any[ExecutionContext]()))
          .thenReturn(Future.successful(AuditResult.Success))

        val result = testExitSurveyController.onSubmit(fakeRequest)
        status(result) shouldBe OK
        bodyOf(await(result))(materializer) shouldBe "Completed Survey"
      }

      "given valid partially complete json" in{
        val incorrectJson: JsValue = Json.parse(
          """
             {
             "radioFeedback": "",
             "textFeedback": "This service was perfect, wow, I'm Owen Wilson."
             }""")

        val fakeRequest = FakeRequest().withJsonBody(incorrectJson)
        val result = testExitSurveyController.onSubmit(fakeRequest)
        status(result) shouldBe OK
        bodyOf(await(result))(materializer) shouldBe "Completed Survey"
      }

      "given valid json & audit event fails" in {
        val correctJson: JsValue = Json.parse(
          """
             {
             "radioFeedback": "Satisfied",
             "textFeedback": "This service was perfect, wow, I'm Owen Wilson."
             }""")

        val fakeRequest = FakeRequest().withJsonBody(correctJson)


        when(auditConnector.sendEvent(Matchers.any())(Matchers.any[HeaderCarrier](), Matchers.any[ExecutionContext]()))
          .thenReturn(Future.failed(new RuntimeException()))

        val result = testExitSurveyController.onSubmit(fakeRequest)
        status(result) shouldBe OK
        bodyOf(await(result))(materializer) shouldBe "Completed Survey"
      }

    }
  }
}