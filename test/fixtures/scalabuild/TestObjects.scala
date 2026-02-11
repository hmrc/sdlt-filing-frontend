/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package fixtures.scalabuild
import models.SliceDetails
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.PropertyType.Residential
import models.scalabuild.{DetailTableRow, LeaseDates, RentPeriods, ResultDisplayTable, UserAnswers}
import pages.scalabuild._
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import utils.scalabuild.DateTimeFormats.localDateTimeFormatter
import viewmodels.scalabuild.DetailSummary
import viewmodels.scalabuild.FormatUtils.{bigDecimalFormat, keyCssClass, valueCssClass}
import viewmodels.scalabuild.checkanswerssummary._
import viewmodels.scalabuild.govuk.summarylist.{FluentKey, FluentValue, SummaryListViewModel}

import java.time.{Instant, LocalDate}
import scala.util.{Failure, Success, Try}

trait TestObjects {
  val date = LocalDate.of(2022, 2, 11)
  val price = 550000

  private val clock = Instant.parse("2025-09-02T14:00:00Z")

  val emptyUserAnswers2: UserAnswers = UserAnswers(
    id = "user-123",
    data = JsObject.empty,
    lastUpdated = clock
  )
  val uaFreeRes: UserAnswers = UserAnswers(
    id = "user-123",
    data = Json.parse(
      """
        |{
        |"holdingType":"Freehold",
        |"propertyType":"Residential"
        |}
        |""".stripMargin).as[JsObject],
    lastUpdated = clock
  )

  val freeResNonIndAddMainJourney: Try[UserAnswers] = for {
    holding <- emptyUserAnswers2.set(HoldingPage, Freehold)
    propertyType <- holding.set(ResidentialOrNonResidentialPage, Residential)
    effectiveDate <- propertyType.set(EffectiveDatePage, LocalDate.of(2025, 1, 1))
    nonUKResident <- effectiveDate.set(NonUkResidentPage, true)
    additionalProperty <- nonUKResident.set(IsAdditionalPropertyPage, true)
    replaceMainResidence <- additionalProperty.set(ReplaceMainResidencePage, true)
    purchasePrice <- replaceMainResidence.set(PremiumPage, BigDecimal(500000))
    individual <- purchasePrice.set(IsPurchaserIndividualPage, true)
    allUpdatedAnswers <- individual.set(ReplaceMainResidencePage, true)
  } yield allUpdatedAnswers

  val freeResNonIndAddMainUaData: JsObject = Json.parse(
    """{
      |    "holdingType": "Freehold",
      |    "propertyType": "Residential",
      |    "effectiveDate": "2025-01-01",
      |    "nonUKResident": true,
      |    "propertyDetails": {
      |      "individual": true,
      |      "twoOrMoreProperties": true,
      |      "ownedOtherProperties": false,
      |      "replaceMainResidence": true
      |    },
      |    "premium": 500000
      |}""".stripMargin).as[JsObject]

