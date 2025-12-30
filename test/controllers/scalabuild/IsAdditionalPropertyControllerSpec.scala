/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import base.ScalaSpecBase
import forms.scalabuild.IsAdditionalPropertyFormProvider
import play.api.data.Form
import views.html.scalabuild.IsAdditionalPropertyView
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call

class IsAdditionalPropertyControllerSpec extends ScalaSpecBase {
  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/replace-main-residence")
  val formProvider = new IsAdditionalPropertyFormProvider()
  val form: Form[Boolean] = formProvider()
  lazy val isAdditionalPropertyRoute: String = controllers.scalabuild.routes.IsAdditionalPropertyController.onPageLoad().url

  "IsAdditionalProperty Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, isAdditionalPropertyRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[IsAdditionalPropertyView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isAdditionalPropertyRoute)
            .withFormUrlEncodedBody(("twoOrMore", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isAdditionalPropertyRoute)
            .withFormUrlEncodedBody(("twoOrMore", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("twoOrMore" -> ""))

        val view = application.injector.instanceOf[IsAdditionalPropertyView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}