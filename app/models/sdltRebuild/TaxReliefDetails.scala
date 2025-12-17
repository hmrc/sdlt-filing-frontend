/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.sdltRebuild

import enums.sdltRebuild.TaxReliefCode
import play.api.libs.json.{Json, Reads}

case class TaxReliefDetails(
                             taxReliefCode: TaxReliefCode,
                             isPartialRelief: Option[Boolean]
                             )

object TaxReliefDetails {
  implicit val reads: Reads[TaxReliefDetails] = Json.reads[TaxReliefDetails]
}
