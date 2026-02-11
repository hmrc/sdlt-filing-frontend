/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package navigation.scalabuild
import com.google.inject.{Inject, Singleton}
import controllers.scalabuild.routes
import data.Dates._
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.PropertyType.{NonResidential, Residential}
import models.scalabuild.{HoldingTypes, RentPeriods, UserAnswers}
import pages.scalabuild._
import play.api.mvc.Call
import services.scalabuild.FtbLimitService
import utils.CalculationUtils.DateHelper

@Singleton
class Navigator @Inject() (
  service: FtbLimitService,
){

  private val normalRoutes: Page => UserAnswers => Call = {
    case HoldingPage                     => _ => routes.ResidentialOrNonResidentialController.onPageLoad()
    case ResidentialOrNonResidentialPage => _ => routes.EffectiveDateController.onPageLoad()
    case EffectiveDatePage               => userAnswers => effectiveDatesNormalMode(userAnswers)
    case NonUkResidentPage               => _ => routes.IsPurchaserIndividualController.onPageLoad()
    case IsPurchaserIndividualPage       => userAnswers => isPurchaserIndividualPageNormalMode(userAnswers)
    case IsAdditionalPropertyPage        => userAnswers => additionalPropertyNormalMode(userAnswers)
    case PurchasePricePage               => _ => routes.CheckYourAnswersController.onPageLoad()
    case ReplaceMainResidencePage        => userAnswers => replaceMainResidenceNormalMode(userAnswers)
    case OwnsOtherPropertiesPage         => userAnswers => ownsOtherPropertiesNormalMode(userAnswers)
    case MainResidencePage               => userAnswers => mainResidenceNormalMode(userAnswers)
    case CurrentValuePage                => userAnswers => currentValueNormalMode(userAnswers)
    case MarketValuePage                 => _ => routes.LeaseDatesController.onPageLoad()
    case LeaseDatesPage                  => userAnswers => leaseDatesNormalMode(userAnswers)
    case RentPage                        => userAnswers => rentNormalMode(userAnswers)
    case ExchangeContractsPage           => userAnswers => exchangeContractsNormalMode(userAnswers)
    case PremiumPage                     => _ => routes.RentController.onPageLoad()
    case RelevantRentPage                => _ => routes.CheckYourAnswersController.onPageLoad()
    case _                               => _ => routes.JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, userAnswers: UserAnswers): Call =
//    mode match {
//      case NormalMode =>
    normalRoutes(page)(userAnswers)
//      case CheckMode => ???
//        checkRouteMap(page) (userAnswers)
//    }

  private def exchangeContractsNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val contractPreMar2016 = userAnswers.get(ExchangeContractsPage)
    val changedPostMar2016 = userAnswers.get(ContractPost201603Page)

    (contractPreMar2016, changedPostMar2016) match {
      case (Some(true), Some(false)) => routes.RelevantRentController.onPageLoad()
      case _ => routes.CheckYourAnswersController.onPageLoad()
    }
  }
  private def rentNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      propertyType <- userAnswers.get(ResidentialOrNonResidentialPage)
      premium <- userAnswers.get(PremiumPage)
      premiumBelow150000 = premium < 150000
      rents <- userAnswers.get(RentPage)
      allRentsUnder2000 = allRentsBelow2000(rents)
      effectiveDate <- userAnswers.get(EffectiveDatePage)
    } yield (propertyType, premiumBelow150000, allRentsUnder2000, effectiveDate)

    answers match {
      case Some(Tuple4(NonResidential, true, true, date)) if date.isAfter(MARCH2016_NON_RESIDENTIAL_DATE) => routes.ExchangeContractsPreAndPostController.onPageLoad()
      case Some(Tuple4(NonResidential, true, true, date)) => routes.RelevantRentController.onPageLoad()
      case _ =>
        routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def allRentsBelow2000(rents: RentPeriods) = {
    if (rents.rents.max < 2000) true else false
  }

  private def currentValueNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      currentValue <- userAnswers.get(CurrentValuePage)
    } yield (holding, currentValue)

    answers match {
      case Some(Tuple2(Freehold, false)) => routes.PurchasePriceController.onPageLoad()
      case Some(Tuple2(Leasehold, false)) => routes.LeaseDatesController.onPageLoad()
      case Some(Tuple2(_, true)) => routes.MarketValueController.onPageLoad()
      case None        => ???
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def leaseDatesNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      sharedOwnership <- userAnswers.get(SharedOwnershipPage)
      currentValue <- userAnswers.get(CurrentValuePage)
    } yield (sharedOwnership, currentValue)
    val oPremium = userAnswers.get(PremiumPage)

    (answers, oPremium) match {
      case (Some(Tuple2(true, true)), Some(_)) => routes.RentController.onPageLoad()
      case _ =>
        routes.PremiumController.onPageLoad()
    }
  }

  private def ownsOtherPropertiesNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      ownsOtherProperties <- userAnswers.get(OwnsOtherPropertiesPage)
    } yield (holding, ownsOtherProperties)

    answers match {
      case Some(Tuple2(_, false)) =>
        routes.MainResidenceController.onPageLoad()
      case Some(Tuple2(Leasehold, true)) =>
        routes.LeaseDatesController.onPageLoad()
      case Some(Tuple2(Freehold, true)) =>
        routes.PurchasePriceController.onPageLoad()
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def mainResidenceNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      mainResidence <- userAnswers.get(MainResidencePage)
    } yield (holding, mainResidence)

    answers match {
      case Some(Tuple2(Leasehold, false)) =>
        routes.LeaseDatesController.onPageLoad()
      case Some(Tuple2(Freehold, false)) =>
        routes.PurchasePriceController.onPageLoad()
      case Some(Tuple2(_, true)) =>
        routes.CurrentValueController.onPageLoad()
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def replaceMainResidenceNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
    } yield holding

    answers match {
      case Some(Leasehold) =>
        routes.LeaseDatesController.onPageLoad()
      case Some(Freehold) =>
        routes.PurchasePriceController.onPageLoad()
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def effectiveDatesNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      residential <- userAnswers.get(ResidentialOrNonResidentialPage)
      effectiveDate <- userAnswers.get(EffectiveDatePage)
    } yield (holding, residential, effectiveDate)

    answers match {
      case Some(Tuple3(holding, Residential, date)) if (date.isBefore(APRIL2016_RESIDENTIAL_DATE)) =>
        redirectBasedOnHolding(holding)
      case Some(Tuple3(_, Residential, date)) if date.onOrAfter(APR2021_RESIDENTIAL_DATE)  =>
        routes.NonUkResidentController.onPageLoad()
      case Some(Tuple3(_, Residential, _))  =>
        routes.IsPurchaserIndividualController.onPageLoad()
      case Some(Tuple3(holding, _, _)) =>
        redirectBasedOnHolding(holding)
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def redirectBasedOnHolding(holding: HoldingTypes): Call = {
    holding match {
      case HoldingTypes.Freehold  => routes.PurchasePriceController.onPageLoad()
      case HoldingTypes.Leasehold => routes.LeaseDatesController.onPageLoad()
    }
  }

  private def additionalPropertyNormalMode(
      userAnswers: UserAnswers
  ):Call = {
    def replaceMainResidenceOption(isAdditionalProperty: Option[Boolean]):Option[Boolean] = {
      isAdditionalProperty match {
        case Some(value) if value => (userAnswers.get(ReplaceMainResidencePage))
        case Some(_)     => None
        case _        => None
      }
    }
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      residential <- userAnswers.get(ResidentialOrNonResidentialPage)
      isIndividual <- userAnswers.get(IsPurchaserIndividualPage)
      isAdditionalProperty <- userAnswers.get(IsAdditionalPropertyPage)
      effectiveDate <- userAnswers.get(EffectiveDatePage)
    } yield (holding, residential, isIndividual, isAdditionalProperty, effectiveDate)

    (answers, replaceMainResidenceOption(answers.map(_._4))) match {
      case (Some(Tuple5(_, Residential, true, false,  date)),_)
        if (date.isAfter(NOV2017_RESIDENTIAL_DATE) && date.isBefore(JULY2020_RESIDENTIAL_DATE) | date.isAfter(JUNE2021_RESIDENTIAL_DATE)) =>
        routes.OwnsOtherPropertiesController.onPageLoad()
      case (Some(Tuple5(Freehold, _, _, _,  _)),_) =>
        routes.PurchasePriceController.onPageLoad()
      case (Some(Tuple5(Leasehold, _, _, _,  _)),_ )=>
        routes.PurchasePriceController.onPageLoad()
      case _ => routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def isPurchaserIndividualPageNormalMode(
      userAnswers: UserAnswers
  ): Call = {
    val answers = for {
      holding <- userAnswers.get(HoldingPage)
      isIndividual <- userAnswers.get(IsPurchaserIndividualPage)
    } yield (holding, isIndividual)
    answers match {
      case Some(Tuple2(_, true)) =>
        routes.AdditionalPropAndReplaceController.onPageLoad()
      case Some(Tuple2(Freehold, false)) =>
        routes.PurchasePriceController.onPageLoad()
      case Some(Tuple2(Leasehold, false)) =>
        routes.LeaseDatesController.onPageLoad()
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  }

}
