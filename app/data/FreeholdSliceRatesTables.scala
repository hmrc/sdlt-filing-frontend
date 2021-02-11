/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package data

import models.calculationtables.{Slice, SliceTable}

object FreeholdSliceRatesTables {

  //Standard Residential Rates

  val freeholdResidentialJuly21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(250000),  rate = 0),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val freeholdResidentialJuly20OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(500000),  rate = 0),
      Slice(from = 500000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val freeholdResidentialDec14OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 0),
      Slice(from = 125000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val freeholdNonResidentialMar16OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = None,          rate = 5)
    )
  )

  //NRSDLT Rates

  val freeholdResidentialOct21OnwardsNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 2),
      Slice(from = 125000,  to = Some(250000),  rate = 4),
      Slice(from = 250000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  val freeholdResidentialJuly21OnwardsNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  val freeholdResidentialApril21OnwardsNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(500000),  rate = 2),
      Slice(from = 500000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  //Additional Property Rates

  val freeholdResidentialAddPropNonUKResOct21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 5),
      Slice(from = 125000,   to = Some(250000),   rate = 7),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val freeholdResidentialAddPropNonUKResJuly21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val freeholdResidentialAddPropNonUKResApril21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 5),
      Slice(from = 500000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val freeholdResidentialAddPropOct21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 3),
      Slice(from = 125000,   to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val freeholdResidentialAddPropJuly21OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 3),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val freeholdResidentialAddPropJuly20OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 3),
      Slice(from = 500000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val freeholdResidentialAddPropApr16OnwardsRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 3),
      Slice(from = 125000,   to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  //First Time Buyer Rates (FTB)

  val freeholdResidentialOct21OnwardsFTBNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 2),
      Slice(from = 300000,   to = Some(500000),   rate = 7)
    )
  )

  val freeholdResidentialJuly21OnwardsFTBNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 2),
      Slice(from = 300000,   to = Some(500000),   rate = 7)
    )
  )

  val freeholdResidentialNov17OnwardsFTBRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 0),
      Slice(from = 300000,   to = Some(500000),   rate = 5)
    )
  )
}
