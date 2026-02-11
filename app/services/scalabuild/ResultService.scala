/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.scalabuild
import Errors.{ResultServiceError, ServiceError}
import enums.{CalcTypes, TaxTypes}
import jakarta.inject.Singleton
import models.CalculationDetails
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import models.scalabuild.{HoldingTypes, RequestFromMongo, ResultDisplayTable, UserAnswers}
import pages.scalabuild.HoldingPage
import play.api.i18n.Lang.logger
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.CalculationService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.scalabuild.checkanswerssummary.{EffectiveDateSummary, PurchasePriceSummary}
import viewmodels.scalabuild.govuk.summarylist.SummaryListViewModel
import viewmodels.scalabuild.{NpvSummary, RateSummary, TaxesDueByTypeSummary, TotalDueSummary}

import javax.inject.Inject

@Singleton
class ResultService @Inject() (
    calculationService: CalculationService
) {

  def getResultDisplayTableList(
      userAnswers: UserAnswers,
      requestFromMongo: RequestFromMongo
  )(implicit messages: Messages): Either[ServiceError, Seq[ResultDisplayTable]] = {
    val holdingType = userAnswers.get(HoldingPage)
    if (userAnswers.data == Json.parse("""{}"""))
      Left(ResultServiceError("[ResultService][getResultDisplayTableList] User answers was empty"))
    else if (holdingType.isEmpty) {
      Left(ResultServiceError("[ResultService][getResultDisplayTableList] Could not get holding type"))
    } else {
      val calculationResult = calculationService.calculateTax(requestFromMongo.toRequest).result
      val isSlabInResult = calculationResult.exists(_.taxCalcs.exists(_.calcType.equals(CalcTypes.slab)))
      val resultsDisplayTable = calculationResult.zipWithIndex.map { case (results, index) =>
        val slabOrSlice = results.taxCalcs.head.calcType

        ResultDisplayTable(
          resultHeading = results.resultHeading,
          resultHint = results.resultHint,
          summaryTable = (holdingType, slabOrSlice) match {
            case (Some(holding), CalcTypes.slab) =>
              ResultSummarySlab(userAnswers, results.totalTax, results.taxCalcs.head.rate, holding)
            case (Some(HoldingTypes.Freehold), _) =>
              ResultSummaryFreehold(userAnswers, results.totalTax, isSlabInResult)
            case (Some(HoldingTypes.Leasehold), _) =>
              ResultSummaryLeasehold(results.totalTax, taxesDueByType(results.taxCalcs), results.npv)
              //impossible case
            case _ => {
                logger.error("\"[ResultService][getResultDisplayTableList] Could not infer Holding type and reached impossible case\"")
                throw new Exception(
                  "[ResultService][getResultDisplayTableList] Could not infer Holding type and reached impossible case"
                )
              }
          },
          totalTax = results.totalTax,
          netPresentValue = results.npv,
          taxesDue = taxesDueByType(results.taxCalcs),
          viewDetailsLink = controllers.scalabuild.routes.DetailController.onPageLoad(index).url
        )
      }
      Right(resultsDisplayTable)
    }

  }

  private def taxesDueByType(taxCalcs: Seq[CalculationDetails]): Seq[(TaxTypes.Value, Int)] = {
    taxCalcs.map(taxCalc => (taxCalc.taxType, taxCalc.taxDue))
  }
  private def ResultSummaryFreehold(ua: UserAnswers, totalTax: Int, slabInResult: Boolean)(implicit messages: Messages): SummaryList = {
    val summaryTable = SummaryListViewModel(
      rows = Seq(
        EffectiveDateSummary.row(ua, withAction = false),
        PurchasePriceSummary.row(ua, withAction = false)
      ).flatten :+ TotalDueSummary.row(totalTax, Freehold, slab = slabInResult)
    )
    summaryTable
  }

  private def ResultSummarySlab(ua: UserAnswers, totalTax: Int, rate: Option[Int], holding: HoldingTypes )(implicit messages: Messages): SummaryList = {
    val summaryTable = SummaryListViewModel(
      rows = Seq(
        EffectiveDateSummary.row(ua, withAction = false),
        PurchasePriceSummary.row(ua, withAction = false),
        RateSummary.row(rate)
      ).flatten :+ TotalDueSummary.row(totalTax, holding, slab = true)
    )
    summaryTable
  }

  private def ResultSummaryLeasehold(
      totalTax: Int,
      taxCalcs: Seq[(TaxTypes.Value, Int)],
      nvp: Option[Int]
  )(implicit messages: Messages): SummaryList = {
    val taxesSummaryRows: Seq[SummaryListRow] =
      taxCalcs.map(taxTotal => TaxesDueByTypeSummary.row(taxTotal._1, taxTotal._2))
    val totalDueSummary: SummaryListRow = TotalDueSummary.row(totalTax, Leasehold)
    val nvpRow = NpvSummary.row(nvp).toSeq

    val rows:Seq[SummaryListRow] = Seq(totalDueSummary) ++ nvpRow ++ taxesSummaryRows
    val summaryTable = SummaryListViewModel(
      rows = rows
    )
    summaryTable
  }
}
