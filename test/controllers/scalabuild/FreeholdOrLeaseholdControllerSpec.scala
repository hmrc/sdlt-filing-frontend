/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import base.ScalaSpecBase
import forms.scalabuild.FreeholdOrLeaseholdFormProvider
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import views.html.scalabuild.FreeholdOrLeaseholdView

class FreeholdOrLeaseholdControllerSpec extends ScalaSpecBase with MockitoSugar {


def onwardRoute = Call("GET", "/holding")

val formProvider = new FreeholdOrLeaseholdFormProvider()
val form = formProvider()

lazy val freeholdOrLeaseholdControllerRoute: String = controllers.scalabuild.routes.FreeholdOrLeaseholdController.onPageLoad().url

"FreeholdOrLease Controller" - {

  "must return OK and the correct view for a GET when saved user Details are fetched" in {

    val application =
      applicationBuilder()
        .build()


    running(application) {
      val request = FakeRequest(GET, freeholdOrLeaseholdControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

      val result = route(application, request).value

      val view = application.injector.instanceOf[FreeholdOrLeaseholdView]

      status(result) mustEqual OK
      contentAsString(result) must include(view(form)(request, messages(application)).body.toString)
    }
  }
}
  }