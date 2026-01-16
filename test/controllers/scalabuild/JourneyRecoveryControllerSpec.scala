/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.JourneyRecoveryStartAgainView
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class JourneyRecoveryControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  "JourneyRecovery Controller" - {

    "must return OK and the start again view" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad().url).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        val startAgainView = application.injector.instanceOf[JourneyRecoveryStartAgainView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual startAgainView()(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
