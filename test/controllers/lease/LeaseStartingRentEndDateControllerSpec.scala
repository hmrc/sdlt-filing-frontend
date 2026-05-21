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

package controllers.lease

import base.SpecBase
import controllers.routes
import forms.lease.LeaseStartingRentEndDateFormProvider
import models.{FullReturn, Lease, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.{LeaseStartDatePage, LeaseStartingRentEndDatePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.lease.LeaseStartingRentEndDateView

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class LeaseStartingRentEndDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()
  private val formProvider = new LeaseStartingRentEndDateFormProvider()
  private def form = formProvider()

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: LocalDate = LocalDate.of(2023, 3, 27)

  lazy val leaseStartingRentEndDateRoute: String =
    controllers.lease.routes.LeaseStartingRentEndDateController.onPageLoad(NormalMode).url

  val testUserAnswers: UserAnswers = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(),
    lastUpdated = Instant.now
  )

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, leaseStartingRentEndDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, leaseStartingRentEndDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "LeaseStartingRentEndDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[LeaseStartingRentEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers.set(LeaseStartingRentEndDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[LeaseStartingRentEndDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest(), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      val request =
        FakeRequest(POST, leaseStartingRentEndDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[LeaseStartingRentEndDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return BadRequest when rent end date is before the lease start date for a POST" in {

      val fullReturn = FullReturn(stornId = "1", returnResourceRef = "ref",
        lease = Some(Lease(contractEndDate = Some("1 01 2028"))))
      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturn))
        .set(LeaseStartDatePage, LocalDate.of(2023, 6, 1)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, leaseStartingRentEndDateRoute)
          .withFormUrlEncodedBody("value.day" -> "1", "value.month" -> "1", "value.year" -> "2023")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("The end date for starting rent must be after the start date as specified in the lease")
      }
    }

    "must return BadRequest when rent end date is after the lease end date for a POST" in {

      val fullReturn = FullReturn(stornId = "1", returnResourceRef = "ref",
        lease = Some(Lease(contractEndDate = Some("1 06 2023"))))
      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturn))
        .set(LeaseStartDatePage, LocalDate.of(2023, 1, 1)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, leaseStartingRentEndDateRoute)
          .withFormUrlEncodedBody("value.day" -> "1", "value.month" -> "1", "value.year" -> "2024")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("The end date for starting rent must be before the end date as specified in the lease")
      }
    }

    "must redirect to Journey Recovery when lease end date is before lease start date (inconsistent backend data) for a POST" in {

      val fullReturn = FullReturn(stornId = "1", returnResourceRef = "ref",
        lease = Some(Lease(contractEndDate = Some("1 01 2020"))))
      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturn))
        .set(LeaseStartDatePage, LocalDate.of(2023, 6, 1)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, leaseStartingRentEndDateRoute)
          .withFormUrlEncodedBody("value.day" -> "1", "value.month" -> "1", "value.year" -> "2022")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
