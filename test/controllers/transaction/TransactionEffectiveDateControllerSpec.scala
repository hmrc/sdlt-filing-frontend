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

package controllers.transaction

import base.SpecBase
import controllers.routes
import forms.transaction.TransactionEffectiveDateFormProvider
import models.{NormalMode, UserAnswers}
import pages.transaction.TransactionEffectiveDatePage
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.TimeMachine
import views.html.transaction.TransactionEffectiveDateView
import java.time.{Instant, LocalDate, ZoneOffset}

class TransactionEffectiveDateControllerSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()
  private val time = TimeMachine()
  private val formProvider = new TransactionEffectiveDateFormProvider(time)
  private def form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  lazy val transactionEffectiveDateRoute: String = controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(NormalMode).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, transactionEffectiveDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, transactionEffectiveDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  val testUserAnswers: UserAnswers = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(),
    lastUpdated = Instant.now
  )

  "TransactionEffectiveDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[TransactionEffectiveDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers.set(TransactionEffectiveDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[TransactionEffectiveDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest(), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, transactionEffectiveDateRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString)

        val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(NormalMode).url // TODO DTR-2917 - Sprint 12 - configure routing to tr-3 page
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      val request =
        FakeRequest(POST, transactionEffectiveDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TransactionEffectiveDateView]

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
  }
}
