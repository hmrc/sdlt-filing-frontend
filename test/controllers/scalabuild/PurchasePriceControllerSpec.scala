/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import base.ScalaSpecBase
import forms.scalabuild.PurchasePriceFormProvider
import play.api.mvc.request.RequestAttrKey
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call
import views.html.scalabuild.PurchasePriceView

class PurchasePriceControllerSpec extends ScalaSpecBase {
  def onwardRoute = Call("GET", "/calculate-stamp-duty-land-tax/purchase-price")
  val formProvider = new PurchasePriceFormProvider()
  val form          = formProvider()
  lazy val purchasePriceRoute = controllers.scalabuild.routes.PurchasePriceController.onPageLoad().url

  "PurchasePrice Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, purchasePriceRoute).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")
        val result = route(application, request).value
        val view = application.injector.instanceOf[PurchasePriceView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, purchasePriceRoute)
            .withFormUrlEncodedBody(("premium", "100000")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, purchasePriceRoute)
            .withFormUrlEncodedBody(("purchasePrice", "")).addAttr(RequestAttrKey.CSPNonce, "fake-nonce")

        val boundForm = form.bind(Map("purchasePrice" -> ""))

        val view = application.injector.instanceOf[PurchasePriceView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}

