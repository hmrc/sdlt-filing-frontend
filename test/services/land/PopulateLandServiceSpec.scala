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

import base.SpecBase
import models.address.Address
import models.land.{LandInterestTransferredOrCreated, LandSelectMeasurementUnit, LandTypeOfProperty}
import models.{Land, UserAnswers}
import org.scalatest.prop.TableDrivenPropertyChecks.*
import org.scalatestplus.mockito.MockitoSugar
import pages.land.*

import scala.util.{Failure, Success}

class PopulateLandServiceSpec extends SpecBase with MockitoSugar {

  val service = new PopulateLandService()

  "PopulateLandService" - {
    "when property type is Mixed" - {
      "when area of land is known" - {
        "must correctly populate the session" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("02"), // Mixed
            interestCreatedTransferred = Some("FG"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = Some("250.5"),
            areaUnit = Some("SQMETRE"),
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

          val result = service.populateLandInSession(land, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
          updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Mixed)
          updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
          updatedAnswers.get(LandAddressPage) mustBe Some(Address(
            line1 = "Baker Street",
            line2 = Some("Marylebone"),
            line3 = Some("London"),
            line4 = None,
            postcode = Some("NW1 6XE")
          ))
          updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
          updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
          updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
          updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
          updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
          updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
          updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

          updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe Some(true)
          updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe Some(true)
          updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe Some(LandSelectMeasurementUnit.Sqms)
          updatedAnswers.get(AreaOfLandPage) mustBe Some("250.5")
        }
      }

      "when area of land is not known" - {
        "must correctly populate the session" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("02"), // Mixed
            interestCreatedTransferred = Some("FG"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = None,
            areaUnit = None,
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

          val result = service.populateLandInSession(land, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
          updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Mixed)
          updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
          updatedAnswers.get(LandAddressPage) mustBe Some(Address(
            line1 = "Baker Street",
            line2 = Some("Marylebone"),
            line3 = Some("London"),
            line4 = None,
            postcode = Some("NW1 6XE")
          ))
          updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
          updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
          updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
          updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
          updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
          updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
          updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

          updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe Some(false)
          updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe Some(false)
        }
      }
    }

    "when property type is NonResidential" - {
      "when area of land is known" - {
        "must correctly populate the session" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("03"), // NonResidential
            interestCreatedTransferred = Some("FG"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = Some("250.5"),
            areaUnit = Some("HECTARES"),
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

          val result = service.populateLandInSession(land, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
          updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.NonResidential)
          updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
          updatedAnswers.get(LandAddressPage) mustBe Some(Address(
            line1 = "Baker Street",
            line2 = Some("Marylebone"),
            line3 = Some("London"),
            line4 = None,
            postcode = Some("NW1 6XE")
          ))
          updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
          updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
          updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
          updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
          updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
          updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
          updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

          updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe Some(true)
          updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe Some(true)
          updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe Some(LandSelectMeasurementUnit.Hectares)
          updatedAnswers.get(AreaOfLandPage) mustBe Some("250.5")
        }

      }

      "when area of land is not known" - {
        "must correctly populate the session" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("03"), // NonResidential
            interestCreatedTransferred = Some("FG"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = None,
            areaUnit = None,
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

          val result = service.populateLandInSession(land, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
          updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.NonResidential)
          updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
          updatedAnswers.get(LandAddressPage) mustBe Some(Address(
            line1 = "Baker Street",
            line2 = Some("Marylebone"),
            line3 = Some("London"),
            line4 = None,
            postcode = Some("NW1 6XE")
          ))
          updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
          updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
          updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
          updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
          updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
          updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
          updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

          updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe Some(false)
          updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe Some(false)
        }
      }
    }

    "when property type is Residential" - {
      "must correctly populate the session" in {
        val land = Land(
          landID = Some("LDN001"),
          returnID = Some("RET123456789"),
          propertyType = Some("01"), // Residential
          interestCreatedTransferred = Some("FG"),
          houseNumber = Some("123"),
          address1 = Some("Baker Street"),
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("NW1 6XE"),
          landArea = None,
          areaUnit = None,
          localAuthorityNumber = Some("5900"),
          mineralRights = Some("NO"),
          NLPGUPRN = Some("10012345678"),
          willSendPlanByPost = Some("NO"),
          titleNumber = Some("TGL12456"),
          landResourceRef = Some("LND-REF-001"),
          nextLandID = None,
          DARPostcode = Some("NW1 6XE")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateLandInSession(land, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
        updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Residential)
        updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
        updatedAnswers.get(LandAddressPage) mustBe Some(Address(
          line1 = "Baker Street",
          line2 = Some("Marylebone"),
          line3 = Some("London"),
          line4 = None,
          postcode = Some("NW1 6XE")
        ))
        updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
        updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
        updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
        updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
        updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
        updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
        updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

        updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe None
        updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe None
        updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe None
        updatedAnswers.get(AreaOfLandPage) mustBe None
      }
    }

    "when property type is Additional" - {
      "must correctly populate the session" in {
        val land = Land(
          landID = Some("LDN001"),
          returnID = Some("RET123456789"),
          propertyType = Some("04"), // Additional
          interestCreatedTransferred = Some("FG"),
          houseNumber = Some("123"),
          address1 = Some("Baker Street"),
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("NW1 6XE"),
          landArea = None,
          areaUnit = None,
          localAuthorityNumber = Some("5900"),
          mineralRights = Some("NO"),
          NLPGUPRN = Some("10012345678"),
          willSendPlanByPost = Some("NO"),
          titleNumber = Some("TGL12456"),
          landResourceRef = Some("LND-REF-001"),
          nextLandID = None,
          DARPostcode = Some("NW1 6XE")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateLandInSession(land, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
        updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Additional)
        updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
        updatedAnswers.get(LandAddressPage) mustBe Some(Address(
          line1 = "Baker Street",
          line2 = Some("Marylebone"),
          line3 = Some("London"),
          line4 = None,
          postcode = Some("NW1 6XE")
        ))
        updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
        updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(true)
        updatedAnswers.get(LandTitleNumberPage) mustBe Some("TGL12456")
        updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
        updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
        updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
        updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)

        updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe None
        updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe None
        updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe None
        updatedAnswers.get(AreaOfLandPage) mustBe None
      }
    }
    
    "when the land is not registered with HM Land Registry" - {
      "must correctly populate the session" in {
        val land = Land(
          landID = Some("LDN001"),
          returnID = Some("RET123456789"),
          propertyType = Some("04"), // Additional
          interestCreatedTransferred = Some("FG"),
          houseNumber = Some("123"),
          address1 = Some("Baker Street"),
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("NW1 6XE"),
          landArea = None,
          areaUnit = None,
          localAuthorityNumber = Some("5900"),
          mineralRights = Some("NO"),
          NLPGUPRN = Some("10012345678"),
          willSendPlanByPost = Some("NO"),
          titleNumber = None,
          landResourceRef = Some("LND-REF-001"),
          nextLandID = None,
          DARPostcode = Some("NW1 6XE")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateLandInSession(land, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
        updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Additional)
        updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
        updatedAnswers.get(LandAddressPage) mustBe Some(Address(
          line1 = "Baker Street",
          line2 = Some("Marylebone"),
          line3 = Some("London"),
          line4 = None,
          postcode = Some("NW1 6XE")
        ))
        updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
        updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(true)
        updatedAnswers.get(LandNlpgUprnPage) mustBe Some("10012345678")
        updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
        updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)
        updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe None
        updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe None
        updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe None
        updatedAnswers.get(AreaOfLandPage) mustBe None

        updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(false)
        updatedAnswers.get(LandTitleNumberPage) mustBe None
      }
    }

    "when the land has no NLPG UPRN" - {
      "must correctly populate the session" in {
        val land = Land(
          landID = Some("LDN001"),
          returnID = Some("RET123456789"),
          propertyType = Some("04"), // Additional
          interestCreatedTransferred = Some("FG"),
          houseNumber = Some("123"),
          address1 = Some("Baker Street"),
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("NW1 6XE"),
          landArea = None,
          areaUnit = None,
          localAuthorityNumber = Some("5900"),
          mineralRights = Some("NO"),
          NLPGUPRN = None,
          willSendPlanByPost = Some("NO"),
          titleNumber = None,
          landResourceRef = Some("LND-REF-001"),
          nextLandID = None,
          DARPostcode = Some("NW1 6XE")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateLandInSession(land, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(LandIdPage) mustBe Some("LDN001")
        updatedAnswers.get(LandTypeOfPropertyPage) mustBe Some(LandTypeOfProperty.Additional)
        updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(LandInterestTransferredOrCreated.FG)
        updatedAnswers.get(LandAddressPage) mustBe Some(Address(
          line1 = "Baker Street",
          line2 = Some("Marylebone"),
          line3 = Some("London"),
          line4 = None,
          postcode = Some("NW1 6XE")
        ))
        updatedAnswers.get(LocalAuthorityCodePage) mustBe Some("5900")
        updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(false)
        updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(false)
        updatedAnswers.get(AgriculturalOrDevelopmentalLandPage) mustBe None
        updatedAnswers.get(DoYouKnowTheAreaOfLandPage) mustBe None
        updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe None
        updatedAnswers.get(AreaOfLandPage) mustBe None
        updatedAnswers.get(LandRegisteredHmRegistryPage) mustBe Some(false)
        updatedAnswers.get(LandTitleNumberPage) mustBe None

        updatedAnswers.get(LandAddNlpgUprnPage) mustBe Some(false)
        updatedAnswers.get(LandNlpgUprnPage) mustBe None
      }
    }
    
    "must populate landInterestTransferredOrCreated correctly" - {
      val cases = Table(
        ("typeOfInterestTransferredOrCreated", "value"),
        ("FG", LandInterestTransferredOrCreated.FG),
        ("FP", LandInterestTransferredOrCreated.FP),
        ("FT", LandInterestTransferredOrCreated.FT),
        ("LG", LandInterestTransferredOrCreated.LG),
        ("LP", LandInterestTransferredOrCreated.LP),
        ("LT", LandInterestTransferredOrCreated.LT),
        ("OT", LandInterestTransferredOrCreated.OT)
      )
      
      forAll(cases) { (typeOfInterestTransferredOrCreated, value) =>
        
        s"when type is ${typeOfInterestTransferredOrCreated}" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("04"), // Additional
            interestCreatedTransferred = Some(typeOfInterestTransferredOrCreated),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = None,
            areaUnit = None,
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
  
          val result = service.populateLandInSession(land, userAnswers)
  
          result mustBe a[Success[_]]
  
          val updatedAnswers = result.get
  
          updatedAnswers.get(LandInterestTransferredOrCreatedPage) mustBe Some(value)
        }
      }
    }
    
    "must populate areaUnit correctly" - {
      val cases = Table(
        ("typeOfUnit", "value"),
        ("HECTARES", LandSelectMeasurementUnit.Hectares),
        ("SQMETRE", LandSelectMeasurementUnit.Sqms)
      )
      
      forAll(cases) { (typeOfUnit, value) =>
        
        s"when type is ${typeOfUnit}" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("02"), // Mixed
            interestCreatedTransferred = Some("FP"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = Some("123.2"),
            areaUnit = Some(typeOfUnit),
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
  
          val result = service.populateLandInSession(land, userAnswers)
  
          result mustBe a[Success[_]]
  
          val updatedAnswers = result.get

          updatedAnswers.get(LandSelectMeasurementUnitPage) mustBe Some(value)
        }
      }
    }
    
    "must populate landSendingPlanByPost correctly" - {
      val cases = Table(
        ("answer", "value"),
        ("YES", true),
        ("NO", false)
      )
      
      forAll(cases) { (answer, value) =>
        
        s"when the answer is ${answer}" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("02"), // Mixed
            interestCreatedTransferred = Some("FP"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = None,
            areaUnit = None,
            localAuthorityNumber = Some("5900"),
            mineralRights = Some("NO"),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some(answer),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
  
          val result = service.populateLandInSession(land, userAnswers)
  
          result mustBe a[Success[_]]
  
          val updatedAnswers = result.get

          updatedAnswers.get(LandSendingPlanByPostPage) mustBe Some(value)
        }
      }
    }
    
    "must populate landMineralsOrMineralRights correctly" - {
      val cases = Table(
        ("answer", "value"),
        ("YES", true),
        ("NO", false)
      )
      
      forAll(cases) { (answer, value) =>
        
        s"when the answer is ${answer}" in {
          val land = Land(
            landID = Some("LDN001"),
            returnID = Some("RET123456789"),
            propertyType = Some("02"), // Mixed
            interestCreatedTransferred = Some("FP"),
            houseNumber = Some("123"),
            address1 = Some("Baker Street"),
            address2 = Some("Marylebone"),
            address3 = Some("London"),
            address4 = None,
            postcode = Some("NW1 6XE"),
            landArea = None,
            areaUnit = None,
            localAuthorityNumber = Some("5900"),
            mineralRights = Some(answer),
            NLPGUPRN = Some("10012345678"),
            willSendPlanByPost = Some("NO"),
            titleNumber = Some("TGL12456"),
            landResourceRef = Some("LND-REF-001"),
            nextLandID = None,
            DARPostcode = Some("NW1 6XE")
          )

          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
  
          val result = service.populateLandInSession(land, userAnswers)
  
          result mustBe a[Success[_]]
  
          val updatedAnswers = result.get

          updatedAnswers.get(LandMineralsOrMineralRightsPage) mustBe Some(value)
        }
      }
    }
    
    "must fail when address1 is missing" in {
      val land = Land(
        landID = Some("LDN001"),
        returnID = Some("RET123456789"),
        propertyType = Some("04"), // Additional
        interestCreatedTransferred = Some("FG"),
        houseNumber = Some("123"),
        address1 = None,
        address2 = Some("Marylebone"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("NW1 6XE"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = Some("5900"),
        mineralRights = Some("NO"),
        NLPGUPRN = Some("10012345678"),
        willSendPlanByPost = Some("NO"),
        titleNumber = Some("TGL12456"),
        landResourceRef = Some("LND-REF-001"),
        nextLandID = None,
        DARPostcode = Some("NW1 6XE")
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

      val result = service.populateLandInSession(land, userAnswers)
      
      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }
    
    "must fail when property type is missing" in {
      val land = Land(
        landID = Some("LDN001"),
        returnID = Some("RET123456789"),
        propertyType = None,
        interestCreatedTransferred = Some("FG"),
        houseNumber = Some("123"),
        address1 = Some("Baker Street"),
        address2 = Some("Marylebone"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("NW1 6XE"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = Some("5900"),
        mineralRights = Some("NO"),
        NLPGUPRN = Some("10012345678"),
        willSendPlanByPost = Some("NO"),
        titleNumber = Some("TGL12456"),
        landResourceRef = Some("LND-REF-001"),
        nextLandID = None,
        DARPostcode = Some("NW1 6XE")
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

      val result = service.populateLandInSession(land, userAnswers)
      
      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }

    "must fail when localAuthorityNumber is missing" in {
      val land = Land(
        landID = Some("LDN001"),
        returnID = Some("RET123456789"),
        propertyType = Some("04"), // Additional
        interestCreatedTransferred = Some("FG"),
        houseNumber = Some("123"),
        address1 = Some("Baker Street"),
        address2 = Some("Marylebone"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("NW1 6XE"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = None,
        mineralRights = Some("NO"),
        NLPGUPRN = Some("10012345678"),
        willSendPlanByPost = Some("NO"),
        titleNumber = Some("TGL12456"),
        landResourceRef = Some("LND-REF-001"),
        nextLandID = None,
        DARPostcode = Some("NW1 6XE")
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

      val result = service.populateLandInSession(land, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }

    "must fail when landID is missing" in {
      val land = Land(
        landID = None,
        returnID = Some("RET123456789"),
        propertyType = Some("04"), // Additional
        interestCreatedTransferred = Some("FG"),
        houseNumber = Some("123"),
        address1 = Some("Baker Street"),
        address2 = Some("Marylebone"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("NW1 6XE"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = Some("5900"),
        mineralRights = Some("NO"),
        NLPGUPRN = Some("10012345678"),
        willSendPlanByPost = Some("NO"),
        titleNumber = Some("TGL12456"),
        landResourceRef = Some("LND-REF-001"),
        nextLandID = None,
        DARPostcode = Some("NW1 6XE")
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

      val result = service.populateLandInSession(land, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }

    "must fail when interestTransferredOrCreated is missing" in {
      val land = Land(
        landID = Some("LDN001"),
        returnID = Some("RET123456789"),
        propertyType = Some("04"), // Additional
        interestCreatedTransferred = None,
        houseNumber = Some("123"),
        address1 = Some("Baker Street"),
        address2 = Some("Marylebone"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("NW1 6XE"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = Some("5900"),
        mineralRights = Some("NO"),
        NLPGUPRN = Some("10012345678"),
        willSendPlanByPost = Some("NO"),
        titleNumber = Some("TGL12456"),
        landResourceRef = Some("LND-REF-001"),
        nextLandID = None,
        DARPostcode = Some("NW1 6XE")
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

      val result = service.populateLandInSession(land, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }
  }
}

