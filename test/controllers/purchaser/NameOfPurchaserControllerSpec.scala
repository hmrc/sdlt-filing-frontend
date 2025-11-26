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
import controllers.routes
import forms.purchaser.NameOfPurchaserFormProvider
import models.purchaser.{NameOfPurchaser, WhoIsMakingThePurchase}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.NameOfPurchaserView

import scala.concurrent.Future

class NameOfPurchaserControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new NameOfPurchaserFormProvider()
  val form = formProvider()

  lazy val nameOfPurchaserRoute: String =
    controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url

  private val testStorn = "TESTSTORN"

  private val individualNameOfPurchaser = NameOfPurchaser(
    forename1 = Some("John"),
    forename2 = Some("Middle"),
    name = "Doe"
  )

  private val companyNameOfPurchaser = NameOfPurchaser(
    forename1 = None,
    forename2 = None,
    name = "ACME Corporation"
  )

  val userAnswersWithIndividualType: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

  val userAnswersWithCompanyType: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

  val userAnswersWithIndividualAndName: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
      .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

  val userAnswersWithCompanyAndName: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
      .set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

  "NameOfPurchaserController.onPageLoad" - {

    "when WhoIsMakingThePurchasePage is Individual" - {

      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualType))
          .build()

        running(application) {
          val request = FakeRequest(GET, nameOfPurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[NameOfPurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Individual")(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualAndName))
          .build()

        running(application) {
          val request = FakeRequest(GET, nameOfPurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[NameOfPurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(individualNameOfPurchaser), NormalMode, "Individual")(request, messages(application)).toString
        }
      }
    }

    "when WhoIsMakingThePurchasePage is Company" - {

      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyType))
          .build()

        running(application) {
          val request = FakeRequest(GET, nameOfPurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[NameOfPurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Company")(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyAndName))
          .build()

        running(application) {
          val request = FakeRequest(GET, nameOfPurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[NameOfPurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(companyNameOfPurchaser), NormalMode, "Company")(request, messages(application)).toString
        }
      }
    }

    "when WhoIsMakingThePurchasePage is not answered" - {
      "must redirect 303 and go to WhoIsMakingThePurchasePage" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, nameOfPurchaserRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url

        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, nameOfPurchaserRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "NameOfPurchaserController.onSubmit" - {

    "when WhoIsMakingThePurchasePage is Individual" - {

      "must redirect to the next page when valid individual data is submitted" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualType))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(
              ("forename1", "John"),
              ("forename2", "Middle"),
              ("name", "Doe")
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualType))
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))
          val view = application.injector.instanceOf[NameOfPurchaserView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Individual")(request, messages(application)).toString
        }
      }
    }

    "when WhoIsMakingThePurchasePage is Company" - {

      "must redirect to the next page when valid company data is submitted" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyType))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(("name", "ACME Corporation"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyType))
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))
          val view = application.injector.instanceOf[NameOfPurchaserView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Company")(request, messages(application)).toString
        }
      }
    }

    "when WhoIsMakingThePurchasePage is not answered" - {

      "must redirect to the next page when valid data is submitted" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(("name", "Test Name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request with empty purchaser type when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, nameOfPurchaserRoute)
            .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))
          val view = application.injector.instanceOf[NameOfPurchaserView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "")(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, nameOfPurchaserRoute)
          .withFormUrlEncodedBody(
            ("forename1", "John"),
            ("forename2", "Middle"),
            ("name", "Doe")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}