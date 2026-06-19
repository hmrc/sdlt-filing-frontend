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

package services.land

import org.scalatest.matchers.must.Matchers
import play.api.mvc.Results
import models.{Land, NormalMode, ReturnInfo, UserAnswers}
import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.land.LandTypeOfProperty
import pages.land.LandTypeOfPropertyPage
import play.api.i18n.Messages
import play.api.test.Helpers.*
import services.crossflow.{CrossFlowBody, CrossFlowFailure, CrossFlowTarget, Pages, ReturnSection}

import scala.concurrent.Future

class LandServiceSpec extends SpecBase with Matchers {

  private val service = new LandService()

  private val continueRoute = Results.Ok("continue")

  private val landA = Land(landID = Some("LND001"), address1 = Some("Baker Street"),  postcode = Some("NW1 6XE"), propertyType = Some("01"))
  private val landB = Land(landID = Some("LND002"), address1 = Some("Cardiff Castle"), postcode = Some("CF10 3RB"), propertyType = Some("03"))
  private val landC = Land(landID = Some("LND003"), address1 = Some("Park Lane"),     postcode = Some("W1K 1LB"),  propertyType = Some("02"))

  private val authorityCodeFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-9.welsh6996_6997.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  "propertyTypeCheck" - {

    "must return continueRoute when property type is mixed" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed)
        .success
        .value

      val result = service.propertyTypeCheck(userAnswers, continueRoute)

