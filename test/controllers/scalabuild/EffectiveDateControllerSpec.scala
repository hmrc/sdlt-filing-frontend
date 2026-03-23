/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import forms.scalabuild.EffectiveDateFormProvider
import models.scalabuild.PropertyType.{NonResidential, Residential}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.scalabuild.EffectiveDatePage
import play.api.data.Form
import play.api.mvc.Call
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.EffectiveDateView

import java.time.LocalDate

class EffectiveDateControllerSpec extends AnyWordSpec with ScalaSpecBase with MockitoSugar with TestObjects {

  def onwardRoute: Call = Call("GET", "/calculate-stamp-duty-land-tax/non-uk-resident")

  val formProvider = new EffectiveDateFormProvider()
  val formForResidential: Form[LocalDate] = formProvider(Residential)
  val formForNonResidential: Form[LocalDate] = formProvider(NonResidential)
  val thisYear = LocalDate.now().getYear
  val validEffectiveDate = LocalDate.of(thisYear, 1, 1)

  lazy val effectiveDateControllerRoute: String = controllers.scalabuild.routes.EffectiveDateController.onPageLoad().url

  "Effective Date Controller" should {

    "must return OK and the correct view for a GET when saved user Details are fetched" when {
      "property is Residential" in {
        val userAnswers = uaFreeRes

        val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
          val request = FakeRequest(GET, effectiveDateControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val result = route(application, request).value

          val view = application.injector.instanceOf[EffectiveDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formForResidential)(request, messages(application)).toString
        }
      }
      "property is Non-Residential" in {
        val userAnswers = uaFreeRes
        val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
          val request = FakeRequest(GET, effectiveDateControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

          val result = route(application, request).value

          val view = application.injector.instanceOf[EffectiveDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formForNonResidential)(request, messages(application)).toString
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = uaFreeRes
        .set(EffectiveDatePage, validEffectiveDate).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, effectiveDateControllerRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[EffectiveDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formForResidential.fill(validEffectiveDate))(request, messages(application)).toString
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

      val application = applicationBuilder(userAnswers = Some(uaFreeRes)).build()

      running(application) {
        val request =
          FakeRequest(POST, effectiveDateControllerRoute)
            .withFormUrlEncodedBody(
              "effectiveDate.day" -> "invalidData",
              "effectiveDate.month" -> "2",
              "effectiveDate.year" -> "2022"
            ).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = formForResidential.bind(Map(
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