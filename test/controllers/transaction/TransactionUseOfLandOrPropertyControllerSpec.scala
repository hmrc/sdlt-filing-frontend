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
import constants.FullReturnConstants
import controllers.routes
import forms.transaction.TransactionUseOfLandOrPropertyFormProvider
import models.land.LandTypeOfProperty
import models.{FullReturn, Land, NormalMode, UserAnswers}
import models.transaction.{TransactionUseOfLandOrProperty, TransactionUseOfLandOrPropertyAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.TransactionUseOfLandOrPropertyPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.transaction.TransactionUseOfLandOrPropertyView

import scala.concurrent.Future

class TransactionUseOfLandOrPropertyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val transactionUseOfLandOrPropertyRoute = controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(NormalMode).url

  val formProvider = new TransactionUseOfLandOrPropertyFormProvider()
  val form = formProvider()

  val userAnswersMixedLand: UserAnswers =
    emptyUserAnswers.copy(
      fullReturn = Some(
        FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(
            FullReturnConstants.completeLand.copy(propertyType = Some(LandTypeOfProperty.Mixed.toString))
          ))
        )
      )
    )

  val userAnswersNonResidentialLand: UserAnswers =
    emptyUserAnswers.copy(
      fullReturn = Some(
        FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(
            FullReturnConstants.completeLand.copy(propertyType = Some(LandTypeOfProperty.NonResidential.toString))
          ))
        )
      )
    )

  val userAnswersResidentialLand: UserAnswers =
    emptyUserAnswers.copy(
      fullReturn = Some(
        FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(
            FullReturnConstants.completeLand.copy(propertyType = Some(LandTypeOfProperty.Residential.toString))
          ))
        )
      )
    )

  "TransactionUseOfLandOrProperty Controller" - {

    "must return OK and the correct view for a GET when property type is Mixed" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMixedLand)).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TransactionUseOfLandOrPropertyView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when property type is Non-residential" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNonResidentialLand)).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TransactionUseOfLandOrPropertyView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the Sale of a Business page for a GET when property type is Residential" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersResidentialLand)).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.SaleOfBusinessController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the Sale of a Business page for a GET when property type is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.SaleOfBusinessController.onPageLoad(NormalMode).url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersMixedLand.set(
        TransactionUseOfLandOrPropertyPage,
        TransactionUseOfLandOrPropertyAnswers.fromSet(TransactionUseOfLandOrProperty.values.toSet)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val view = application.injector.instanceOf[TransactionUseOfLandOrPropertyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TransactionUseOfLandOrProperty.values.toSet), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, transactionUseOfLandOrPropertyRoute)
            .withFormUrlEncodedBody(("value[0]", TransactionUseOfLandOrProperty.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, transactionUseOfLandOrPropertyRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TransactionUseOfLandOrPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, transactionUseOfLandOrPropertyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, transactionUseOfLandOrPropertyRoute)
            .withFormUrlEncodedBody(("value[0]", TransactionUseOfLandOrProperty.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
