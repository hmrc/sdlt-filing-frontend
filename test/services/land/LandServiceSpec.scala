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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.Results
import models.{Land, NormalMode, ReturnInfo, UserAnswers}
import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.land.LandTypeOfProperty
import pages.land.LandTypeOfPropertyPage
import play.api.test.Helpers.*

import scala.concurrent.Future

class LandServiceSpec extends SpecBase with Matchers {

  private val service = new LandService()

  private val continueRoute = Results.Ok("continue")

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
}