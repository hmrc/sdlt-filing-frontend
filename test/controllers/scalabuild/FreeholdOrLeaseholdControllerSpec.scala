/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.FreeholdOrLeaseholdFormProvider
import models.scalabuild.HoldingTypes
import models.scalabuild.HoldingTypes.Freehold
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild.HoldingPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.FreeholdOrLeaseholdView

class FreeholdOrLeaseholdControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/property")

  val formProvider = new FreeholdOrLeaseholdFormProvider()
  val form: Form[HoldingTypes] = formProvider()

  lazy val freeholdOrLeaseholdRoute: String =
    controllers.scalabuild.routes.FreeholdOrLeaseholdController.onPageLoad().url

  "FreeholdOrLease Controller" - {

    "must return OK and the correct view for a GET when saved user Details are fetched" in {

      val application =
        applicationBuilder()
          .build()

      running(application) {
        val request =
          FakeRequest(GET, freeholdOrLeaseholdRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        val view = application.injector.instanceOf[FreeholdOrLeaseholdView]

        status(result) mustEqual OK
        contentAsString(result) must include(view(form)(request, messages(application)).body)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .set(HoldingPage, Freehold).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, freeholdOrLeaseholdRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[FreeholdOrLeaseholdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Freehold))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, freeholdOrLeaseholdRoute)
            .withFormUrlEncodedBody(("value", "Freehold"))
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
          FakeRequest(POST, freeholdOrLeaseholdRoute)
            .withFormUrlEncodedBody(("twoOrMore", ""))
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("twoOrMore" -> ""))

        val view = application.injector.instanceOf[FreeholdOrLeaseholdView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
