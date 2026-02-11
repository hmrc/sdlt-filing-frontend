/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.OwnsOtherPropertiesFormProvider
import models.scalabuild.HoldingTypes
import org.scalatest.freespec.AnyFreeSpec
import pages.scalabuild.{HoldingPage, OwnsOtherPropertiesPage}
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.OwnsOtherPropertiesView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class OwnsOtherPropertiesControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/purchase-price")
  val formProvider = new OwnsOtherPropertiesFormProvider()
  val form = formProvider()
  lazy val ownsOtherPropertiesRoute = controllers.scalabuild.routes.OwnsOtherPropertiesController.onPageLoad().url

  "OwnsOtherProperty Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, ownsOtherPropertiesRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[OwnsOtherPropertiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(OwnsOtherPropertiesPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, ownsOtherPropertiesRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[OwnsOtherPropertiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(emptyUserAnswers.set(HoldingPage, HoldingTypes.Freehold).toOption).build()

      running(application) {
        val request =
          FakeRequest(POST, ownsOtherPropertiesRoute)
            .withFormUrlEncodedBody(("ownedOtherProperties", "true"))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, ownsOtherPropertiesRoute)
            .withFormUrlEncodedBody(("ownedOtherProperties", ""))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("ownedOtherProperties" -> ""))

        val view = application.injector.instanceOf[OwnsOtherPropertiesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
