/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.purchaser.ChangePurchaserOneFormProvider
import models.purchaser.NameOfPurchaser
import models.{FullReturn, NormalMode, Purchaser, ReturnInfo, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.ChangePurchaserOnePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserService
import views.html.purchaser.ChangePurchaserOneView

import scala.concurrent.Future


class ChangePurchaserOneControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val changePurchaserOneRoute: String = controllers.purchaser.routes.ChangePurchaserOneController.onPageLoad().url

  val formProvider = new ChangePurchaserOneFormProvider()
  val form: Form[String] = formProvider()


  val purchaserOne = Purchaser(
    purchaserID = Some("PUR001"),
    forename1 = Some("John"),
    forename2 = Some("David"),
    surname = Some("Smith")
  )

  val purchaserTwo = Purchaser(
    purchaserID = Some("PUR002"),
    forename1 = Some("Sarah"),
    forename2 = Some("Anne"),
    surname = Some("Jones")
  )

  val purchaserThree = Purchaser(
    purchaserID = Some("PUR003"),
    forename1 = Some("Matthew"),
    forename2 = Some("John"),
    surname = Some("Brown")
  )

  val purchaserOneName = "John David Smith"

  val otherPurchasersWithNames: Seq[(Purchaser, String)] = Seq(
    (purchaserTwo, "Sarah Anne Jones"),
    (purchaserThree, "Matthew John Brown")
  )

  val testFullReturn = FullReturn(
    stornId = "123456",
    returnResourceRef = "REF001",
    returnInfo = Some(testReturnInfo)
  )

  val testReturnInfo = ReturnInfo(
    mainPurchaserID = Some("PUR001")
  )


  "ChangePurchaserOne Controller" - {

    "must return OK and the correct view for a GET" in {

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(testFullReturn))

      val mockPurchaserService = mock[PurchaserService]

      when(mockPurchaserService.splitPurchasers(any()))
        .thenReturn(
          (Some(purchaserOne), Some(purchaserOneName), otherPurchasersWithNames)
        )

      when(mockPurchaserService.mainPurchaserName(any()))
        .thenReturn(
          Some(
            NameOfPurchaser(
              forename1 = Some("John"),
              forename2 = Some("David"),
              name = "Smith"
            )
          )
        )

      val application = applicationBuilder(userAnswers = Some(testUserAnswers))
        .overrides(
          bind[PurchaserService].toInstance(mockPurchaserService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, changePurchaserOneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangePurchaserOneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, purchaserOne, purchaserOneName, otherPurchasersWithNames)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(testFullReturn))

      val mockPurchaserService = mock[PurchaserService]

      when(mockPurchaserService.splitPurchasers(any()))
        .thenReturn(
          (Some(purchaserOne), Some(purchaserOneName), otherPurchasersWithNames)
        )

      when(mockPurchaserService.mainPurchaserName(any()))
        .thenReturn(
          Some(
            NameOfPurchaser(
              forename1 = Some("John"),
              forename2 = Some("David"),
              name = "Smith"
            )
          )
        )

      val application = applicationBuilder(userAnswers = Some(testUserAnswers
        .set(ChangePurchaserOnePage, purchaserTwo.purchaserID.getOrElse("")).success.value))
        .overrides(
          bind[PurchaserService].toInstance(mockPurchaserService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, changePurchaserOneRoute)

        val view = application.injector.instanceOf[ChangePurchaserOneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("PUR002"), NormalMode, purchaserOne, purchaserOneName, otherPurchasersWithNames)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(testFullReturn))

      val mockPurchaserService = mock[PurchaserService]

      when(mockPurchaserService.splitPurchasers(any()))
        .thenReturn(
          (Some(purchaserOne), Some(purchaserOneName), otherPurchasersWithNames)
        )

      when(mockPurchaserService.mainPurchaserName(any()))
        .thenReturn(
          Some(
            NameOfPurchaser(
              forename1 = Some("John"),
              forename2 = Some("David"),
              name = "Smith"
            )
          )
        )

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, changePurchaserOneRoute)
            .withFormUrlEncodedBody(("value", "PUR002"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(testFullReturn))

      val mockPurchaserService = mock[PurchaserService]

      when(mockPurchaserService.splitPurchasers(any()))
        .thenReturn(
          (Some(purchaserOne), Some(purchaserOneName), otherPurchasersWithNames)
        )

      when(mockPurchaserService.mainPurchaserName(any()))
        .thenReturn(
          Some(
            NameOfPurchaser(
              forename1 = Some("John"),
              forename2 = Some("David"),
              name = "Smith"
            )
          )
        )

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, changePurchaserOneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChangePurchaserOneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, purchaserOne, purchaserOneName, otherPurchasersWithNames)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, changePurchaserOneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Purchaser Overview Page for a GET if no purchaser one is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, changePurchaserOneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, changePurchaserOneRoute)
            .withFormUrlEncodedBody(("value", "PUR001"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Purchaser Overview Page for a POST if no purchaser one is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, changePurchaserOneRoute)
          .withFormUrlEncodedBody("value" -> "PUR001")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      }
    }
  }
}
