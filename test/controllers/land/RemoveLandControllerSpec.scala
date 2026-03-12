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
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import forms.land.RemoveLandFormProvider
import models.land.DeleteLandReturn
import models.{FullReturn, Land, ReturnInfo, ReturnVersionUpdateReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.land.{LandOverviewRemovePage, RemoveLandPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.land.RemoveLandView

import scala.concurrent.Future

class RemoveLandControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveLandFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val removeLandRoute: String = controllers.land.routes.RemoveLandController.onPageLoad().url

  val mockConnector: StampDutyLandTaxConnector = mock[StampDutyLandTaxConnector]

  val testStorn = "TESTSTORN"

  val landWithLandId = Land(
    returnID = Some("221110168"),
    landResourceRef = Some("LND-REF-001"),
    landID = Some("LND-REF-001"),
    address1 = Some("Test"),
    address2 = Some("stafford"),
    postcode = Some("TE11 7HE"),
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnInfo = Some(ReturnInfo(
      version = Some("2")
    )),
    land = Some(Seq(
      landWithLandId
    ))
  )


  "RemoveLand Controller" - {

    "onPageLoad()" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()


        running(application) {

          val request = FakeRequest(GET, removeLandRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveLandView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, "Test")(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute)

          val view = application.injector.instanceOf[RemoveLandView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), "Test")(request, messages(application)).toString
        }
      }

      "must redirect to journey recovery if full return does not contains land for a GET" in {

        val userAnswers = emptyUserAnswers.set(LandOverviewRemovePage, "LND-REF-001").success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to LandOverviewController if full return does not contains landId for a GET" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))


        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

    }

    "onSubmit()" - {

      "must redirect to journey recovery if full return does not contain land for a POST" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn.copy(land = None)))
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removeLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(
          mockBackendConnector.updateReturnVersion(any())(any(), any())
        ).thenReturn(
          Future.successful(ReturnVersionUpdateReturn(Some(2)))
        )

        when(
          mockBackendConnector.deleteLand(any())(any(), any())
        ).thenReturn(
          Future.successful(DeleteLandReturn(true))
        )

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          ).build()

        running(application) {
          val request =
            FakeRequest(POST, removeLandRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value
          .set(LandOverviewRemovePage, "LND-REF-001").success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeLandRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[RemoveLandView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, "Test")(request, messages(application)).toString
        }
      }


      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn.copy(land = None)))
          .set(LandOverviewRemovePage, "LND-REF-001").success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeLandRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to land overview when no is selected" in {
        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removeLandRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must redirect to land overview when backend fails" in {
        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value
          .set(LandOverviewRemovePage, "LND-REF-001").success.value

        when(mockConnector.updateReturnVersion(any())(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.failed(new RuntimeException("simulated backend failure")))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockConnector)
          ).build()

        running(application) {
          val request = FakeRequest(POST, removeLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }

      "must redirect to the overview page when missing landId in LandOverviewRemovePage Page" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn)).set(RemoveLandPage, true).success.value

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(
          mockBackendConnector.updateReturnVersion(any())(any(), any())
        ).thenReturn(
          Future.successful(ReturnVersionUpdateReturn(Some(2)))
        )

        when(
          mockBackendConnector.deleteLand(any())(any(), any())
        ).thenReturn(
          Future.successful(DeleteLandReturn(true))
        )

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          ).build()

        running(application) {
          val request =
            FakeRequest(POST, removeLandRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
        }
      }


      "must redirect to land overview and set flash message when land is deleted" in {
        val testLand = Land(
          landID = Some("LND001"),
          returnID = Some("RET123456789"),
          propertyType = Some("04"),
          interestCreatedTransferred = Some("FREEHOLD"),
          houseNumber = Some("123"),
          address1 = Some("Baker Street"),
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("NW1 6XE"),
          landArea = Some("250.5"),
          areaUnit = Some("SQMETRE"),
          localAuthorityNumber = Some("5900"),
          mineralRights = Some("false"),
          NLPGUPRN = Some("10012345678"),
          willSendPlanByPost = Some("false"),
          titleNumber = Some("TGL123456"),
          landResourceRef = Some("LND-REF-001"),
          nextLandID = None,
          DARPostcode = Some("NW1 6XE")
        )

        val fullReturnWithLand = completeFullReturn.copy(
          land = Some(Seq(testLand))
        )

        val landId = testLand.landID.value

        val userAnswers = emptyUserAnswers.set(LandOverviewRemovePage, landId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(fullReturnWithLand))

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(
          mockBackendConnector.updateReturnVersion(any())(any(), any())
        ).thenReturn(
          Future.successful(ReturnVersionUpdateReturn(Some(2)))
        )

        when(
          mockBackendConnector.deleteLand(any())(any(), any())
        ).thenReturn(
          Future.successful(DeleteLandReturn(true))
        )

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          ).build()

        running(application) {
          val request = FakeRequest(POST, removeLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
          flash(result).get("landDeleted").value mustEqual testLand.address1.get
        }
      }

    }
  }

}
