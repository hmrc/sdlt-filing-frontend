/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.RentFormProvider
import models.scalabuild.{LeaseContext, LeaseContextBuilder}
import org.mockito.ArgumentMatchers._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.FormBinding
import play.api.inject.bind
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.scalabuild.RentView

class RentControllerSpec extends AnyFreeSpec with ScalaSpecBase {
  lazy val rentRoute = controllers.scalabuild.routes.RentController.onPageLoad().url

  "RentController" - {
    val mockLeaseContextBuilder = mock[LeaseContextBuilder]
    val expectedPeriodCount = 4
    when(mockLeaseContextBuilder.build(any(), any(), any()))
      .thenReturn(LeaseContext(periodCount = expectedPeriodCount))


    "must return OK and render the view on GET" in {
      val application = applicationBuilder().overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, rentRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[RentView]
        val formProvider = application.injector.instanceOf[RentFormProvider]
        val form = formProvider(expectedPeriodCount)

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(form, expectedPeriodCount)(request, messages(application)).toString
      }
    }

    "must redirect to GET when valid rents are submitted" in {
      val application = applicationBuilder().overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.scalabuild.routes.RentController.onSubmit().url)
            .withFormUrlEncodedBody(
              "rents[0]" -> "100",
              "rents[1]" -> "200",
              "rents[2]" -> "300",
              "rents[3]" -> "400"
            )
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe rentRoute
      }
    }

    "must return Bad Request and show errors when invalid rents are submitted" in {
      val application = applicationBuilder().overrides(
          bind[LeaseContextBuilder].toInstance(mockLeaseContextBuilder)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.scalabuild.routes.RentController.onSubmit().url)
            .withFormUrlEncodedBody(
              "rents[0]" -> "100",
              "rents[1]" -> "",
              "rents[2]" -> "300"
              // missing rents[3] → should fail length = periodCount validation
            )
            .addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentView]
        val formProvider = application.injector.instanceOf[RentFormProvider]
        val form = formProvider(expectedPeriodCount)
        val boundForm = form.bindFromRequest()(request, FormBinding.Implicits.formBinding)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(boundForm, expectedPeriodCount)(request, messages(application)).toString
      }
    }
  }
}
