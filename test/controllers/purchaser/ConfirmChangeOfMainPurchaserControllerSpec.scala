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
import constants.FullReturnConstants
import controllers.routes
import forms.purchaser.ConfirmChangeOfMainPurchaserFormProvider
import models.{FullReturn, Purchaser, ReturnInfo, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{ChangePurchaserOnePage, ConfirmChangeOfMainPurchaserPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.{PurchaserService, PurchaserUpdateMainPurchaserService}
import views.html.purchaser.ConfirmChangeOfMainPurchaserView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmChangeOfMainPurchaserControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ConfirmChangeOfMainPurchaserFormProvider()
  val form = formProvider()

  lazy val confirmChangeOfMainPurchaserRoute: String = controllers.purchaser.routes.ConfirmChangeOfMainPurchaserController.onPageLoad().url
  lazy val purchaserCheckYourAnswersRoute: String = controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private def createPurchaser(
                               id: String,
                               forename1: Option[String] = None,
                               forename2: Option[String] = None,
                               surname: Option[String] = None,
                               companyName: Option[String] = None,
                               isCompany: Option[String] = Some("NO"),
                               nextPurchaserID: Option[String] = None,
                               purchaserRef: Option[String] = Some("1")
                             ): Purchaser = Purchaser(
    purchaserID = Some(id),
    forename1 = forename1,
    forename2 = forename2,
    surname = surname,
    companyName = companyName,
    isCompany = isCompany,
    nextPurchaserID = nextPurchaserID,
    isTrustee = Some("YES"),
    isConnectedToVendor = Some("NO"),
    isRepresentedByAgent = Some("NO"),
    address1 = Some("123 Test Street"),
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None,
    purchaserResourceRef = purchaserRef
  )

  val returnInfoWithMainPurchaser: ReturnInfo = FullReturnConstants.completeReturnInfo.copy(mainPurchaserID = Some("PUR001"))

  val seqOfPurchasers: Seq[Purchaser] = Seq(
    createPurchaser(id = "PUR001", surname = Some("Ralph"), isCompany = Some("No")),
    createPurchaser("PUR002", surname = Some("John"), isCompany = Some("No")),
    createPurchaser("PUR003", companyName = Some("Company"), isCompany = Some("Yes")))

  val fullReturn: FullReturn = FullReturnConstants.emptyFullReturn.copy(
    returnInfo = Some(returnInfoWithMainPurchaser),
    purchaser = Some(seqOfPurchasers))

  val testUserAnswers: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

  "ConfirmChangeOfMainPurchaser Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = testUserAnswers.set(ChangePurchaserOnePage, "PUR002").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmChangeOfMainPurchaserView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, "John")(request, messages(application)).toString
      }
    }

    "must redirect to Change Purchaser One when the purchaser id is missing from the session for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.ChangePurchaserOneController.onPageLoad().url
      }
    }

    "must redirect to Change Purchaser One when the purchaser id is missing from the session for a POST" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.ChangePurchaserOneController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the name is missing from the purchaser in full return for a GET" in {

      val seqOfPurchasers: Seq[Purchaser] = Seq(
        createPurchaser(id = "PUR001", surname = Some("John")),
        createPurchaser("PUR002", surname = None),
        createPurchaser("PUR003", surname = Some("John")))

      val fullReturn: FullReturn = FullReturnConstants.emptyFullReturn.copy(
        returnInfo = Some(returnInfoWithMainPurchaser),
        purchaser = Some(seqOfPurchasers))

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(fullReturn))
        .set(ChangePurchaserOnePage, "PUR002").success.value

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the name is missing from the purchaser in full return for a POST" in {

      val seqOfPurchasers: Seq[Purchaser] = Seq(
        createPurchaser(id = "PUR001", surname = Some("John")),
        createPurchaser("PUR002", surname = None),
        createPurchaser("PUR003", surname = Some("John")))

      val fullReturn: FullReturn = FullReturnConstants.emptyFullReturn.copy(
        returnInfo = Some(returnInfoWithMainPurchaser),
        purchaser = Some(seqOfPurchasers))

      val testUserAnswers: UserAnswers = emptyUserAnswers
        .copy(fullReturn = Some(fullReturn))
        .set(ChangePurchaserOnePage, "PUR002").success.value

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers
        .set(ChangePurchaserOnePage, "PUR002").success.value
        .set(ConfirmChangeOfMainPurchaserPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmChangeOfMainPurchaserRoute)

        val view = application.injector.instanceOf[ConfirmChangeOfMainPurchaserView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), "John")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when the answer is 'Yes'" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockPurchaserUpdateMainPurchaserService = mock[PurchaserUpdateMainPurchaserService]
      val mockPurchaserService = mock[PurchaserService]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val userAnswers = testUserAnswers.set(ChangePurchaserOnePage, "PUR002").success.value

      when(
        mockPurchaserUpdateMainPurchaserService
          .updateMainPurchaser(any())(any(), any(), any())
      ).thenReturn(Future.successful(Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())))

      when(mockPurchaserService.getPurchaserNameById(any(), any()))
        .thenReturn(Some("John"))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[PurchaserUpdateMainPurchaserService].toInstance(mockPurchaserUpdateMainPurchaserService),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[SessionRepository].toInstance(mockSessionRepository),
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmChangeOfMainPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to the Purchaser Overview page when the answer is 'No'" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockPurchaserUpdateMainPurchaserService = mock[PurchaserUpdateMainPurchaserService]
      val mockPurchaserService = mock[PurchaserService]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val userAnswers = testUserAnswers.set(ChangePurchaserOnePage, "PUR002").success.value

      when(
        mockPurchaserUpdateMainPurchaserService
          .updateMainPurchaser(eqTo(userAnswers))(any(), any(), any())
      ).thenReturn(Future.successful(Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())))

      when(mockPurchaserService.getPurchaserNameById(any(), any()))
        .thenReturn(Some("John"))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[PurchaserUpdateMainPurchaserService].toInstance(mockPurchaserUpdateMainPurchaserService),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[SessionRepository].toInstance(mockSessionRepository),
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmChangeOfMainPurchaserRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = testUserAnswers.set(ChangePurchaserOnePage, "PUR002").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmChangeOfMainPurchaserRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ConfirmChangeOfMainPurchaserView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "John")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmChangeOfMainPurchaserRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmChangeOfMainPurchaserRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
