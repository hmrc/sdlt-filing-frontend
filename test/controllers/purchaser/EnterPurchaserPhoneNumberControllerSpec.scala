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
import constants.FullReturnConstants.emptyFullReturn
import controllers.routes
import forms.purchaser.EnterPurchaserPhoneNumberFormProvider
import models.purchaser.{NameOfPurchaser, WhoIsMakingThePurchase}
import models.{FullReturn, NormalMode, Purchaser, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{EnterPurchaserPhoneNumberPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import org.scalatest.BeforeAndAfterEach
import views.html.purchaser.EnterPurchaserPhoneNumberView

import scala.concurrent.Future

class EnterPurchaserPhoneNumberControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new EnterPurchaserPhoneNumberFormProvider()
  val form = formProvider()

  lazy val enterPurchaserPhoneNumberRoute = controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(NormalMode).url

  private val testStorn = "TESTSTORN"

  private val individualPurchaser = Purchaser(
    purchaserID = Some("PURCH001"),
    forename1 = Some("John"),
    forename2 = Some("Middle"),
    surname = Some("Doe"),
    companyName = None,
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  private val companyPurchaser = Purchaser(
    purchaserID = Some("PURCH002"),
    forename1 = None,
    forename2 = None,
    surname = None,
    companyName = Some("ACME Corporation"),
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  val phoneNumber = "0987654321"

  private val individualNameOfPurchaser = NameOfPurchaser(
    forename1 = Some("John"),
    forename2 = Some("Middle"),
    name = "Doe"
  )

  private def fullReturnWithIndividualPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaser)))

  private def fullReturnWithCompanyPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaser)))

  val userAnswersWithIndividualPurchaser: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
      .set(NameOfPurchaserPage, individualPurchaser.toNameOfPurchaser).success.value
      .set(WhoIsMakingThePurchasePage, models.purchaser.WhoIsMakingThePurchase.Individual).success.value

  val userAnswersWithCompanyPurchaser: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))
      .set(NameOfPurchaserPage, companyPurchaser.toNameOfPurchaser).success.value
      .set(WhoIsMakingThePurchasePage, models.purchaser.WhoIsMakingThePurchase.Company).success.value

  implicit class PurchaserOps(p: Purchaser) {
    def toNameOfPurchaser = models.purchaser.NameOfPurchaser(p.forename1, p.forename2, p.surname.orElse(p.companyName).getOrElse(""))
  }

  "EnterPurchaserPhoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, enterPurchaserPhoneNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EnterPurchaserPhoneNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersWithIndividualAndName: UserAnswers =
        UserAnswers(userAnswersId, storn = testStorn)
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value
          .set(EnterPurchaserPhoneNumberPage, phoneNumber).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualAndName))
        .build()

      running(application) {
        val request = FakeRequest(GET, enterPurchaserPhoneNumberRoute)

        val view = application.injector.instanceOf[EnterPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(phoneNumber), NormalMode, individualNameOfPurchaser.fullName)(request, messages(application)).toString
      }
    }
    
    "must redirect to next page (DoesPurchaserHaveNI page) when valid data is submitted for an individual purchaser" in {

      val userAnswersWithIndividual = UserAnswers(userAnswersId, storn = testStorn)
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
        .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithIndividual))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "0987654321"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswersWithName = UserAnswers(userAnswersId, storn = testStorn)
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
        .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EnterPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val actualContent: String = contentAsString(result)(defaultAwaitTimeout)
        val expectedContent: String = view(boundForm, NormalMode, individualNameOfPurchaser.fullName)(request, messages(application)).toString

        actualContent mustEqual expectedContent
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, enterPurchaserPhoneNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "07987654321"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return Bad Request when phone number is too long" in {

      val userAnswersWithName = UserAnswers(userAnswersId, storn = testStorn)
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
        .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithName)).build()

      running(application) {
        val PhoneNumberTooLong = "0" * 15

        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", PhoneNumberTooLong))

        val boundForm = form.bind(Map("value" -> PhoneNumberTooLong))

        val view = application.injector.instanceOf[EnterPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must accept valid phone numbers with spaces" in {

      val userAnswersWithName = UserAnswers(userAnswersId, storn = testStorn)
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
        .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "07987 654 321"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
         redirectLocation(result).value mustEqual controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Confirm identity page when valid data is submitted for a company purchaser" in {

      val userAnswersWithCompany = UserAnswers(userAnswersId, storn = testStorn)
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
        .set(NameOfPurchaserPage, companyPurchaser.toNameOfPurchaser).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCompany))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterPurchaserPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "0987654321"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
      }
    }
  }
}
