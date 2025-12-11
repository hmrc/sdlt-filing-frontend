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
import forms.purchaser.AddPurchaserPhoneNumberFormProvider
import models.{FullReturn, NormalMode, Purchaser, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{AddPurchaserPhoneNumberPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.AddPurchaserPhoneNumberView

import scala.concurrent.Future

class AddPurchaserPhoneNumberControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  lazy val addPurchaserPhoneNumberRoute: String = controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode).url

  val formProvider = new AddPurchaserPhoneNumberFormProvider()
  val form = formProvider()

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

  "AddPurchaserPhoneNumber Controller" - {

    "must return OK and correct view for individual purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserPhoneNumberRoute)
        val view = application.injector.instanceOf[AddPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must return OK and correct view for company purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserPhoneNumberRoute)
        val view = application.injector.instanceOf[AddPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "ACME Corporation")(request, messages(application)).toString
      }
    }

    "must populate the form when previously answered" in {
      val ua = userAnswersWithIndividualPurchaser
        .set(AddPurchaserPhoneNumberPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserPhoneNumberRoute)
        val view = application.injector.instanceOf[AddPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to PurchaserPhoneNumberController if Yes for individual purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserPhoneNumberRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to PurchaserPhoneNumberController if Yes for company purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserPhoneNumberRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to DoesPurchaserHaveNIController if No for individual purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserPhoneNumberRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to PurchaserConfirmIdentityController if No for company purchaser" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserPhoneNumberRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
      }
    }

    "must return BadRequest for invalid data" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserPhoneNumberRoute)
          .withFormUrlEncodedBody(("value", "invalid"))

        val boundForm = form.bind(Map("value" -> "invalid"))
        val view = application.injector.instanceOf[AddPurchaserPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "ACME Corporation")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery if no existing data" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserPhoneNumberRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
