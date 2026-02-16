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

package controllers.purchaserAgent

import base.SpecBase
import forms.purchaserAgent.SelectPurchaserAgentFormProvider
import models.purchaser.NameOfPurchaser
import models.purchaserAgent.*
import models.{Agent, FullReturn, NormalMode, Purchaser}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.{PurchaserAgentBeforeYouStartPage, SelectPurchaserAgentPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserService
import services.purchaserAgent.PurchaserAgentService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.purchaserAgent.SelectPurchaserAgentView

import scala.concurrent.Future


class SelectPurchaserAgentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectPurchaserAgentRoute: String = controllers.purchaserAgent.routes.SelectPurchaserAgentController.onPageLoad(NormalMode).url

  val testStorn = "STN005"

  private def createAgent(
                           agentId: Option[String] = None,
                           name: Option[String] = None,
                           houseNumber: Option[String] = None,
                           address1: Option[String] = Some("123 Street"),
                           address2: Option[String] = Some("Town"),
                           address3: Option[String] = Some("City"),
                           address4: Option[String] = Some("County"),
                           postcode: Option[String] = Some("AA1 1AA"),
                           phone: Option[String] = Some("0123456789"),
                           email: Option[String] = Some("test@example.com"),
                           dxAddress: Option[String] = Some("yes"),
                           agentResourceReference: Option[String] = Some("REF001")
                         ) : Agent =
    Agent(
      storn = Some(testStorn),
      agentId = agentId,
      name = name,
      houseNumber = houseNumber,
      address1 = address1,
      address2 = address2,
      address3 = address3,
      address4 = address4,
      postcode = postcode,
      phone = phone,
      email = email,
      dxAddress = dxAddress,
      agentResourceReference = agentResourceReference
    )

  val agentList: Seq[Agent] = Seq(
    createAgent(agentId = Some("AGT001"), name = Some("John Smith")),
    createAgent(agentId = Some("AGT002"), name = Some("Jane Jones"))
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnInfo = Some(models.ReturnInfo(mainPurchaserID = Some("P001"))),
    purchaser = Some(Seq(
      Purchaser(
        purchaserID = Some("P001"),
        surname = Some("Jones"),
        forename1 = Some("Sarah"),
        forename2 = None,
        companyName = None
      )
    )),
    agent = Some(agentList)
  )

  private val testFullReturnWithNoAgents = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnInfo = Some(models.ReturnInfo(mainPurchaserID = Some("P001"))),
    purchaser = Some(Seq(
      Purchaser(
        purchaserID = Some("P001"),
        surname = Some("Jones"),
        forename1 = Some("Sarah"),
        forename2 = None,
        companyName = None
      )
    )),
    agent = None
  )

  private val testFullReturnWithEmptyListOfAgents = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnInfo = Some(models.ReturnInfo(mainPurchaserID = Some("P001"))),
    purchaser = Some(Seq(
      Purchaser(
        purchaserID = Some("P001"),
        surname = Some("Jones"),
        forename1 = Some("Sarah"),
        forename2 = None,
        companyName = None
      )
    )),
    agent = Some(List.empty)
  )

  val agentSummaryList: Seq[(String, Option[String])] =
    agentList.map { agent =>
      val displayName = Seq(agent.name, agent.address3).flatten.mkString(", ")
      (displayName, agent.agentId.map(_.toString))
    }

  val formProvider = new SelectPurchaserAgentFormProvider()
  val form: Form[String] = formProvider(agentList)

  val mockPurchaserService: PurchaserService = mock[PurchaserService]
  val mockPurchaserAgentService: PurchaserAgentService = mock[PurchaserAgentService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "SelectPurchaserAgent Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when agent list exists" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(agentSummaryList)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService),
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[SelectPurchaserAgentView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Sarah Jones", Some(agentSummaryList))(request, messages(application)).toString
        }
      }

      "must redirect to Purchaser Agent Name Page for a GET when agent is None" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(Seq.empty)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturnWithNoAgents))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Purchaser Agent Name Page for a GET when agent list is empty" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(Seq.empty)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturnWithEmptyListOfAgents))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(agentSummaryList)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value
          .set(SelectPurchaserAgentPage, SelectPurchaserAgent.values.head.toString).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Sarah Jones")
        }
      }

      "must redirect to Name of Purchaser page for a GET when main purchaser name does not exist" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(None)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Return Task List when Before you start does not equal yes for a GET" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to Return Task List for a GET when Before you start equals Yes but there is no storn" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = None)
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to Name of Purchaser page for a get when main purchaser name does not exist for a POST" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(None)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, selectPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "value"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Purchaser Agent Name Page for a POST when agent is None" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(Seq.empty)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturnWithNoAgents))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, selectPurchaserAgentRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Return Task List when main purchaser name exists but no storn for a POST" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = None)
          .set(PurchaserAgentBeforeYouStartPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, selectPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "value"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(agentSummaryList)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, selectPurchaserAgentRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))
          val view = application.injector.instanceOf[SelectPurchaserAgentView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Sarah Jones", Some(agentSummaryList))(request, messages(application)).toString
        }
      }

      "must call handleAgentSelection and redirect to Do you want to add a reference for this return when a valid agent is selected" in {
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(agentSummaryList)
        when(mockPurchaserAgentService.handleAgentSelection(any(), any[Seq[Agent]], any(), any()))
          .thenReturn(Future.successful(Redirect(onwardRoute.url)))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, selectPurchaserAgentRoute)
            .withFormUrlEncodedBody("value" -> "AGT001")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when no data is submitted" in {
        when(mockPurchaserService.mainPurchaserName(any())).thenReturn(Some(NameOfPurchaser(None, None, "Sarah Jones")))
        when(mockPurchaserAgentService.agentSummaryList(any())).thenReturn(agentSummaryList)

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(testFullReturn))
          .set(PurchaserAgentBeforeYouStartPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[PurchaserAgentService].toInstance(mockPurchaserAgentService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, selectPurchaserAgentRoute)
              .withFormUrlEncodedBody("value" -> "")

          val boundForm = form.bind(Map("value" -> ""))
          val view = application.injector.instanceOf[SelectPurchaserAgentView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Sarah Jones", Some(agentSummaryList))(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, selectPurchaserAgentRoute)
              .withFormUrlEncodedBody(("value", SelectPurchaserAgent.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}