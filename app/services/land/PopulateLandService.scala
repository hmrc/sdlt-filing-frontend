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

import models.address.Address
import models.land.{LandInterestTransferredOrCreated, LandSelectMeasurementUnit, LandTypeOfProperty}
import models.{Land, UserAnswers}
import pages.land.*

import scala.util.Try

class PopulateLandService {

  def populateLandInSession(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {

    (land.address1, land.localAuthorityNumber, land.landID) match
      case (Some(line1), Some(localAuthorityCode), Some(landId)) =>

        val address = Address(
          line1 = line1,
          line2 = land.address2,
          line3 = land.address3,
          line4 = land.address4,
          postcode = land.postcode
        )

        for {
          typeOfProperty <- typeOfPropertyPages(land, userAnswers)
          withInterestTransferredOrCreated <- interestTransferredOrCreatedPage(land, typeOfProperty)
          withAddress <- withInterestTransferredOrCreated.set(LandAddressPage, address)
          withLocalAuthorityCode <- withAddress.set(LocalAuthorityCodePage, localAuthorityCode)
          withTitleNumber <- titleNumberPages(land, withLocalAuthorityCode)
          withNlpgUprn <- nlpgUprnPages(land, withTitleNumber)
          withSendingPlanByPost <- sendingPlanByPostPage(land, withNlpgUprn)
          withMineralsOrMineralRights <- mineralsOrMineralRightsPage(land, withSendingPlanByPost)
          finalAnswers <- withMineralsOrMineralRights.set(LandOverviewPage, landId)
        } yield finalAnswers

      case _ =>
        Try(throw new IllegalStateException(s"Land ${land.landID} is missing required data"))
  }

  private def typeOfPropertyPages(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    Try {
      val propertyType: LandTypeOfProperty = land.propertyType match {
        case Some("01") => LandTypeOfProperty.Residential
        case Some("02") => LandTypeOfProperty.Mixed
        case Some("03") => LandTypeOfProperty.NonResidential
        case Some("04") => LandTypeOfProperty.Additional
        case _ =>
          throw new IllegalStateException(s"Land ${land.landID} is missing required property type")
      }
      propertyType
    }.flatMap {
      case propertyType@(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential) =>
        for {
          propertyType <- userAnswers.set(LandTypeOfPropertyPage, propertyType)
          finalAnswer <- landAreaAndUnitPages(land, propertyType)
        } yield finalAnswer
      case propertyType =>
        userAnswers.set(LandTypeOfPropertyPage, propertyType)
    }
  }

  private def landAreaAndUnitPages(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    (land.areaUnit, land.landArea) match {
      case (Some(areaUnit), Some(area)) =>
        val unit: LandSelectMeasurementUnit =
          if (areaUnit.equalsIgnoreCase(LandSelectMeasurementUnit.Sqms.toString))
            LandSelectMeasurementUnit.Sqms
          else
            LandSelectMeasurementUnit.Hectares

        for {
          agriculturalOrDevelopmentalLand <- userAnswers.set(AgriculturalOrDevelopmentalLandPage, true)
          doYouKnowTheAreaOfLand <- agriculturalOrDevelopmentalLand.set(DoYouKnowTheAreaOfLandPage, true)
          unitOfArea <- doYouKnowTheAreaOfLand.set(LandSelectMeasurementUnitPage, unit)
          finalAnswer <- unitOfArea.set(AreaOfLandPage, area)
        } yield finalAnswer

      case _ =>
        for {
          agriculturalOrDevelopmentalLand <- userAnswers.set(AgriculturalOrDevelopmentalLandPage, false)
          finalAnswer <- agriculturalOrDevelopmentalLand.set(DoYouKnowTheAreaOfLandPage, false)
        } yield finalAnswer
    }
  }

  private def interestTransferredOrCreatedPage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    val interestCreatedTransferred: LandInterestTransferredOrCreated = land.interestCreatedTransferred match {
      case Some("FG") => LandInterestTransferredOrCreated.FG
      case Some("FP") => LandInterestTransferredOrCreated.FP
      case Some("FT") => LandInterestTransferredOrCreated.FT
      case Some("LG") => LandInterestTransferredOrCreated.LG
      case Some("LP") => LandInterestTransferredOrCreated.LP
      case Some("LT") => LandInterestTransferredOrCreated.LT
      case Some("OT") => LandInterestTransferredOrCreated.OT
      case _ => throw new IllegalStateException(s"Land ${land.landID} is missing interestCreatedTransferred")
    }

    userAnswers.set(LandInterestTransferredOrCreatedPage, interestCreatedTransferred)
  }

  private def titleNumberPages(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    land.titleNumber match {
      case Some(title) =>
        for {
          isRegisteredWithHMLandRegistry <- userAnswers.set(LandRegisteredHmRegistryPage, true)
          finalAnswer <- isRegisteredWithHMLandRegistry.set(LandTitleNumberPage, title)
        } yield finalAnswer
      case None =>
        userAnswers.set(LandRegisteredHmRegistryPage, false)
    }
  }

  private def nlpgUprnPages(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    land.NLPGUPRN match {
      case Some(nlpgUprn) =>
        for {
          addNlpgUprn <- userAnswers.set(LandAddNlpgUprnPage, true)
          finalAnswer <- addNlpgUprn.set(LandNlpgUprnPage, nlpgUprn)
        } yield finalAnswer
      case None =>
        userAnswers.set(LandAddNlpgUprnPage, false)
    }
  }

  private def sendingPlanByPostPage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    val willSendPlanByPost = land.willSendPlanByPost.exists(_.equalsIgnoreCase("YES"))

    userAnswers.set(LandSendingPlanByPostPage, willSendPlanByPost)
  }

  private def mineralsOrMineralRightsPage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    val mineralRights = land.mineralRights.exists(_.equalsIgnoreCase("YES"))

    userAnswers.set(LandMineralsOrMineralRightsPage, mineralRights)
  }
}