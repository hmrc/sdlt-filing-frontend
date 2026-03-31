/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.purchaser

import base.SpecBase
import models.{FullReturn, Purchaser}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.purchaser.PurchaserBeforeYouStartView

class PurchaserBeforeYouStartControllerSpec extends SpecBase {

  val incompletePurchaserOne = Purchaser(
    purchaserID = Some("PUR001"),
    surname = Some("Smith"),
  )

  val incompletePurchaserTwo = Purchaser(
    purchaserID = Some("PUR002"),
    surname = Some("Smith"),
  )

  val testFullReturn: FullReturn = FullReturn(
    stornId = "123456",
    returnResourceRef = "REF001",
    purchaser = Some(Seq(incompletePurchaserOne, incompletePurchaserTwo))
  )

  "PurchaserBeforeYouStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to Purchaser Overview page when more than one purchasers exist" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      }
    }
  }
}
