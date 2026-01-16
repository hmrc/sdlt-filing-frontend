/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.SharedOwnershipFormProvider
import org.scalatest.freespec.AnyFreeSpec
import pages.scalabuild.SharedOwnershipPage
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.SharedOwnershipView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class SharedOwnershipControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/shared-ownership")

  val formProvider = new SharedOwnershipFormProvider()
  val form = formProvider()
  lazy val sharedOwnershipRoute = routes.SharedOwnershipController.onPageLoad().url

  "Shared ownership Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, sharedOwnershipRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[SharedOwnershipView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(SharedOwnershipPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, sharedOwnershipRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[SharedOwnershipView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
      }
    }


    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, sharedOwnershipRoute)
            .withFormUrlEncodedBody(("sharedOwnership", "true")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, sharedOwnershipRoute)
            .withFormUrlEncodedBody(("sharedOwnership", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("sharedOwnership" -> ""))

        val view = application.injector.instanceOf[SharedOwnershipView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}