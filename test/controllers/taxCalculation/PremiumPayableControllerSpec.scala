/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.taxCalculation

import base.ScalaSpecBase
import forms.scalabuild.taxCalculation.PremiumPayableFormProvider
import org.scalatest.freespec.AnyFreeSpec
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, running, status}
import views.html.scalabuild.taxCalculation.PremiumPayableView
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper


class PremiumPayableControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  val formProvider = new PremiumPayableFormProvider()
  val form = formProvider()
  lazy val premiumPayableRoute = controllers.taxCalculation.routes.PremiumPayableController.onPageLoad().url

  "PremiumPayable Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, premiumPayableRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[PremiumPayableView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }
}
