/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package navigation.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import models.scalabuild.CurrentValue.writes
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.PropertyType.{NonResidential, Residential}
import models.scalabuild.{HoldingTypes, LeaseDates, RentPeriods, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.scalabuild._
import play.api.libs.json.{JsPath, JsValue}
import services.scalabuild.FtbLimitService

import java.time.LocalDate

class NavigatorSpec extends AnyFreeSpec with ScalaSpecBase with TestObjects{

  val ftbLimitService = new FtbLimitService(appConfig)
  val navigator = new Navigator(ftbLimitService)

  def baseUA: UserAnswers = UserAnswers("id")

  "Navigator" - {
    "in Normal mode" - {
      "should redirect from HoldingPage to ResidentialOrNonResidentialPage" in {
        navigator.nextPage(
          HoldingPage,
          emptyUserAnswers
        ) mustBe controllers.scalabuild.routes.ResidentialOrNonResidentialController.onPageLoad()
      }

      "should redirect from ResidentialOrNonResidentialPage to EffectiveDatePage" in {
        navigator.nextPage(
          ResidentialOrNonResidentialPage,
          emptyUserAnswers
        ) mustBe controllers.scalabuild.routes.EffectiveDateController.onPageLoad()
      }

      "should redirect from EffectiveDatePage to IsPurchaserIndividualPage Based on freehold and Residential type Property" in {
        navigator.nextPage(
          EffectiveDatePage,
          uaFreeResBefore2016
        ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
      }

      "should redirect from EffectiveDatePage to NonUKResident for residential after April 2021" in {
        val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2023,1,1)).success.value

        val result = navigator.nextPage(EffectiveDatePage, ua)

        result mustBe controllers.scalabuild.routes.NonUkResidentController.onPageLoad()
      }

      "should redirect from LeaseDatesPage to rent when shared ownership and current value true" in {

        val ua = baseUA
          .set(SharedOwnershipPage, true).success.value
          .set(CurrentValuePage, true).success.value
          .set(PremiumPage, BigDecimal(10000)).success.value

        val result = navigator.nextPage(LeaseDatesPage, ua)

        result shouldBe controllers.scalabuild.routes.RentController.onPageLoad()
      }

      "should redirect from LeaseDatesPage to PremiumController Based on Leasehold and Residential type Property" in {
        val ua = baseUA
          .set(HoldingPage, HoldingTypes.Leasehold).success.value
          .set(ResidentialOrNonResidentialPage, Residential).success.value
          .set(EffectiveDatePage, LocalDate.of(2020,4,1)).success.value
          .set(NonUkResidentPage, true).success.value
        navigator.nextPage(
          LeaseDatesPage,
          ua,
        ) mustBe controllers.scalabuild.routes.PremiumController.onPageLoad()
      }

      "should redirect from RentPage to exchange contracts for qualifying non residential rent" in {

        val rents = RentPeriods(
          year1Rent = BigDecimal(1000.00),
          year2Rent = Some(BigDecimal(1500.50)))

        val ua = baseUA
          .set(ResidentialOrNonResidentialPage, NonResidential).success.value
          .set(PremiumPage, BigDecimal(100000)).success.value
          .set(RentPage, rents).success.value
          .set(EffectiveDatePage, LocalDate.of(2017,1,1)).success.value

        val result = navigator.nextPage(RentPage, ua)

        result shouldBe controllers.scalabuild.routes.ExchangeContractsPreAndPostController.onPageLoad()
      }

      "should redirect from ExchangeContractsPage to relevant rent when contract pre 2016 and not changed" in {

        val ua = baseUA
          .set(ExchangeContractsPage, true).success.value
          .set(ContractPost201603Page, false).success.value

        val result = navigator.nextPage(ExchangeContractsPage, ua)

        result shouldBe controllers.scalabuild.routes.RelevantRentController.onPageLoad()
      }

      "should redirect from Navigator to journey recovery when answers missing" in {
        val result = navigator.nextPage(CurrentValuePage, emptyUserAnswers)

        result shouldBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
      }



      "Based on Dates" - {
        "Should redirect to StartAgainView Page when holdingType is missing and effectiveDate is 31/03/2016" in {

          case object TestPage extends QuestionPage[JsValue] {
            override def path: JsPath = JsPath \ "test"
          }
          navigator.nextPage(
            TestPage,
            emptyUserAnswers2
          ) mustBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }

        "should redirect to ResidentialOrNonResidentialController when propertyType is Residential and effective date is 01/04/2021" in {
          val ua = baseUA
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2021,4,1)).success.value
          navigator.nextPage(
            HoldingPage,
            ua
          ) mustBe controllers.scalabuild.routes.ResidentialOrNonResidentialController.onPageLoad()
        }

        "should redirect to IsPurchaserIndividualController when propertyType is Residential and holdingType is Leasehold and effective date is 01/04/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,4,1)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.IsPurchaserIndividualController.onPageLoad()
        }

        "should redirect to IsPurchaserIndividualController when propertyType is Residential and holdingType is Freehold and effective date is 01/04/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,4,1)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.IsPurchaserIndividualController.onPageLoad()
        }

        "should redirect to LeaseDatesController when propertyType is Residential and holdingType is Leasehold and effective date is 31/03/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,3,31)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to PurchasePriceController when propertyType is Residential and holdingType is Freehold and effective date is 31/03/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,3,31)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }
        "should redirect to LeaseDatesController when propertyType is \"Non-residential\" and holdingType is \"Leasehold\" and effective date is 01/04/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,4,1)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to PurchasePriceController when propertyType is \"Non-residential\" and holdingType is \"Freehold\" and effective date is 01/04/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,4,1)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect to /lease-dates when propertyType is \"Non-residential\" and holdingType is \"Leasehold\" and effective date is 31/03/2016" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,3,31)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to /purchase-price when propertyType is \"Non-residential\" and holdingType is \"Freehold\" and effective date is 31/03/2016*/" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,3,31)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect to StartAgain \"JourneyRecoveryController\"  when EffectiveDatePage is 31/03/2016*/" in {
          val ua = baseUA
            .set(EffectiveDatePage, LocalDate.of(2016,3,31)).success.value
          navigator.nextPage(
            EffectiveDatePage,
            ua
          ) mustBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }

      }

      "OwnsOtherProperties" -{
        "should redirect to /main-residence when when holdingType is Freehold and ownedOtherProperties is \"No\"" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(EffectiveDatePage, LocalDate.of(2017,11,22)).success.value
            .set(IsPurchaserIndividualPage, true).success.value
            .set(IsAdditionalPropertyPage, true).success.value
            .set(OwnsOtherPropertiesPage, false).success.value
          navigator.nextPage(
            OwnsOtherPropertiesPage,
            ua
          ) mustBe controllers.scalabuild.routes.MainResidenceController.onPageLoad()
        }

        "should redirect to /purchase-price when holdingType is Freehold and ownedOtherProperties is \"yes\"" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(OwnsOtherPropertiesPage, true).success.value
          navigator.nextPage(
            OwnsOtherPropertiesPage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect to /purchase-price when holdingType is Leasehold and ownedOtherProperties is \"yes\"" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(OwnsOtherPropertiesPage, true).success.value
          navigator.nextPage(
            OwnsOtherPropertiesPage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to JourneyRecoveryController when ownedOtherProperties is \"yes\" and other details are missing" in {
          val ua = baseUA
            .set(OwnsOtherPropertiesPage, true).success.value
          navigator.nextPage(
            OwnsOtherPropertiesPage,
            ua
          ) mustBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "Main Residence " - {
        "should redirect to LeaseDatesController when holdingType is Leasehold and mainResidence is false" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(MainResidencePage, false).success.value
          navigator.nextPage(
            MainResidencePage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to PurchasePriceController when holdingType is Freehold and mainResidence is true" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(MainResidencePage, true).success.value
          navigator.nextPage(
            MainResidencePage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect to JourneyRecoveryController when mainResidence is true" in {
          val ua = baseUA
            .set(MainResidencePage, true).success.value
          navigator.nextPage(
            MainResidencePage,
            ua
          ) mustBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }

        "should redirect to SharedOwnershipController when holdingType is Leasehold and mainResidence is true" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(MainResidencePage, true).success.value
          navigator.nextPage(
            MainResidencePage,
            ua
          ) mustBe controllers.scalabuild.routes.SharedOwnershipController.onPageLoad()
        }
      }

      "Shared Ownership" - {

        "should redirect to CurrentValueController when holdingType is Leasehold and mainResidence is true" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(SharedOwnershipPage, true).success.value
          navigator.nextPage(
            SharedOwnershipPage,
            ua
          ) mustBe controllers.scalabuild.routes.CurrentValueController.onPageLoad()
        }

        "should redirect to LeaseDatesController when holdingType is Leasehold and mainResidence is false" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(SharedOwnershipPage, false).success.value
          navigator.nextPage(
            SharedOwnershipPage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to PurchasePriceController when holdingType is Freehold and mainResidence is false" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(SharedOwnershipPage, false).success.value
          navigator.nextPage(
            SharedOwnershipPage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect to JourneyRecoveryController when sharedOwnership is true" in {
          val ua = baseUA
            .set(SharedOwnershipPage, false).success.value
          navigator.nextPage(
            SharedOwnershipPage,
            ua
          ) mustBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }
      }


      "LeaseDates" - {
        "should redirect to /lease-dates when holdingType is Leasehold and currentValue is false" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(CurrentValuePage, false).success.value
          navigator.nextPage(
            CurrentValuePage,
            ua
          ) mustBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect to /purchase-price when holdingType is Freehold and currentValue is false" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Freehold).success.value
            .set(CurrentValuePage, false).success.value
          navigator.nextPage(
            CurrentValuePage,
            ua
          ) mustBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }
      }

      "MarketValue" - {
        "should redirect to /market-value when currentValue is true" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(CurrentValuePage, true).success.value
          navigator.nextPage(
            CurrentValuePage,
            ua
          ) mustBe controllers.scalabuild.routes.MarketValueController.onPageLoad()
        }
      }

      "Relevant rent " - {
        "should redirect to /relevant-rent page when propertyType is \"Non-residential\" and premium < \"150000\" and all rents < \"2000\"" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,2,16)).success.value
            .set(LeaseDatesPage, LeaseDates(LocalDate.of(2016,2,16), LocalDate.of(2116,2,15))).success.value
            .set(PremiumPage, BigDecimal(149000)).success.value
            .set(RentPage, RentPeriods(year1Rent = 1001,
              year2Rent = Some(1001),
              year3Rent = Some(1001),
              year4Rent = Some(1001),
              year5Rent = Some(1001))).success.value
          navigator.nextPage(
            RentPage,
            ua
          ) mustBe controllers.scalabuild.routes.RelevantRentController.onPageLoad()
        }

        "should redirect to CYA Page when propertyType is \"Non-residential\" and premium < \"150000\" and rent > 2000" in {
          val ua = baseUA
            .set(HoldingPage, HoldingTypes.Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, NonResidential).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,2,16)).success.value
            .set(LeaseDatesPage, LeaseDates(LocalDate.of(2016,2,16), LocalDate.of(2116,2,15))).success.value
            .set(PremiumPage, BigDecimal(149000)).success.value
            .set(RentPage, RentPeriods(year1Rent = 10010,
              year2Rent = Some(1001),
              year3Rent = Some(1001),
              year4Rent = Some(1001),
              year5Rent = Some(1001))).success.value
          navigator.nextPage(
            RentPage,
            ua
          ) mustBe controllers.scalabuild.routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "Additional Properties" - {
        "should redirect from IsAdditionalPropertyPage to /owned-other-properties when conditions match FTB rule" in {
          val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(IsPurchaserIndividualPage, true).success.value
            .set(IsAdditionalPropertyPage, false).success.value
            .set(EffectiveDatePage, LocalDate.of(2019,1,1)).success.value

          val result = navigator.nextPage(IsAdditionalPropertyPage, ua)

          result shouldBe controllers.scalabuild.routes.OwnsOtherPropertiesController.onPageLoad()
        }

        "should redirect from IsAdditionalPropertyPage to /owned-other-properties when conditions match FTB rule and effective date is after 30/06/2021*/" in {
          val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(IsPurchaserIndividualPage, true).success.value
            .set(IsAdditionalPropertyPage, false).success.value
            .set(EffectiveDatePage, LocalDate.of(2021,7,1)).success.value

          val result = navigator.nextPage(IsAdditionalPropertyPage, ua)

          result shouldBe controllers.scalabuild.routes.OwnsOtherPropertiesController.onPageLoad()
        }

        "should redirect from IsAdditionalPropertyPage to LeaseDatesController when HoldingType is LeaseHold and effective date is before 30/06/2016*/" in {
          val ua = baseUA
            .set(HoldingPage, Leasehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(IsPurchaserIndividualPage, true).success.value
            .set(IsAdditionalPropertyPage, false).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,7,1)).success.value

          val result = navigator.nextPage(IsAdditionalPropertyPage, ua)

          result shouldBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect from IsAdditionalPropertyPage to PurchasePriceController when HoldingType is Freehold and effective date is before 30/06/2016*/" in {
          val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(ResidentialOrNonResidentialPage, Residential).success.value
            .set(IsPurchaserIndividualPage, true).success.value
            .set(IsAdditionalPropertyPage, false).success.value
            .set(EffectiveDatePage, LocalDate.of(2016,7,1)).success.value

          val result = navigator.nextPage(IsAdditionalPropertyPage, ua)

          result shouldBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }
      }

      "Purchaser Individual "- {
        "should redirect from IsPurchaserIndividualPage to additional property if individual is purchasing true" in {
          val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(IsPurchaserIndividualPage, true).success.value
          val result = navigator.nextPage(IsPurchaserIndividualPage, ua)
          result shouldBe controllers.scalabuild.routes.AdditionalPropAndReplaceController.onPageLoad()
        }

        "should redirect from IsPurchaserIndividualPage to PurchasePriceController if individual is not purchasing  false" in {
          val ua = baseUA
            .set(HoldingPage, Freehold).success.value
            .set(IsPurchaserIndividualPage, false).success.value
          val result = navigator.nextPage(IsPurchaserIndividualPage, ua)
          result shouldBe controllers.scalabuild.routes.PurchasePriceController.onPageLoad()
        }

        "should redirect from IsPurchaserIndividualPage to LeaseDatesController when holdingType is LeaseHold and  individual is not purchasing  false" in {
          val ua = baseUA
            .set(HoldingPage, Leasehold).success.value
            .set(IsPurchaserIndividualPage, false).success.value
          val result = navigator.nextPage(IsPurchaserIndividualPage, ua)
          result shouldBe controllers.scalabuild.routes.LeaseDatesController.onPageLoad()
        }

        "should redirect from IsPurchaserIndividualPage to JourneyRecoveryController when details are missing" in {
          val result = navigator.nextPage(IsPurchaserIndividualPage, baseUA)
          result shouldBe controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }
  }

}
