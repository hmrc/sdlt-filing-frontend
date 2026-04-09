/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.scalabuild

import errors.{ResultServiceError, ServiceError}
import controllers.scalabuild.routes
import enums.TaxTypes.{premium, rent}
import enums.{CalcTypes, TaxTypes}
import fixtures.scalabuild.{FreeholdRequestFromMongo, TestObjects}
import models._
import models.scalabuild.{HoldingTypes, ResultDisplayTable, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import services.CalculationService
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.scalabuild.TotalDueSummary
import viewmodels.scalabuild.checkanswerssummary.{EffectiveDateSummary, PurchasePriceSummary}

import java.time.Instant

class ResultServiceSpec
    extends PlaySpec
    with MockitoSugar
    with TestObjects
    with GuiceOneAppPerSuite
    with FreeholdRequestFromMongo {

  val mockCalculationService: CalculationService = mock[CalculationService]

  val testResultService = new ResultService(
    mockCalculationService
  )

  val leaseRentSliceDetails = Seq(
    SliceDetails(from = 0, to = Some(250000), rate = 2, taxDue = 1026),
    SliceDetails(from = 250000, to = None, rate = 3, taxDue = 0)
  )
  val leasePremiumSliceDetails = Seq(
    SliceDetails(from = 0, to = Some(250000), rate = 7, taxDue = 17500),
    SliceDetails(from = 250000, to = Some(925000), rate = 12, taxDue = 30000),
    SliceDetails(from = 925000, to = Some(250000), rate = 7, taxDue = 17500),
    SliceDetails(from = 0, to = Some(1500000), rate = 17, taxDue = 0),
    SliceDetails(from = 1500000, to = None, rate = 3, taxDue = 0)
  )

  val leasePremiumSliceDetails2 = Seq(
    SliceDetails(from = 0, to = Some(250000), rate = 7, taxDue = 5000),
    SliceDetails(from = 250000, to = Some(925000), rate = 12, taxDue = 17500),
    SliceDetails(from = 925000, to = Some(250000), rate = 7, taxDue = 0),
    SliceDetails(from = 0, to = Some(1500000), rate = 17, taxDue = 0),
    SliceDetails(from = 1500000, to = None, rate = 3, taxDue = 0)
  )

  val premSliceDetails = Seq(
    SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
    SliceDetails(from = 300000, to = Some(500000), rate = 5, taxDue = 0)
  )

  val freeResCalculationResult = Result(
    totalTax = 20000,
    resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
    resultHint = Some(
      s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
        s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
    ),
    npv = None,
    taxCalcs = Seq(
      CalculationDetails(
        taxType = TaxTypes.premium,
        calcType = CalcTypes.slice,
        detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
        bandHeading = Some("Rent bands (£)"),
        detailFooter = Some("SDLT due on the rent"),
        taxDue = 20000,
        slices = Some(premSliceDetails)
      )
    )
  )

  def resultDisplayTables(holding: HoldingTypes)(implicit messages: Messages) = Seq(
    ResultDisplayTable(
      resultHeading = freeResCalculationResult.resultHeading,
      resultHint = freeResCalculationResult.resultHint,
      totalTax = freeResCalculationResult.totalTax,
      netPresentValue = freeResCalculationResult.npv,
      summaryTable = SummaryList(
        rows = Seq(
          EffectiveDateSummary.row(emptyUserAnswers2.copy(data = freeResNonIndAddMainUaData), withAction = false),
          PurchasePriceSummary.row(emptyUserAnswers2.copy(data = freeResNonIndAddMainUaData), withAction = false)
        ).flatten :+ TotalDueSummary.row(freeResCalculationResult.totalTax, holding, index = 0)
      ),
      taxesDue =
        Seq((freeResCalculationResult.taxCalcs.head.taxType, freeResCalculationResult.taxCalcs.head.taxDue, None)),
      viewDetailsLink = Some(routes.DetailController.onPageLoad(Some(0), Some(0)).url)
    )
  )
  val validUserAnswers = freeResNonIndAddMainJourney
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "getResultDisplayTableList" must {
    "return Service Error" when {
      "When the user Answers is empty" in {
        val emptyAnswers = emptyUserAnswers2
        val errorResult: Either[ServiceError, Seq[ResultDisplayTable]] =
          testResultService.getResultDisplayTableList(emptyAnswers, freeResRequestFromMongo)(messages)

        val error = Left(ResultServiceError("[ResultService][getResultDisplayTableList] User answers was empty"))
        errorResult shouldBe error

      }
    }
    "return an empty result" when {
      "When valid userAnswers and RequestFromMongo are given but we receive an empty calculation response" in {
        when(mockCalculationService.calculateTax(any())).thenReturn(CalculationResponse(Seq()))
        val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
          testResultService.getResultDisplayTableList(validUserAnswers.get, freeResRequestFromMongo)(messages)

        resultDisplayTable shouldBe Right(Seq())
      }
    }
    "return a results display table with a list of 1 result" when {
      "When 1 result is received in calculation details and holding type is Freehold" in {

        val freeResSummaryList = SummaryList(
          List(
            SummaryListRow(
              key = Key(Text("Effective date")),
              value = Value(content = HtmlContent("""<span id="effDate0">1 January 2025</span>""")),
              actions = None
            ),
            SummaryListRow(
              Key(Text("Purchase price (£)")),
              Value(content = HtmlContent("""<span id="premium0">500,000</span>""")),
              actions = None
            ),
            SummaryListRow(
              Key(Text("Total tax")),
              Value(content = HtmlContent("""<span id="totalTax0">20,000</span>""")),
              actions = None
            )
          )
        )

        val freeResFirstExpectedResultsTable = ResultDisplayTable(
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = Some(
            "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
          ),
          totalTax = 20000,
          netPresentValue = None,
          summaryTable = freeResSummaryList,
          taxesDue = List((premium, 20000, None)),
          viewDetailsLink = Some(routes.DetailController.onPageLoad(Some(0), Some(0)).url)
        )

        val userAnswers = UserAnswers(
          id = "some-id",
          data = freeResNonIndAddMainUaData,
          lastUpdated = Instant.parse("2025-09-02T14:00:00Z")
        )

        when(mockCalculationService.calculateTax(freeResRequestFromMongo.toRequest))
          .thenReturn(CalculationResponse(Seq(freeResCalculationResult)))

        val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
          testResultService.getResultDisplayTableList(userAnswers, freeResRequestFromMongo)(messages)

        resultDisplayTable shouldBe Right(Seq(freeResFirstExpectedResultsTable))
      }
    }
    "1 result is received and holdingType is Leasehold" in {
      val leaseRentSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None, rate = 1, taxDue = 0)
      )
      val leasePremiumSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 175000),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 30000),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 175000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val leaseResCalculationResult = Result(
        totalTax = 15000,
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = None,
        npv = Some(51303),
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.rent,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = 0,
            slices = Some(leaseRentSliceDetails)
          ),
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Premium bands (£)"),
            detailFooter = Some("SDLT due on the premium"),
            taxDue = 15000,
            slices = Some(leasePremiumSliceDetails)
          )
        )
      )

      val leaseResSummaryList = SummaryList(
        List(
          SummaryListRow(
            Key(Text("Total amount of tax for this transaction")),
            Value(content = HtmlContent("""<span id="totalTax0">£15,000</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Net Present Value")),
            Value(HtmlContent("""<span id="npv0">£51,303</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(HtmlContent(s"""<span id="taxType00">SDLT on Rent</span>""")),
            Value(HtmlContent(s"""<span id="taxDue00">£0</span>""")),
            actions = Some(
              Actions(
                items = List(
                  ActionItem(
                    href = routes.DetailController.onPageLoad(Some(0), Some(0)).url,
                    content = Text("View calculation"),
                    visuallyHiddenText = Some("View calculation"),
                    attributes = Map("id" -> "detailCalc00")
                  )
                )
              )
            )
          ),
          SummaryListRow(
            Key(HtmlContent(s"""<span id="taxType01">SDLT on Premium</span>""")),
            Value(HtmlContent(s"""<span id="taxDue01">£15,000</span>""")),
            actions = Some(
              Actions(
                items = List(
                  ActionItem(
                    href = routes.DetailController.onPageLoad(Some(0), Some(1)).url,
                    content = Text("View calculation"),
                    visuallyHiddenText = Some("View calculation"),
                    attributes = Map("id" -> "detailCalc01")
                  )
                )
              )
            )
          )
        )
      )

      val leaseResFirstExpectedResultsTable = ResultDisplayTable(
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = None,
        totalTax = 15000,
        netPresentValue = Some(51303),
        summaryTable = leaseResSummaryList,
        taxesDue = List((rent, 0, None), (premium, 15000, None)),
        viewDetailsLink = None
      )

      val userAnswers = UserAnswers(
        id = "some-id",
        data = leaseResNonIndAddMainUaData,
        lastUpdated = Instant.parse("2025-09-02T14:00:00Z")
      )

      when(mockCalculationService.calculateTax(any())).thenReturn(CalculationResponse(Seq(leaseResCalculationResult)))

      val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
        testResultService.getResultDisplayTableList(userAnswers, freeResRequestFromMongo)(messages)

      resultDisplayTable shouldBe Right(Seq(leaseResFirstExpectedResultsTable))
    }
    "return a results display table with a list of 2 results" when {
      "a list of 2 results is received and holdingType is Leasehold" in {

        val leaseResCalculationResult1 = Result(
          totalTax = 48426,
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = Some(
            s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
              s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
          ),
          npv = Some(23526),
          taxCalcs = Seq(
            CalculationDetails(
              taxType = TaxTypes.rent,
              calcType = CalcTypes.slice,
              detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
              bandHeading = Some("Rent bands (£)"),
              detailFooter = Some("SDLT due on the rent"),
              taxDue = 1026,
              slices = Some(leaseRentSliceDetails)
            ),
            CalculationDetails(
              taxType = TaxTypes.premium,
              calcType = CalcTypes.slice,
              detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
              bandHeading = Some("Premium bands (£)"),
              detailFooter = Some("SDLT due on the premium"),
              taxDue = 47400,
              slices = Some(leasePremiumSliceDetails)
            )
          )
        )

        val leaseResCalculationResult2 = Result(
          totalTax = 22500,
          resultHeading =
            Some("Result if you become eligible for a repayment of the higher rate on additional dwellings"),
          resultHint = Some(
            s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
              s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
          ),
          npv = Some(23526),
          taxCalcs = Seq(
            CalculationDetails(
              taxType = TaxTypes.rent,
              calcType = CalcTypes.slice,
              detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
              bandHeading = Some("Rent bands (£)"),
              detailFooter = Some("SDLT due on the rent"),
              taxDue = 0,
              slices = Some(leaseRentSliceDetails)
            ),
            CalculationDetails(
              taxType = TaxTypes.premium,
              calcType = CalcTypes.slice,
              detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
              bandHeading = Some("Premium bands (£)"),
              detailFooter = Some("SDLT due on the premium"),
              taxDue = 22500,
              slices = Some(leasePremiumSliceDetails2)
            )
          )
        )

        val leaseResSummaryList1 = SummaryList(
          List(
            SummaryListRow(
              Key(Text("Total amount of tax for this transaction")),
              Value(content = HtmlContent("""<span id="totalTax0">£48,426</span>""")),
              actions = None
            ),
            SummaryListRow(
              Key(Text("Net Present Value")),
              Value(HtmlContent("""<span id="npv0">£23,526</span>""")),
              actions = None
            ),
            SummaryListRow(
              key = Key(HtmlContent(s"""<span id="taxType00">SDLT on Rent</span>""")),
              Value(HtmlContent(s"""<span id="taxDue00">£1,026</span>""")),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      href = routes.DetailController.onPageLoad(Some(0), Some(0)).url,
                      content = Text("View calculation"),
                      visuallyHiddenText = Some("View calculation"),
                      attributes = Map("id" -> "detailCalc00")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              key = Key(HtmlContent(s"""<span id="taxType01">SDLT on Premium</span>""")),
              Value(HtmlContent(s"""<span id="taxDue01">£47,400</span>""")),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      href = routes.DetailController.onPageLoad(Some(0), Some(1)).url,
                      content = Text("View calculation"),
                      visuallyHiddenText = Some("View calculation"),
                      attributes = Map("id" -> "detailCalc01")
                    )
                  )
                )
              )
            )
          )
        )

        val leaseResSummaryList2 = SummaryList(
          List(
            SummaryListRow(
              Key(Text("Total amount of tax for this transaction")),
              Value(content = HtmlContent("""<span id="totalTax1">£22,500</span>""")),
              actions = None
            ),
            SummaryListRow(
              Key(Text("Net Present Value")),
              Value(HtmlContent("""<span id="npv1">£23,526</span>""")),
              actions = None
            ),
            SummaryListRow(
              key = Key(HtmlContent(s"""<span id="taxType10">SDLT on Rent</span>""")),
              Value(HtmlContent(s"""<span id="taxDue10">£0</span>""")),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      href = routes.DetailController.onPageLoad(Some(1), Some(0)).url,
                      content = Text("View calculation"),
                      visuallyHiddenText = Some("View calculation"),
                      attributes = Map("id" -> "detailCalc10")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              key = Key(HtmlContent(s"""<span id="taxType11">SDLT on Premium</span>""")),
              Value(HtmlContent(s"""<span id="taxDue11">£22,500</span>""")),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      href = routes.DetailController.onPageLoad(Some(1), Some(1)).url,
                      content = Text("View calculation"),
                      visuallyHiddenText = Some("View calculation"),
                      attributes = Map("id" -> "detailCalc11")
                    )
                  )
                )
              )
            )
          )
        )

        val leaseResFirstExpectedResultsTable1 = ResultDisplayTable(
          resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
          resultHint = Some(
            "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
          ),
          totalTax = 48426,
          netPresentValue = Some(23526),
          summaryTable = leaseResSummaryList1,
          taxesDue = List((rent, 1026, None), (premium, 47400, None)),
          viewDetailsLink = None
        )

        val leaseResFirstExpectedResultsTable2 = ResultDisplayTable(
          resultHeading =
            Some("Result if you become eligible for a repayment of the higher rate on additional dwellings"),
          resultHint = Some(
            "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
          ),
          totalTax = 22500,
          netPresentValue = Some(23526),
          summaryTable = leaseResSummaryList2,
          taxesDue = List((rent, 0, None), (premium, 22500, None)),
          viewDetailsLink = None
        )

        val userAnswers = UserAnswers(
          id = "some-id",
          data = leaseResNonIndAddMainUaData,
          lastUpdated = Instant.parse("2025-09-02T14:00:00Z")
        )

        when(mockCalculationService.calculateTax(any()))
          .thenReturn(CalculationResponse(Seq(leaseResCalculationResult1, leaseResCalculationResult2)))

        val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
          testResultService.getResultDisplayTableList(userAnswers, freeResRequestFromMongo)(messages)

        resultDisplayTable shouldBe Right(List(leaseResFirstExpectedResultsTable1, leaseResFirstExpectedResultsTable2))
      }
    }
    "1 slice and 1 slab calculation type is received" in {

      val firstCalculationResult1 = Result(
        totalTax = 4500,
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
        ),
        npv = None,
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = 4500,
            slices = Some(leasePremiumSliceDetails)
          )
        )
      )

      val slabCalculationResult = Result(
        totalTax = 9000,
        resultHeading =
          Some("Result if you become eligible for a repayment of the higher rate on additional dwellings"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slab,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = 9000,
            rate = Some(3),
            slices = None
          )
        )
      )

      val firstSummaryList1 = SummaryList(
        List(
          SummaryListRow(
            Key(Text("Effective date")),
            value = Value(content = HtmlContent("""<span id="effDate0">1 July 2021</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Purchase price (£)")),
            Value(content = HtmlContent("""<span id="premium0">300,000</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Total SDLT due (£)")),
            Value(content = HtmlContent("""<span id="totalTax0">4,500</span>""")),
            actions = None
          )
        )
      )

      val slabSummaryList = SummaryList(
        List(
          SummaryListRow(
            Key(Text("Effective date")),
            value = Value(content = HtmlContent("""<span id="effDate1">1 July 2021</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Purchase price (£)")),
            Value(content = HtmlContent("""<span id="premium1">300,000</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Percentage rate (%)")),
            Value(HtmlContent("""<span id="taxRate1">3</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Total SDLT due (£)")),
            Value(content = HtmlContent("""<span id="totalTax1">9,000</span>""")),
            actions = None
          )
        )
      )

      val firstExpectedResultsTable1 = ResultDisplayTable(
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
        ),
        totalTax = 4500,
        netPresentValue = None,
        summaryTable = firstSummaryList1,
        taxesDue = List((premium, 4500, None)),
        viewDetailsLink = Some(routes.DetailController.onPageLoad(Some(0), Some(0)).url)
      )

      val slabExpectedResultsTable = ResultDisplayTable(
        resultHeading =
          Some("Result if you become eligible for a repayment of the higher rate on additional dwellings"),
        resultHint = None,
        totalTax = 9000,
        netPresentValue = None,
        summaryTable = slabSummaryList,
        taxesDue = List((premium, 9000, Some(3))),
        viewDetailsLink = None
      )

      val userAnswers = UserAnswers(
        id = "some-id",
        data = slabUaData,
        lastUpdated = Instant.parse("2025-09-02T14:00:00Z")
      )

      when(mockCalculationService.calculateTax(any()))
        .thenReturn(CalculationResponse(Seq(firstCalculationResult1, slabCalculationResult)))

      val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
        testResultService.getResultDisplayTableList(userAnswers, slabRequest)(messages)

      resultDisplayTable shouldBe Right(List(firstExpectedResultsTable1, slabExpectedResultsTable))
    }
    "When 2 slice calculation type results are received" in {
      val freeResCalculationResult1 = Result(
        totalTax = 20000,
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
            s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
        ),
        npv = None,
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = 20000,
            slices = Some(premSliceDetails)
          )
        )
      )

      val freeResCalculationResult2 = Result(
        totalTax = 20000,
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          s"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date."
        ),
        npv = None,
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = 20000,
            slices = Some(premSliceDetails)
          )
        )
      )

      val freeResSummaryList1 = SummaryList(
        List(
          SummaryListRow(
            Key(Text("Effective date")),
            value = Value(content = HtmlContent("""<span id="effDate0">1 January 2025</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Purchase price (£)")),
            Value(content = HtmlContent("""<span id="premium0">500,000</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Total tax")),
            Value(content = HtmlContent("""<span id="totalTax0">20,000</span>""")),
            actions = None
          )
        )
      )
      val freeResSummaryList2 = SummaryList(
        List(
          SummaryListRow(
            Key(Text("Effective date")),
            value = Value(content = HtmlContent("""<span id="effDate1">1 January 2025</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Purchase price (£)")),
            Value(content = HtmlContent("""<span id="premium1">500,000</span>""")),
            actions = None
          ),
          SummaryListRow(
            Key(Text("Total tax")),
            Value(content = HtmlContent("""<span id="totalTax1">20,000</span>""")),
            actions = None
          )
        )
      )

      val freeResFirstExpectedResultsTable = ResultDisplayTable(
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £3000."
        ),
        totalTax = 20000,
        netPresentValue = None,
        summaryTable = freeResSummaryList1,
        taxesDue = List((premium, 20000, None)),
        viewDetailsLink = Some(routes.DetailController.onPageLoad(Some(0), Some(0)).url)
      )
      val freeResSecondExpectedResultsTable = ResultDisplayTable(
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = Some(
          "If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date."
        ),
        totalTax = 20000,
        netPresentValue = None,
        summaryTable = freeResSummaryList2,
        taxesDue = List((premium, 20000, None)),
        viewDetailsLink = Some(routes.DetailController.onPageLoad(Some(1), Some(0)).url)
      )

      val userAnswers = UserAnswers(
        id = "some-id",
        data = freeResNonIndAddMainUaData,
        lastUpdated = Instant.parse("2025-09-02T14:00:00Z")
      )

      when(mockCalculationService.calculateTax(freeResRequestFromMongo.toRequest))
        .thenReturn(CalculationResponse(Seq(freeResCalculationResult1, freeResCalculationResult2)))

      val resultDisplayTable: Either[ServiceError, Seq[ResultDisplayTable]] =
        testResultService.getResultDisplayTableList(userAnswers, freeResRequestFromMongo)(messages)

      resultDisplayTable shouldBe Right(Seq(freeResFirstExpectedResultsTable, freeResSecondExpectedResultsTable))
    }

  }
}