      result mustBe continueRoute
    }

    "must return continueRoute when property type is non-residential" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential)
        .success
        .value

      val result = service.propertyTypeCheck(userAnswers, continueRoute)

      result mustBe continueRoute
    }

    "must redirect to LandTypeOfPropertyPage when property type is none" in {
      val userAnswers: UserAnswers = emptyUserAnswers

      val result = service.propertyTypeCheck(userAnswers, continueRoute)

      result.header.status mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode).url)
    }

    "must redirect to CYA when property type is Residential" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential)
        .success
        .value

      val result = service.propertyTypeCheck(userAnswers, continueRoute)

      result.header.status mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some(controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
    }

    "must redirect to CYA when property type is Additional" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Additional)
        .success
        .value

      val result = service.propertyTypeCheck(userAnswers, continueRoute)

      result.header.status mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some(controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
    }
  }

  "getMainLand" - {
    "must return the main land" - {
      "when only one land in the list" in {
        val singleLand = Land(landID = Some("LND001"))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            land = Some(Seq(singleLand)),
            returnInfo = Some(ReturnInfo(mainLandID = Some("LND001")))
          )))

        service.getMainLand(userAnswers) mustBe Some(singleLand)
      }

      "when multiple lands in the list" in {
        val land1 = Land(landID = Some("LND001"))

        val land2 = Land(landID = Some("LND002"))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            land = Some(Seq(land1, land2)),
            returnInfo = Some(ReturnInfo(mainLandID = Some("LND001")))
          )))

        service.getMainLand(userAnswers) mustBe Some(land1)
      }
    }

    "must return None" - {
      "when the mainLandId doesn't match any in the land list" in {
        val land = Land(landID = Some("LND001"))

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            land = Some(Seq(land)),
            returnInfo = Some(ReturnInfo(mainLandID = Some("LND002")))
          )))

        service.getMainLand(userAnswers) mustBe None
      }

      "when fullReturn is None" in {
        val userAnswers = emptyUserAnswers
          .copy(fullReturn = None)

        service.getMainLand(userAnswers) mustBe None
      }
    }
  }

  "isMainLand" - {
    "must return true" - {
      "when the land id is in main land" in {
        val landId = "LND001"

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainLandID = Some("LND001")))
          )))

        service.isMainLand(userAnswers, landId) mustBe true
      }
    }

    "must return false" - {
      "when the land id is not a main land" in {
        val landId = "LND002"

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainLandID = Some("LND001")))
          )))

        service.isMainLand(userAnswers, landId) mustBe false
      }

      "when main land is None" in {
        val landId = "LND001"

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(emptyFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainLandID = None))
          )))

        service.isMainLand(userAnswers, landId) mustBe false
      }
    }
  }

  "generateLandPropertyTypeRows" - {

    "must return an empty Seq when no lands are provided" in {
      service.generateLandPropertyTypeRows(allLands = Nil, mismatchLands = Nil) mustBe empty
    }

    "must return a row for every land in allLands" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB, landC), Nil)

      rows.size mustBe 3
      rows.map(_.landId) mustBe Seq("LND001", "LND002", "LND003")
    }

    "must mark only the lands in mismatchLands as mismatch" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB, landC), Seq(landA, landC))

      rows.find(_.landId == "LND001").value.isMismatch mustBe true
      rows.find(_.landId == "LND002").value.isMismatch mustBe false
      rows.find(_.landId == "LND003").value.isMismatch mustBe true
    }

    "must render the property type display from the code" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB, landC), Nil)

      rows.find(_.landId == "LND001").value.propertyTypeDisplay mustBe "01 - Residential"
      rows.find(_.landId == "LND002").value.propertyTypeDisplay mustBe "03 - Non-residential"
      rows.find(_.landId == "LND003").value.propertyTypeDisplay mustBe "02 - Mixed"
    }

    "must render '04 - Additional residential property liable to higher rate' for code 04" in {
      val land04 = Land(landID = Some("LND004"), propertyType = Some("04"))

      val rows = service.generateLandPropertyTypeRows(Seq(land04), Nil)

      rows.head.propertyTypeDisplay mustBe "04 - Additional residential property liable to higher rate"
    }

    "must render the raw code when the property type is unrecognised" in {
      val landWeird = Land(landID = Some("LND099"), propertyType = Some("99"))

      val rows = service.generateLandPropertyTypeRows(Seq(landWeird), Nil)

      rows.head.propertyTypeDisplay mustBe "99"
    }

    "must render an empty string when the property type is None" in {
      val landNoPt = Land(landID = Some("LND099"), propertyType = None)

      val rows = service.generateLandPropertyTypeRows(Seq(landNoPt), Nil)

      rows.head.propertyTypeDisplay mustBe ""
    }

    "must use 'address1, postcode' as the label when both are present" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA), Nil)

      rows.head.label mustBe "Baker Street, NW1 6XE"
    }

    "must use address1 alone when postcode is missing" in {
      val landNoPostcode = Land(landID = Some("LND001"), address1 = Some("Baker Street"), postcode = None)

      val rows = service.generateLandPropertyTypeRows(Seq(landNoPostcode), Nil)

      rows.head.label mustBe "Baker Street"
    }

    "must fall back to 'Land N' when address1 is missing" in {
      val landNoAddress  = Land(landID = Some("LND001"), address1 = None, postcode = Some("NW1 6XE"))
      val landNoAddress2 = Land(landID = Some("LND002"), address1 = None, postcode = Some("CF10 3RB"))

      val rows = service.generateLandPropertyTypeRows(Seq(landNoAddress, landNoAddress2), Nil)

      rows.head.label    mustBe "Land 1"
      rows(1).label      mustBe "Land 2"
    }

    "must point updateUrl at LandPropertyTypeMultiEntityController.updateLand for each land" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB), Nil)

      rows.head.updateUrl mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.updateLand("LND001").url
      rows(1).updateUrl   mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.updateLand("LND002").url
    }

    "must point removeUrl at LandPropertyTypeMultiEntityController.removeLand for each land" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB), Nil)

      rows.head.removeUrl mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.removeLand("LND001").url
      rows(1).removeUrl   mustBe controllers.land.routes.LandPropertyTypeMultiEntityController.removeLand("LND002").url
    }

    "must set canRemove to true when there is more than one land" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA, landB), Nil)

      rows.foreach(_.canRemove mustBe true)
    }

    "must set canRemove to false when there is only one land" in {
      val rows = service.generateLandPropertyTypeRows(Seq(landA), Nil)

      rows.head.canRemove mustBe false
    }

    "must use an empty string as landId when a land has no landID" in {
      val landNoId = Land(landID = None, address1 = Some("Anonymous Lane"))

      val rows = service.generateLandPropertyTypeRows(Seq(landNoId), Nil)

      rows.head.landId mustBe ""
    }
  }

  "generateLandErrors" - {

    "must return an empty SummaryList when no failures are provided" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val result = service.generateLandErrors(Nil)

        result.rows mustBe empty
      }
    }

    "must return a SummaryList row for each land with failures" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val failures = Seq(
          (landA, Seq(authorityCodeFailure)),
          (landB, Seq(authorityCodeFailure))
        )

        val result = service.generateLandErrors(failures)

        result.rows.size mustBe 2
      }
    }

    "must use the land's address1 as the row key text" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val result = service.generateLandErrors(Seq((landA, Seq(authorityCodeFailure))))

        result.rows.head.key.content.asHtml.toString must include("Baker Street")
      }
    }

    "must fall back to landID as the row key text when address1 is None" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val landNoAddress = Land(landID = Some("LND001"), address1 = None)
        val result = service.generateLandErrors(Seq((landNoAddress, Seq(authorityCodeFailure))))

        result.rows.head.key.content.asHtml.toString must include("LND001")
      }
    }

    "must skip lands without a landID" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val landNoId = Land(landID = None, address1 = Some("Anonymous Lane"))
        val result = service.generateLandErrors(Seq((landNoId, Seq(authorityCodeFailure))))

        result.rows mustBe empty
      }
    }

    "must include a Change action that links to LandAuthorityCodeSingleEntityController" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val result = service.generateLandErrors(Seq((landA, Seq(authorityCodeFailure))))

        val actions = result.rows.head.actions.value.items
        actions.exists(_.href == controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("LND001").url) mustBe true
      }
    }

    "must include a Remove action that links to LandAuthorityCodeMultiEntityController" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)

        val result = service.generateLandErrors(Seq((landA, Seq(authorityCodeFailure))))

        val actions = result.rows.head.actions.value.items
        actions.exists(_.href == controllers.land.routes.LandAuthorityCodeMultiEntityController.removeLand("LND001").url) mustBe true
      }
    }
  }
}