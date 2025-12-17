/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.LeaseDatesFormProvider
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.LeaseDatesView

import java.time.LocalDate

class LeaseDatesControllerSpec extends ScalaSpecBase {

  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/lease-dates")

  val formProvider = new LeaseDatesFormProvider()
  val validEffectiveDate = LocalDate.of(2025, 1, 1)
  val form = formProvider(validEffectiveDate)

  lazy val leaseDatesControllerRoute: String = controllers.scalabuild.routes.LeaseDatesController.onPageLoad().url

  "Lease Dates Controller" - {

    "must return OK and the correct view for a GET when saved user Details are fetched" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, leaseDatesControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[LeaseDatesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, leaseDatesControllerRoute)
            .withFormUrlEncodedBody(
              ("leaseStartDate.day", "1"), ("leaseStartDate.month", "1"), ("leaseStartDate.year", "2025"),
              ("leaseEndDate.day", "1"), ("leaseEndDate.month", "1"), ("leaseEndDate.year", "2026")
            ).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, leaseDatesControllerRoute)
            .withFormUrlEncodedBody(("leaseStartDate.day", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val boundForm = form.bind(Map("leaseStartDate.day" -> ""))
        val view = application.injector.instanceOf[LeaseDatesView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}