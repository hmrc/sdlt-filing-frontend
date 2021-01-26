/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.data

import calculation.models.calculationtables.{Slice, SliceTable}

object LeaseholdSliceRatesTables {

  //Non-residential Rates

  val leaseholdNonResidentialMar12toMar16LeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
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

  //Residential Lease Rates

  val leaseholdResidentialApr21OnwardsNonUKResLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 2),
      Slice(from = 125000, to = None,         rate = 3)
    )
  )

  val leaseholdResidentialJuly20OnwardsLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(500000), rate = 0),
      Slice(from = 500000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialDec14OnwardsLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialMar12toDec14LeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //Residential Premium Rates

  val leaseholdResidentialApr21OnwardsNonUKResPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 2),
      Slice(from = 125000,  to = Some(250000),  rate = 4),
      Slice(from = 250000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  val leaseholdResidentialJuly20nwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 0),
      Slice(from = 500000,   to = Some(925000),   rate = 5),
      Slice(from = 925000,   to = Some(1500000),  rate = 10),
      Slice(from = 1500000,  to = None,           rate = 12)
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

  //Additional Property Lease Rates

  val leaseholdResidentialAddPropApr16OnwardsLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //Additional Property Premium Rates

  val leaseholdResidentialAddPropNonUKResApr21OnwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 5),
      Slice(from = 125000,   to = Some(250000),   rate = 7),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val leaseholdResidentialAddPropJuly20OnwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 3),
      Slice(from = 500000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val leaseholdResidentialAddPropApr16OnwardsPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 3),
      Slice(from = 125000,   to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  //First Time Buyer Lease Rates (FTB)

  val leaseholdResidentialApr21FTBNonUKResLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 2),
      Slice(from = 125000, to = None,         rate = 3)
    )
  )

  val leaseholdResidentialJuly20FTBLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(500000), rate = 0),
      Slice(from = 500000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialNov17FTBLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //First Time Buyer Premium Rates (FTB)

  val leaseholdResidentialApr21OnwardsFTBNonUKResPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 2),
      Slice(from = 300000,   to = Some(500000),   rate = 7)
    )
  )

  val leaseholdResidentialJuly20OnwardsFTBPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 0)
    )
  )

  val leaseholdResidentialNov17OnwardsFTBPremiumRates = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 0),
      Slice(from = 300000,   to = Some(500000),   rate = 5)
    )
  )

  //Shared Ownership & First Time Buyer Rates (FTB)

  val leaseholdResidentialApr21FTBSharedNonUKResLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 0)
    )
  )

  val leaseholdResidentialJuly20FTBSharedLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 0)
    )
  )

  val leaseholdResidentialNov17FTBSharedLeaseRates = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 0)
    )
  )

}