  val leaseResNonIndAddMainJourney: Try[UserAnswers] = for {
    holding <- emptyUserAnswers2.set(HoldingPage, Leasehold)
    propertyType <- holding.set(ResidentialOrNonResidentialPage, Residential)
    effectiveDate <- propertyType.set(EffectiveDatePage, LocalDate.of(2025, 1, 1))
    nonUKResident <- effectiveDate.set(NonUkResidentPage, true)
    additionalProperty <- nonUKResident.set(IsAdditionalPropertyPage, true)
    replaceMainResidence <- additionalProperty.set(ReplaceMainResidencePage, true)
    premium <- replaceMainResidence.set(PremiumPage, BigDecimal(500000))
    leaseDates <- premium.set[LeaseDates](LeaseDatesPage, LeaseDates(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2095, 1, 1)))
    individual <- leaseDates.set(IsPurchaserIndividualPage, true)
    rents <- individual.set[RentPeriods](RentPage, RentPeriods(year1Rent = 200, year2Rent = Some(200), year3Rent = Some(200), year4Rent = Some(200), year5Rent = Some(200)))
    allUpdatedAnswers <- rents.set(ReplaceMainResidencePage, true)
  } yield allUpdatedAnswers

  val leaseResNonIndAddMainUaData: JsObject = Json
    .parse("""{
      |    "holdingType": "Leasehold",
      |    "propertyType": "Residential",
      |    "effectiveDate": "2025-01-01",
      |    "nonUKResident": false,
      |    "propertyDetails": {
      |      "individual": true,
      |      "twoOrMoreProperties": false,
      |      "ownedOtherProperties": false,
      |      "mainResidence": false
      |    },
      |    "mongoLeaseDetails": {
      |      "leaseDates": {
      |        "startDate": "2025-01-01",
      |        "endDate": "2200-01-01"
      |      },
      |      "rentDetails": {
      |        "year1Rent": 1800,
      |        "year2Rent": 1800,
      |        "year3Rent": 1800,
      |        "year4Rent": 1800,
      |        "year5Rent": 1800
      |      },
      |      "leaseTerm": {
      |        "years": 175,
      |        "days": 1,
      |        "daysInPartialYear": 365
      |      }
      |    },
      |    "premium": 500000
      |}""".stripMargin)
    .as[JsObject]

  val slabUaData: JsObject = Json
    .parse("""{
      |    "holdingType": "Freehold",
      |    "propertyType": "NonResidential",
      |    "effectiveDate": "2021-07-01",
      |    "premium": 300000
      |}""".stripMargin)
    .as[JsObject]

  def summaryListHelper(tryUa: Try[UserAnswers], withAction: Boolean = true)(implicit
      messages: Messages
  ): Option[SummaryList] = {
    tryUa match {
      case Failure(_) => None
      case Success(ua) =>
        Some(
          SummaryListViewModel(
            rows = StartAgainActionSummaryRow.row() +: Seq(
              HoldingSummary.row(ua, withAction = withAction),
              PropertySummary.row(ua, withAction = withAction),
              EffectiveDateSummary.row(ua, withAction = withAction),
              IsAdditionalPropertySummary.row(ua, withAction = withAction),
              MainResidenceSummary.row(ua, withAction = withAction),
              NonUkResidentSummary.row(ua, withAction = withAction),
              OwnsOtherPropertiesSummary.row(ua, withAction = withAction),
              PurchasePriceSummary.row(ua, withAction = withAction),
              PurchaserSummary.row(ua, withAction = withAction)
            ).flatten
          )
        )
    }
  }

  def summaryRowHelper(
      messageKey: String,
      stringValue: Option[String] = None,
      currencyValue: Option[Int] = None,
      dateValue: Option[LocalDate] = None
  )(messages: Messages): SummaryListRow = {
    val valueText = (stringValue, currencyValue, dateValue) match {
      case (Some(thisValue), None, None) => Value(Text(thisValue)).withCssClass(valueCssClass)
      case (None, Some(thisValue), None) => Value(Text(bigDecimalFormat(thisValue))).withCssClass(valueCssClass)
      case (None, None, Some(thisValue)) =>
        Value(Text(thisValue.format(localDateTimeFormatter()))).withCssClass(valueCssClass)
    }
    SummaryListRow(
      key = Key(Text(messages(messageKey))).withCssClass(keyCssClass),
      value = valueText
    )
  }

  def minResultDisplayTable()(implicit messages: Messages) = {

    ResultDisplayTable(
      resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
      resultHint = None,
      totalTax = 2000,
      netPresentValue = None,
      summaryTable = SummaryList(
        Seq(
          summaryRowHelper(
            messageKey = "effectiveDate.resultLabel",
            dateValue = Some(LocalDate.of(2025, 1, 1)))(
            messages
          ),
          summaryRowHelper("purchasePrice.resultLabel", currencyValue = Some(600000))(messages),
          summaryRowHelper("totalTax.resultLabel", Some("2000"))(messages)
        )
      ),
      taxesDue = Seq.empty,
      viewDetailsLink = controllers.scalabuild.routes.DetailController.onPageLoad(0).url
    )
  }
  val sliceFreeResUkIndTwoMain: Seq[SliceDetails] = List(
    SliceDetails(0,Some(250000),0,0),
    SliceDetails(250000,Some(925000),5,12500),
    SliceDetails(925000,Some(1500000),10,0),
    SliceDetails(1500000,None,12,0))
  
  val detailFreeResUkIndTwoMain: Seq[DetailTableRow] = DetailSummary.tableRows(sliceFreeResUkIndTwoMain)

}
