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

import scala.util.{Success, Try}

class PopulateLandService {

  def populateLandInSession(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {

    land.landID match {
      case Some(landId) =>
        for {
          typeOfProperty <- typeOfPropertyPages(land, userAnswers)
          withInterestTransferredOrCreated <- interestTransferredOrCreatedPage(land, typeOfProperty)
          withAddress <- addressPage(land, withInterestTransferredOrCreated)
          withLocalAuthorityCode <- localAuthorityCodePage(land, withAddress)
          withTitleNumber <- titleNumberPages(land, withLocalAuthorityCode)
          withNlpgUprn <- nlpgUprnPages(land, withTitleNumber)
          withSendingPlanByPost <- sendingPlanByPostPage(land, withNlpgUprn)
          withMineralsOrMineralRights <- mineralsOrMineralRightsPage(land, withSendingPlanByPost)
          finalAnswers <- withMineralsOrMineralRights.set(LandOverviewPage, landId)
        } yield finalAnswers
      case _ =>
        Try(throw new IllegalStateException(s"Land ${land.landID} is missing a landID"))
    }
  }

  private def addressPage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    land.address1 match {
      case Some(address1) =>
        val address = Address(
          line1 = address1,
          line2 = land.address2,
          line3 = land.address3,
          line4 = land.address4,
          postcode = land.postcode
        )

        userAnswers.set(LandAddressPage, address)
      case None =>
        Success(userAnswers)
    }
  }

  private def localAuthorityCodePage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    land.localAuthorityNumber match {
      case Some(localAuthorityCode) =>
        userAnswers.set(LocalAuthorityCodePage, localAuthorityCode)
      case None =>
        Success(userAnswers)
    }
  }

  private def typeOfPropertyPages(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
      land.propertyType.flatMap(LandTypeOfProperty.fromCode) match {
        case Some(propertyType @ (LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential)) =>
          for {
            withPropertyType <- userAnswers.set(LandTypeOfPropertyPage, propertyType)
            finalAnswer      <- landAreaAndUnitPages(land, withPropertyType)
          } yield finalAnswer

        case Some(propertyType) =>
          userAnswers.set(LandTypeOfPropertyPage, propertyType)

        case None =>
          Success(userAnswers)
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
    val interestCreatedTransferredOpt = land.interestCreatedTransferred.flatMap(LandInterestTransferredOrCreated.fromCode)
    interestCreatedTransferredOpt match {
      case Some(interestCreatedTransferred) =>
        userAnswers.set(LandInterestTransferredOrCreatedPage, interestCreatedTransferred)
      case _ => userAnswers.remove(LandInterestTransferredOrCreatedPage)
    }
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
    val willSendPlanByPost = land.willSendPlanByPost.exists(_.equalsIgnoreCase("yes"))

    userAnswers.set(LandSendingPlanByPostPage, willSendPlanByPost)
  }

  private def mineralsOrMineralRightsPage(land: Land, userAnswers: UserAnswers): Try[UserAnswers] = {
    val mineralRights = land.mineralRights.exists(_.equalsIgnoreCase("yes"))

    userAnswers.set(LandMineralsOrMineralRightsPage, mineralRights)
  }
}