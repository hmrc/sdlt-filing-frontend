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

  val freeholdNonResidentialMar16OnwardsRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = None,          rate = 5)
    )
  )

  val leaseholdNonResidentialMar12toMar16LeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(250000),  rate = 0),
      Slice(from = 250000,  to = None,          rate = 1)
    )
  )

  val leaseholdNonResidentialMar16OnwardsLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(150000),   rate = 0),
      Slice(from = 150000,   to = Some(5000000),  rate = 1),
      Slice(from = 5000000,  to = None,           rate = 2)
    )
  )

  val leaseholdNonResidentialMar16OnwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = None,          rate = 5)
    )
  )

  val freeholdResidentialAddPropApr16OnwardsRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 3),
      Slice(from = 125000,   to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val leaseholdResidentialDec14OnwardsLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialDec14OnwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 0),
      Slice(from = 125000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val leaseholdResidentialMar12toDec14LeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

}
