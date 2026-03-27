/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import base.ScalaSpecBase
import org.scalatest.freespec.AnyFreeSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.StartGuidanceView
import play.api.mvc.request.RequestAttrKey
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class StartGuidanceControllerSpec extends AnyFreeSpec with ScalaSpecBase {

"Start Controller" - {

  "must return OK and the correct view for a GET" in {

    val application = applicationBuilder().build()
    running(application) {
      val request = FakeRequest(GET, routes.StartGuidanceController.onPageLoad().url).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

      val result = route(application, request).value

      val view = application.injector.instanceOf[StartGuidanceView]

      status(result) mustEqual OK
      contentAsString(result) must equal(view()(request, messages(application)).toString)
    }
  }
}
}