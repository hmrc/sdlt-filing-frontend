package calculation.data

import calculation.models.calculationtables.{Slice, SliceTable}

object SliceRatesTables {

  val freeholdResidentialDec14OnwardsRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 0),
      Slice(from = 125000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val freeholdNonResidentialMar16Onwards = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = None,  rate = 5)
    )
  )

}
