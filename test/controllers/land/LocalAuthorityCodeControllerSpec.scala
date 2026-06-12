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

package controllers.land

import base.SpecBase
import constants.FullReturnConstants.{completeLand, emptyFullReturn}
import controllers.routes
import forms.land.LocalAuthorityCodeFormProvider
import models.*
import models.address.{Address, Country}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.land.{LandAddressPage, LocalAuthorityCodePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import views.html.land.LocalAuthorityCodeView

import scala.concurrent.Future

class LocalAuthorityCodeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val testStorn = "TESTSTORN"
  val formProvider = new LocalAuthorityCodeFormProvider()
  val form: Form[String] = formProvider()

  lazy val localAuthorityCodeRoute: String =
    controllers.land.routes.LocalAuthorityCodeController.onPageLoad(NormalMode).url

  val completeTransaction: Transaction = Transaction(
    transactionID = Some("TXN001"),
    returnID = Some("RET123456789"),
    contractDate = Some("2024-09-15"),
    effectiveDate = Some("2024-10-01"),
    exchangedLandAddress4 = None
  )

  val completeLandForUK: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    postcode = Some("TE1 6XE")
  )

  val testAddress: Address = Address(
    "16 Coniston Court",
    Some("Holland road"),
    None, None, None,
    Some("RG1 7NQ"),
    Some(Country(Some("UK"), Some("United Kingdom"))),
    false
  )

  private def fullReturnWithReqData: FullReturn =
    emptyFullReturn.copy(transaction = Some(completeTransaction), land = Some(Seq(completeLand)))

  val userAnswersData: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithReqData))
      .set(LandAddressPage, testAddress).success.value

  private val silentCrossFlow = new CrossFlowValidationService(Set.empty, Set.empty) {
    override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] = Nil
  }

  private def crossFlowWithFailure(failure: CrossFlowFailure) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] =
        Seq(failure)
    }

  private val cf9aFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    args           = Nil
  )
  
  private val cf9aInlineText =
    "Entering this local authority code means the effective date of the transaction must be the same as or after 1 April 2018"
  
  private val cf9aBodyText =
    "This local authority code cannot be used when the effective date of transaction is before 1 April 2018"

  "LocalAuthorityCode Controller" - {

    "On Load()" - {

      "must return OK and the correct view for a GET" in {
        val userAnswers = emptyUserAnswers.set(LandAddressPage, testAddress).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[LocalAuthorityCodeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = emptyUserAnswers
          .set(LandAddressPage, testAddress).success.value
          .set(LocalAuthorityCodePage, "1234").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val view = application.injector.instanceOf[LocalAuthorityCodeView]
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill("1234"), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must not surface any cross-flow wording on initial GET, even when cross-flow reports a failure" in {
        val userAnswers = emptyUserAnswers
          .set(LandAddressPage, testAddress).success.value
          .set(LocalAuthorityCodePage, "6996").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(crossFlowWithFailure(cf9aFailure)))
          .build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must not include cf9aInlineText
          content must not include cf9aBodyText
        }
      }
    }

    "On Submit()" - {

      "must redirect to the next page when a valid format code is submitted and cross-flow reports no failures" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(silentCrossFlow)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "0121"))

          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return BAD_REQUEST when value is empty" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view = application.injector.instanceOf[LocalAuthorityCodeView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return BAD_REQUEST when value is too short (less than 4 characters)" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "12"))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST

          val htmlData = contentAsString(result)
          htmlData must include("The local authority code must be 4 characters")
        }
      }

      "must return BAD_REQUEST when value is too long (more than 4 characters)" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "12345"))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST

          val htmlData = contentAsString(result)
          htmlData must include("The local authority code must be 4 characters")
        }
      }

      "must return BAD_REQUEST when value contains non-numeric characters" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(bind[CrossFlowValidationService].toInstance(silentCrossFlow))
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "ABCD"))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST

          val htmlData = contentAsString(result)
          htmlData must include("The local authority code must only include numbers 0 to 9")
        }
      }

      "must return BAD_REQUEST showing the inline cross-flow error (not the body) on submit" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowWithFailure(cf9aFailure))
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "6996"))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST

          val content = contentAsString(result)
          content must include(cf9aInlineText)
          content must not include cf9aBodyText
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, localAuthorityCodeRoute)
            .withFormUrlEncodedBody(("value", "1234"))

          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}