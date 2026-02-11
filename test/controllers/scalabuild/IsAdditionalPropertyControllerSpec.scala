/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import base.ScalaSpecBase
import forms.scalabuild.IsAdditionalPropertyFormProvider
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild.IsAdditionalPropertyPage
import play.api.data.Form
import views.html.scalabuild.IsAdditionalPropertyView
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call

class IsAdditionalPropertyControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/summary")
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

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(IsAdditionalPropertyPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, isAdditionalPropertyRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[IsAdditionalPropertyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
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