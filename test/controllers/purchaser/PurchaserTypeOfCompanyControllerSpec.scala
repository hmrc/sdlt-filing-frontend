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
import forms.purchaser.PurchaserTypeOfCompanyFormProvider
import models.purchaser.{PurchaserTypeOfCompany, PurchaserTypeOfCompanyAnswers, WhoIsMakingThePurchase}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{PurchaserTypeOfCompanyPage, WhoIsMakingThePurchasePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserTypeOfCompanyView

import java.time.Instant
import scala.concurrent.Future

class PurchaserTypeOfCompanyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val purchaserTypeOfCompanyRoute = controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(NormalMode).url

  val testUserAnswersCompany = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> "John",
          "forename2" -> "Middle",
          "name" -> "Doe"
        ),
        "whoIsMakingThePurchase" -> "Company"
      )
    ),
    lastUpdated = Instant.now
  )

  val testUserAnswersIndividual = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> "John",
          "forename2" -> "Middle",
          "name" -> "Doe"
        ),
        "whoIsMakingThePurchase" -> "Individual"
      )
    ),
    lastUpdated = Instant.now
  )

  val formProvider = new PurchaserTypeOfCompanyFormProvider()
  val form = formProvider()

  "PurchaserTypeOfCompany Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersCompany)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserTypeOfCompanyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserTypeOfCompanyView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswersCompany.set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
        buildingAssociation = "YES",
        centralGovernment = "NO",
        individualOther = "NO",
        insuranceAssurance = "NO",
        localAuthority = "NO",
        partnership = "NO",
        propertyCompany = "NO",
        publicCorporation = "NO",
        otherCompany = "NO",
        otherFinancialInstitute = "NO",
        otherIncludingCharity = "NO",
        superannuationOrPensionFund = "NO",
        unincorporatedBuilder = "NO",
        unincorporatedSoleTrader = "NO")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserTypeOfCompanyRoute)

        val view = application.injector.instanceOf[PurchaserTypeOfCompanyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Set(PurchaserTypeOfCompany.Bank, PurchaserTypeOfCompany.BuildingAssociation)), NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswersCompany))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserTypeOfCompanyRoute)
            .withFormUrlEncodedBody(("value[0]", PurchaserTypeOfCompany.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersCompany)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserTypeOfCompanyRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PurchaserTypeOfCompanyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserTypeOfCompanyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserTypeOfCompanyRoute)
            .withFormUrlEncodedBody(("value[0]", PurchaserTypeOfCompany.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Generic Error Page when purchaser type is individual and does not match for a GET" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserTypeOfCompanyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
      }
    }

    "must redirect to purchaser name page when name is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserTypeOfCompanyRoute)
            .withFormUrlEncodedBody(("value", PurchaserTypeOfCompany.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "redirect to PurchaserNamePage when the purchaser name data is missing for a GET" in {
      
      val userAnswersWithCompany = UserAnswers(userAnswersId, storn = "123456")
        .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
      
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompany)).build()
      
      running(application) {
        val request = FakeRequest(GET, purchaserTypeOfCompanyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }
  }
}
