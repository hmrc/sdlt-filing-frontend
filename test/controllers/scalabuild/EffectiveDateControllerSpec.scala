/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.EffectiveDateFormProvider
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import views.html.scalabuild.EffectiveDateView

class EffectiveDateControllerSpec extends ScalaSpecBase with MockitoSugar {


  def onwardRoute = Call("GET", "/date")

  val formProvider = new EffectiveDateFormProvider()
  val form = formProvider()

  lazy val effectiveDateControllerRoute: String = controllers.scalabuild.routes.EffectiveDateController.onPageLoad().url

  "Effective Date Controller" - {

    "must return OK and the correct view for a GET when saved user Details are fetched" in {

      val application =
        applicationBuilder()
          .build()


      running(application) {
        val request = FakeRequest(GET, effectiveDateControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        val view = application.injector.instanceOf[EffectiveDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }
  }
}