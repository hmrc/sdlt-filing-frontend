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
import constants.FullReturnConstants.emptyFullReturn
import forms.purchaser.PurchaserCompanyTypeKnownFormProvider
import models.purchaser.{NameOfPurchaser, WhoIsMakingThePurchase}
import models.{CheckMode, FullReturn, NormalMode, Purchaser, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{NameOfPurchaserPage, PurchaserCompanyTypeKnownPage, WhoIsMakingThePurchasePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserCompanyTypeKnownView

import scala.concurrent.Future

class PurchaserCompanyTypeKnownControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserCompanyTypeKnownFormProvider()
  val form: Form[Boolean] = formProvider()
  private val testStorn = "TESTSTORN"

  lazy val purchaserCompanyTypeKnownRoute: String = controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(NormalMode).url
  lazy val purchaserCompanyTypeKnownCheckModeRoute: String = controllers.purchaser.routes.PurchaserCompanyTypeKnownController.onPageLoad(CheckMode).url


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

  private val companyNameOfPurchaser = NameOfPurchaser(
    forename1 = None,
    forename2 = None,
    name = "ACME Corporation"
  )
  val userAnswersWithCompanyAndName: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
      .set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

  private def fullReturnWithCompanyPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaser)))

  val userAnswersWithCompanyPurchaser: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))
      .set(NameOfPurchaserPage, companyPurchaser.toNameOfPurchaser).success.value
      .set(WhoIsMakingThePurchasePage, models.purchaser.WhoIsMakingThePurchase.Company).success.value
      .set(PurchaserCompanyTypeKnownPage,true).success.value

  val userAnswersWithCompanyPurchaserNameNone: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)

  implicit class PurchaserOps(p: Purchaser) {
    def toNameOfPurchaser = models.purchaser.NameOfPurchaser(p.forename1, p.forename2, p.surname.orElse(p.companyName).getOrElse(""))
  }

  "purchaserCompanyTypeKnown Controller" - {

    "GET Method Operations" - {

      "must return OK and correct view for company purchaser" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyAndName))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserCompanyTypeKnownRoute)
          val view = application.injector.instanceOf[PurchaserCompanyTypeKnownView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "ACME Corporation")(request, messages(application)).toString
        }
      }

      "must redirect to NameOfPurchaserController when company name is none" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaserNameNone))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserCompanyTypeKnownRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserCompanyTypeKnownRoute)

          val view = application.injector.instanceOf[PurchaserCompanyTypeKnownView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), NormalMode, "ACME Corporation")(request, messages(application)).toString
        }
      }
    }

    "POST Method Operations" - {

      "must redirect to purchaser name page when purchaser name is missing for a POST" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaserNameNone))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserCompanyTypeKnownRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }
      "must redirect to purchaserCompanyTypeController if Yes for company purchaser" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserCompanyTypeKnownRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to PurchaserTrusteeController if No for company purchaser" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserCompanyTypeKnownRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to PurchaserCheckYourAnswersController if Yes for company purchaser type known with CheckMode" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserCompanyTypeKnownCheckModeRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to PurchaserCheckYourAnswersController if No for company purchaser type known with CheckMode" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserCompanyTypeKnownCheckModeRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
        }
      }

      "must return BadRequest for invalid data" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserCompanyTypeKnownRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

          val boundForm = form.bind(Map("value" -> "invalid"))
          val view = application.injector.instanceOf[PurchaserCompanyTypeKnownView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "ACME Corporation")(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery if no existing data" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCompanyTypeKnownRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}