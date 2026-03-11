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
import models.scalabuild.{
  DisplayFreehold,
  DisplayLeasehold,
  DisplaySlab,
  DisplayType,
  HoldingTypes,
  RequestFromMongo,
  ResultDisplayTable,
  UserAnswers
}
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
    if (userAnswers.data == Json.parse("""{}""")){
      Left(ResultServiceError("[ResultService][getResultDisplayTableList] User answers was empty"))
    } else {
      holdingType.map { holding =>
        val calculationResult = calculationService.calculateTax(requestFromMongo.toRequest).result
        val isSlabInResult = calculationResult.exists(_.taxCalcs.exists(_.calcType.equals(CalcTypes.slab)))
        val resultsDisplayTable = calculationResult.zipWithIndex.map { case (results, index) =>
          val slabOrSlice = results.taxCalcs.head.calcType
          val displayType = getDisplayType(holding, slabOrSlice)

          ResultDisplayTable(
            resultHeading = results.resultHeading,
            resultHint = results.resultHint,
            summaryTable = displayType match {
              case DisplaySlab =>
                ResultSummarySlab(userAnswers, results.totalTax, results.taxCalcs.head.rate, holding, index)
              case DisplayFreehold =>
                ResultSummaryFreehold(userAnswers, results.totalTax, isSlabInResult, index)
              case DisplayLeasehold =>
                // results displayed need to have view calculation link as action in SummaryListRow, not at the bottom
                ResultSummaryLeasehold(results.totalTax, taxesDueByType(results.taxCalcs), results.npv, index)
              //impossible case
              case _ => {
                logger.error(
                  "\"[ResultService][getResultDisplayTableList] Could not infer Holding type and reached impossible case\""
                )
                throw new Exception(
                  "[ResultService][getResultDisplayTableList] Could not infer Holding type and reached impossible case"
                )
              }
            },
            totalTax = results.totalTax,
            netPresentValue = results.npv,
            taxesDue = taxesDueByType(results.taxCalcs),
            viewDetailsLink = if (displayType == DisplayFreehold) {
                  Some(controllers.scalabuild.routes.DetailController.onPageLoad(Some(index), Some(0)).url)
                } else {
              None
            }
          )
        }
        resultsDisplayTable
      }.toRight(ResultServiceError("[ResultService][getResultDisplayTableList] Could not get holding Type"))
    }
  }

  private def getDisplayType(holdingType: HoldingTypes, slabOrSlice: CalcTypes.Value): DisplayType = {
    (holdingType, slabOrSlice) match {
      case (_, CalcTypes.slab) => DisplaySlab
      case (HoldingTypes.Freehold, _) => DisplayFreehold
      case (HoldingTypes.Leasehold, _) => DisplayLeasehold
    }
  }

  private def taxesDueByType(taxCalcs: Seq[CalculationDetails]): Seq[(TaxTypes.Value, Int, Option[Int])] = {
    taxCalcs.map(taxCalc => (taxCalc.taxType, taxCalc.taxDue, taxCalc.rate))
  }

  private def ResultSummaryFreehold(ua: UserAnswers, totalTax: Int, slabInResult: Boolean, index: Int)(implicit messages: Messages): SummaryList = {
    val summaryTable = SummaryListViewModel(
      rows = Seq(
        EffectiveDateSummary.row(ua, withAction = false, index = Some(index), resultTable = true),
        PurchasePriceSummary.row(ua, withAction = false, index = Some(index), resultTable = true)
      ).flatten :+ TotalDueSummary.row(totalTax, Freehold, slab = slabInResult, index)
    )
    summaryTable
  }

  private def ResultSummarySlab(ua: UserAnswers, totalTax: Int, rate: Option[Int], holding: HoldingTypes, index: Int)(implicit messages: Messages): SummaryList = {
    val summaryTable = SummaryListViewModel(
      rows = Seq(
        EffectiveDateSummary.row(ua, withAction = false, index =Some(index), resultTable = true),
        PurchasePriceSummary.row(ua, withAction = false, index = Some(index), resultTable = true),
        RateSummary.row(rate, index=index)
      ).flatten :+ TotalDueSummary.row(totalTax, holding, slab = true, index)
    )
    summaryTable
  }

  private def ResultSummaryLeasehold(
      totalTax: Int,
      taxCalcs: Seq[(TaxTypes.Value, Int, Option[Int])],
      nvp: Option[Int],
      index: Int
  )(implicit messages: Messages): SummaryList = {
    val taxesSummaryRows: Seq[SummaryListRow] =
      taxCalcs.zipWithIndex.map{ case (taxTotal, taxCalcIndex) => TaxesDueByTypeSummary.row(taxTotal._1, taxTotal._2, rate = taxTotal._3, resultIndex = index, taxCalcIndex = taxCalcIndex)}
    val totalDueSummary: SummaryListRow = TotalDueSummary.row(totalTax, Leasehold, index = index)
    val nvpRow = NpvSummary.row(nvp, index).toSeq

    val rows:Seq[SummaryListRow] = Seq(totalDueSummary) ++ nvpRow ++ taxesSummaryRows
    val summaryTable = SummaryListViewModel(
      rows = rows
    )
    summaryTable
  }
}
