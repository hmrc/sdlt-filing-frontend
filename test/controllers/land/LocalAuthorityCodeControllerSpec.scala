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
import views.html.land.LocalAuthorityCodeView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class LocalAuthorityCodeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val testStorn = "TESTSTORN"
  val requiredKey = "localAuthorityCode.error.required"
  val maxLength = 4
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val effectiveTransactionDate: Option[LocalDate] = Option(LocalDate.parse("2024-04-01", formatter))
  val contractEffDate: Option[LocalDate] = Option(LocalDate.parse("2024-03-01", formatter))
  val postcode: Option[String] = Some("TE1 7NQ")
  val formProvider = new LocalAuthorityCodeFormProvider()
  val form: Form[String] = formProvider(effectiveTransactionDate, contractEffDate, postcode)

  lazy val localAuthorityCodeRoute: String = controllers.land.routes.LocalAuthorityCodeController.onPageLoad(NormalMode).url

  val completeTransaction: Transaction = Transaction(
    transactionID = Some("TXN001"),
    returnID = Some("RET123456789"),
    contractDate = Some("2010-09-15"),
    effectiveDate = Some("2024-10-01"),
    exchangedLandAddress4 = None
  )

  val completeLandForUK: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    postcode = Some("TE1 6XE")
  )


  val completeLandForScotland: Land = Land(
    landID = Some("LND002"),
    returnID = Some("RET123456788"),
    postcode = Some("AB1 6XE")
  )

  val lands: Option[Seq[Land]] = Some(Seq(completeLandForUK, completeLandForScotland))
  val transactions : Option[Transaction] = Some(completeTransaction)

  private def fullReturnWithReqData: FullReturn = {
    emptyFullReturn.copy(land = lands)
                   .copy(transaction = transactions)
  }

  val userAnswersData: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithReqData))

  val testAddress: Address = Address(
    "16 Coniston Court",
    Some("Holland road"),
    None,
    None,
    None,
    Some("RG1 7NQ"),
    Some(Country(Some("UK"), Some("United Kingdom"))),
    false
  )

  val testAddressScotland: Address = Address(
    "16 Coniston Court",
    Some("Holland road"),
    None,
    None,
    None,
    Some("AB1 6XE"),
    Some(Country(Some("UK"), Some("United Kingdom"))),
    false
  )


  "LocalAuthorityCode Controller" - {
    "On Load()" - {
      "must return OK and the correct view for a GET" in {

        val userAnswers: UserAnswers = emptyUserAnswers.set(LandAddressPage, testAddress).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[LocalAuthorityCodeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val form = formProvider(Some(LocalDate.parse("2019-12-31")), Some(LocalDate.parse("2019-12-31")), Some("RG1 7NQ"))

        val userAnswers = emptyUserAnswers
          .set(LandAddressPage, testAddress).success.value
          .set(LocalAuthorityCodePage, "1234").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val view = application.injector.instanceOf[LocalAuthorityCodeView]
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill("1234"), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Confirm Land or Property Address page is not address found in session" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(NormalMode).url
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

      "must populate the view correctly on a GET when the question has previously been answered via model object" in {

        val userAnswers = userAnswersData
          .set(LandAddressPage, testAddress).success.value
          .set(LocalAuthorityCodePage, "1234").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, localAuthorityCodeRoute)
          val view = application.injector.instanceOf[LocalAuthorityCodeView]
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill("1234"), NormalMode)(request, messages(application)).toString
        }
      }
    }

    "On Submit()" - {
      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers
          .set(LandAddressPage, testAddress).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, localAuthorityCodeRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view = application.injector.instanceOf[LocalAuthorityCodeView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, localAuthorityCodeRoute)
              .withFormUrlEncodedBody(("value", "123456"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "Local authority local codes validation response with success " - {

        "must redirect to next page when valid dummy authcode send -8999" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2010-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "8999"))

            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid dummy authcode send -8998" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2010-09-15"),
            effectiveDate = Some("2024-10-01"),
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "8998"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid data Uk authcode - 0335 is submitted" in {

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0335"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid data Uk authcode- 0114 is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0114"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid data Uk authcode - 0630 is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0630"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return redirect to the next page when valid uk auth - 0630  empty eff and contract dates" in {

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForUK)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0630"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid data Uk authcode- 0220 is submitted with empty eff and contract dates" in {

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0220"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must redirect to the next page when valid welsh authcode - 6996 is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "6996"))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
      }

      "Local authority local codes validation response with failure. It will display error message " - {

        "must return invalid error message for dummy authcode send -8999" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForScotland)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "8999"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST
            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid dummy authcode send -8998" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddressScotland).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "8998"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid uk auth - 0335  with Scotland Postcode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForScotland)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddressScotland).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0335"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid uk auth - 0114  with Scotland Postcode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForScotland)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddressScotland).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0114"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid uk auth - 0220  with Scotland Postcode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForScotland)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddressScotland).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0220"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid uk auth - 0335 with Scotland Postcode, older eff and contract dates" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2011-09-15"),
            effectiveDate = Some("2010-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLandForScotland)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddressScotland).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0335"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for welsh authcode - 6805 with invalid dates" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2024-09-15"),
            effectiveDate = Some("2024-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "6805"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }

        "must return invalid error message for valid data Uk authcode - 0724 is submitted with older eff and contract dates" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val completeTransaction = Transaction(
            transactionID = Some("TXN001"),
            returnID = Some("RET123456789"),
            contractDate = Some("2010-09-15"),
            effectiveDate = Some("2010-10-01"),
            exchangedLandAddress4 = None
          )

          def fullReturnWithTransaction: FullReturn =
            emptyFullReturn.copy(transaction = Some(completeTransaction),
              land = Some(Seq(completeLand)))

          val userWithTransaction: UserAnswers =
            UserAnswers(userAnswersId, storn = testStorn)
              .copy(fullReturn = Some(fullReturnWithTransaction))
              .set(LandAddressPage, testAddress).success.value

          val application =
            applicationBuilder(userAnswers = Some(userWithTransaction))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, localAuthorityCodeRoute)
                .withFormUrlEncodedBody(("value", "0724"))

            val result = route(application, request).value
            status(result) mustEqual BAD_REQUEST

            val htmlData = contentAsString(result)
            htmlData must include("Provide valid local authority code")
          }
        }
      }
    }
  }
}