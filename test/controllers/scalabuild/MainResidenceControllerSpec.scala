/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import forms.scalabuild.MainResidenceFormProvider
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild.MainResidencePage
import play.api.data.Form
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.MainResidenceView

  class MainResidenceControllerSpec extends AnyFreeSpec with ScalaSpecBase with TestObjects {
    def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/purchase-price")
    val formProvider = new MainResidenceFormProvider()
    val form: Form[Boolean] = formProvider()
    lazy val mainResidenceRoute: String = controllers.scalabuild.routes.MainResidenceController.onPageLoad().url

    "MainResidence Controller" - {
      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, mainResidenceRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
          val result = route(application, request).value
          val view = application.injector.instanceOf[MainResidenceView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(form)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = emptyUserAnswers2.set(MainResidencePage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, mainResidenceRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
          val result = route(application, request).value
          val view = application.injector.instanceOf[MainResidenceView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val application = applicationBuilder(Some(uaFreeRes)).build()

        running(application) {
          val request =
            FakeRequest(POST, mainResidenceRoute)
              .withFormUrlEncodedBody(("mainResidence", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder().build()

        running(application) {
          val request =
            FakeRequest(POST, mainResidenceRoute)
              .withFormUrlEncodedBody(("mainResidence", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val boundForm = form.bind(Map("mainResidence" -> ""))

          val view = application.injector.instanceOf[MainResidenceView]

          val result = route(application, request).value

          status(result)          mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
        }
      }
    }
}
