/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import forms.scalabuild.EffectiveDateFormProvider
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.scalabuild.EffectiveDatePage
import play.api.data.Form
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.EffectiveDateView

import java.time.LocalDate

class EffectiveDateControllerSpec extends AnyFreeSpec with ScalaSpecBase with MockitoSugar with TestObjects {

  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/non-uk-resident")

  val formProvider = new EffectiveDateFormProvider()
  val form: Form[LocalDate] = formProvider()
  val thisYear = LocalDate.now().getYear
  val validEffectiveDate = LocalDate.of(thisYear, 1, 1)

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

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers2
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, effectiveDateControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[EffectiveDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validEffectiveDate))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(Some(uaFreeRes)).build()

      running(application) {
        val request =
          FakeRequest(POST, effectiveDateControllerRoute)
            .withFormUrlEncodedBody(
              "effectiveDate.day" -> "11",
              "effectiveDate.month" -> "2",
              "effectiveDate.year" -> s"$thisYear"
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
          FakeRequest(POST, effectiveDateControllerRoute)
            .withFormUrlEncodedBody(
              "effectiveDate.day" -> "invalidData",
              "effectiveDate.month" -> "2",
              "effectiveDate.year" -> "2022"
            ).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map(
          "effectiveDate.day" -> "invalidData",
          "effectiveDate.month" -> "2",
          "effectiveDate.year" -> "2022"
        ))

        val view = application.injector.instanceOf[EffectiveDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}