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
import constants.FullReturnConstants.{emptyFullReturn, incompleteFullReturn}
import models.land.*
import models.prelimQuestions.CompanyOrIndividualRequest
import models.{CheckMode, Land, ReturnInfo, ReturnVersionUpdateRequest, ReturnVersionUpdateReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.land.*
import pages.preliminary.PurchaserIsIndividualPage
import play.api.inject.bind
import play.api.libs.json.{JsNull, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class LandCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository   = mock[SessionRepository]
  private val mockBackendConnector    = mock[StampDutyLandTaxConnector]
  private val mockCheckAnswersService = mock[CheckAnswersService]

  implicit val hc:      HeaderCarrier        = HeaderCarrier()
  implicit val request: FakeRequest[_]       = FakeRequest()
  implicit val ec:      ExecutionContext     = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockBackendConnector)
  }

  private val completeLandFullReturn = incompleteFullReturn.copy(
    returnInfo = Some(ReturnInfo(version = Some("1"))),
    land = Some(Seq(Land(
      landID                     = Some("LAND001"),
      landResourceRef            = Some("LAND-REF-001"),
      propertyType               = Some("NonResidential"),
      interestCreatedTransferred = Some("Transfer"),
      address1                   = Some("1 Test Street"),
      address2                   = Some("Test Town"),
      houseNumber                = None,
      address3                   = None,
      address4                   = None,
      postcode                   = Some("AB1 2CD"),
      landArea                   = None,
      areaUnit                   = None,
      localAuthorityNumber       = Some("1234"),
      mineralRights              = None,
      NLPGUPRN                   = None,
      willSendPlanByPost         = None,
      titleNumber                = None,
      nextLandID                 = None
    )))
  )

  private def landCurrentData(landId: Option[String] = None) = Json.obj(
    "landCurrent" -> Json.obj(
      "landId"                           -> Json.toJson(landId),
      "propertyType"                     -> "03",
      "landInterestTransferredOrCreated" -> "FG",
      "landAddress" -> Json.obj(
        "houseNumber"      -> JsNull,
        "line1"            -> "1 Test Street",
        "line2"            -> "Test Town",
        "line3"            -> JsNull,
        "line4"            -> JsNull,
        "line5"            -> JsNull,
        "postcode"         -> "AB1 2CD",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "UK"
        ),
        "addressValidated" -> true
      ),
      "localAuthorityCode"              -> "0220",
      "landRegisteredHmRegistry"        -> false,
      "landAddNlpgUprn"                 -> false,
      "landSendingPlanByPost"           -> false,
      "landMineralsOrMineralRights"     -> false,
      "agriculturalOrDevelopmentalLand" -> false
    )
  )

  private val authorityCodeFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-9.welsh6996_6997.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  private val postcodeFailure = CrossFlowFailure(
    ruleId         = "Cf-16",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-16.body",
    inlineErrorKey = "crossflow.land.Cf-16.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-16.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPostcode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  private val propertyTypeFailure = CrossFlowFailure(
    ruleId         = "Cf-3",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-3.body",
    inlineErrorKey = "crossflow.land.Cf-3.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-3.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-3.heading"
  )

  private def crossFlowWithFailures(forPage: Map[PageId, Seq[CrossFlowFailure]]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] =
        forPage.getOrElse(page, Nil)
    }

  private val crossFlowSilent = new CrossFlowValidationService(Set.empty, Set.empty) {
    override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] = Nil
  }

  "LandCheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskListController when the UserAnswers data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when data is empty but session is not" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = None,
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to ReturnTaskList when data is empty for GET" in {

        val userAnswers = UserAnswers(id = "", storn = "TESTSTORN", returnId = Some("AB2346")).copy(fullReturn = Some(emptyFullReturn))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when data is empty in GET" in {

        val userAnswers = UserAnswers(id = "12345", storn = "TESTSTORN", returnId = None, data = Json.obj())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to BeforeStartReturnController when data is null for GET" in {

        val userAnswers = UserAnswers(id = "12345", storn = "TESTSTORN", returnId = Some("AB2346"), data = Json.obj())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to LandBeforeYouStartController when the land current UserAnswers data is empty" in {

        val userAnswers = emptyUserAnswers.copy(returnId = Some("RE12345"))
          .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must return OK and show agricultural row when property type is NonResidential and agricultural is true" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value
          .set(DoYouKnowTheAreaOfLandPage, false).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and show know area row when property type is NonResidential, agricultural is true and knowArea is true" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value
          .set(DoYouKnowTheAreaOfLandPage, true).success.value
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
          .set(AreaOfLandPage, "100.000").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Do you know the area of the land?")
          contentAsString(result) must include("Area of land")
        }
      }

      "must return OK and show title number row when HM Registry is true" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandRegisteredHmRegistryPage, true).success.value
          .set(LandTitleNumberPage, "12345").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Is the land or property registered with HM Land Registry?")
          contentAsString(result) must include("Title number")
        }
      }

      "must return OK and show NLPG UPRN row when add NLPG is true" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandAddNlpgUprnPage, true).success.value
          .set(LandNlpgUprnPage, "10012345678").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Do you have an NLPG UPRN?")
          contentAsString(result) must include("NLPG UPRN")
        }
      }

      "must return OK and show Mixed property type rows" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Does the transaction involve agricultural or developmental land?")
        }
      }

      "must return redirect call when page is missing" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value

        val redirectCall = controllers.land.routes.DoYouKnowTheAreaOfLandController.onPageLoad(CheckMode)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockCheckAnswersService.redirectOrRender(any()))
          .thenReturn(Left(redirectCall))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CheckAnswersService].toInstance(mockCheckAnswersService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          redirectLocation(result).value must include("about-the-land/add-area-of-land/change")
        }
      }

      "must redirect to LocalAuthorityCode in CheckMode when cross-flow reports an authority-code failure for the session land" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val crossFlow = crossFlowWithFailures(Map(
          Pages.LandAuthorityCode -> Seq(authorityCodeFailure)
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlow)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url
        }
      }

      "must redirect to LandAuthorityCodeSingleEntity with the session landId when cross-flow reports a postcode failure" in {

        val landId = "LAND001"

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData(Some(landId))
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val crossFlow = crossFlowWithFailures(Map(
          Pages.LandPostcode -> Seq(postcodeFailure)
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlow)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad(landId).url
        }
      }

      "must redirect to JourneyRecovery when cross-flow reports a postcode failure but no landId is in session" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData(landId = None)
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val crossFlow = crossFlowWithFailures(Map(
          Pages.LandPostcode -> Seq(postcodeFailure)
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlow)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to LandTypeOfProperty in CheckMode when cross-flow reports a property-type failure for the session land (Cf-3)" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val crossFlow = crossFlowWithFailures(Map(
          Pages.LandPropertyType -> Seq(propertyTypeFailure)
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlow)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
        }
      }

      "must render the CYA normally when cross-flow reports no failures for the session land" in {

        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must render the CYA normally when cross-flow filters Cf-6 (aggregate-only) from page-level failures" in {
        val userAnswers = UserAnswers(
          id       = "12345",
          returnId = Some("AB2346"),
          storn    = "TESTSTORN",
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        // Stub `failuresForPage` to return nothing (mimicking the dispatcher
        // having already filtered out Cf-6 because aggregateOnly = true).
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowSilent)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must create land and redirect to LandOverview when all required data is present and no land ID present" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createLand(any())(any(), any()))
          .thenReturn(Future.successful(CreateLandReturn(landResourceRef = "LAND-REF-001", landId = "LAND001")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createLand(any())(any(), any())
        }
      }

      "must update land and redirect to LandOverview when all required data is present and land ID present" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData(Some("LAND001"))
        ).set(LandOverviewPage, "LAND001").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(newVersion = Some(2))))
        when(mockBackendConnector.updateLand(any[UpdateLandRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLandReturn(updated = true)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateLand(any())(any(), any())
        }
      }

      "must redirect back to LandCheckYourAnswers when update returns false" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData(Some("LAND001"))
        ).set(LandOverviewPage, "LAND001").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(newVersion = Some(2))))
        when(mockBackendConnector.updateLand(any[UpdateLandRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLandReturn(updated = false)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when create returns empty landId" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createLand(any())(any(), any()))
          .thenReturn(Future.successful(CreateLandReturn(landResourceRef = "LAND-REF-001", landId = "")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when title number validation fails" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        ).set(LandRegisteredHmRegistryPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when NLPG UPRN validation fails" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        ).set(LandAddNlpgUprnPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when agricultural validation fails" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        ).set(AgriculturalOrDevelopmentalLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when area of land validation fails" in {

        val userAnswers = UserAnswers(
          id         = "test-session-id",
          storn      = "test-storn",
          returnId   = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data       = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value
          .set(DoYouKnowTheAreaOfLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers on JsError" in {

        val userAnswers = UserAnswers(
          id       = "test-session-id",
          storn    = "test-storn",
          returnId = Some("12345"),
          data     = Json.obj("landCurrent" -> "invalid")
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no session data is found on submit" in {

        val userAnswers = UserAnswers(
          id       = "test-session-id",
          storn    = "test-storn",
          returnId = Some("12345"),
          data     = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}