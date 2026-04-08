/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package data

import models.calculationtables.{Slice, SliceTable}

object LeaseholdSliceRatesTables {

  //Non-residential Rates

  val leaseholdNonResidentialMar12toMar16LeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )

  val leaseholdNonResidentialMar16OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(150000),   rate = 0),
      Slice(from = 150000,   to = Some(5000000),  rate = 1),
      Slice(from = 5000000,  to = None,           rate = 2)
    )
  )

  val leaseholdNonResidentialMar16OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = None,          rate = 5)
    )
  )

  val leaseholdMixedNonResidentialRightToBuyBeforeMarch08RentRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )

  val leaseholdMixedNonResidentialRightToBuyBeforeMarch2016Rates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )

  //Residential Lease Rates

  val leaseholdResidentialOct21OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialOct21OnwardsNonUKResLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 2),
      Slice(from = 125000, to = None,         rate = 3)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(250000), rate = 0),
      Slice(from = 250000, to = None,         rate = 1)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21OnwardsNonUKResLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(250000), rate = 2),
      Slice(from = 250000, to = None,         rate = 3)
    )
  )

  val leaseholdResidentialApr21OnwardsNonUKResLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(500000), rate = 2),
      Slice(from = 500000, to = None,         rate = 3)
    )
  )

  val leaseholdResidentialJuly20OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(500000), rate = 0),
      Slice(from = 500000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialDec14OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialMar12toDec14LeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //Residential Premium Rates

  val leaseholdResidentialOct21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 0),
      Slice(from = 125000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  val leaseholdResidentialOct21OnwardsNonUKResPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 2),
      Slice(from = 125000,  to = Some(250000),  rate = 4),
      Slice(from = 250000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(250000),  rate = 0),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21OnwardsNonUKResPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  val leaseholdResidentialApr21OnwardsNonUKResPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(500000),  rate = 2),
      Slice(from = 500000,  to = Some(925000),  rate = 7),
      Slice(from = 925000,  to = Some(1500000), rate = 12),
      Slice(from = 1500000, to = None,          rate = 14)
    )
  )

  val leaseholdResidentialJuly20OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 0),
      Slice(from = 500000,   to = Some(925000),   rate = 5),
      Slice(from = 925000,   to = Some(1500000),  rate = 10),
      Slice(from = 1500000,  to = None,           rate = 12)
    )
  )

  val leaseholdResidentialDec14OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(125000),  rate = 0),
      Slice(from = 125000,  to = Some(250000),  rate = 2),
      Slice(from = 250000,  to = Some(925000),  rate = 5),
      Slice(from = 925000,  to = Some(1500000), rate = 10),
      Slice(from = 1500000, to = None,          rate = 12)
    )
  )

  //Additional Property Lease Rates

  val leaseholdResidentialAddPropApr16OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //Additional Property Premium Rates

  val leaseholdResidentialAddPropNonUKResOct21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 5),
      Slice(from = 125000,   to = Some(250000),   rate = 7),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialAddPropNonUKResJuly21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialAddPropJuly21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 3),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val leaseholdResidentialAddPropOct24BeforeApr25PremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val leaseholdResidentialAddPropNonUKResOct24BeforeApr25PremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(250000),   rate = 7),
      Slice(from = 250000,   to = Some(925000),   rate = 12),
      Slice(from = 925000,   to = Some(1500000),  rate = 17),
      Slice(from = 1500000,  to = None,           rate = 19)
    )
  )

  val leaseholdResidentialAddPropApr25OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 5),
      Slice(from = 125000,   to = Some(250000),   rate = 7),
      Slice(from = 250000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val leaseholdResidentialAddPropNonUKResApr25OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 7),
      Slice(from = 125000,   to = Some(250000),   rate = 9),
      Slice(from = 250000,   to = Some(925000),   rate = 12),
      Slice(from = 925000,   to = Some(1500000),  rate = 17),
      Slice(from = 1500000,  to = None,           rate = 19)
    )
  )

  val leaseholdResidentialAddPropNonUKResApr21OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 5),
      Slice(from = 500000,   to = Some(925000),   rate = 10),
      Slice(from = 925000,   to = Some(1500000),  rate = 15),
      Slice(from = 1500000,  to = None,           rate = 17)
    )
  )

  val leaseholdResidentialAddPropJuly20OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(500000),   rate = 3),
      Slice(from = 500000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  val leaseholdResidentialAddPropApr16OnwardsPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(125000),   rate = 3),
      Slice(from = 125000,   to = Some(250000),   rate = 5),
      Slice(from = 250000,   to = Some(925000),   rate = 8),
      Slice(from = 925000,   to = Some(1500000),  rate = 13),
      Slice(from = 1500000,  to = None,           rate = 15)
    )
  )

  //First Time Buyer Lease Rates (FTB)

  val leaseholdResidentialOct21FTBNonUKResLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 2),
      Slice(from = 125000, to = None,         rate = 3)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21FTBNonUKResLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(250000), rate = 2),
      Slice(from = 250000, to = None,         rate = 3)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21FTBLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(250000), rate = 0),
      Slice(from = 250000, to = None,         rate = 1)
    )
  )

  val leaseholdResidentialNov17FTBLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 1)
    )
  )

  //First Time Buyer Premium Rates (FTB)

  // This rate can be used for after Sept 22
  val leaseholdResidentialSep22OnwardsFTBNonUKResRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(425000),   rate = 2),
      Slice(from = 425000,   to = Some(625000),   rate = 7)
    )
  )

  // This rate can be used for after Sept 22
  val leaseholdResidentialSep22OnwardsFTBRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(425000),   rate = 0),
      Slice(from = 425000,   to = Some(625000),   rate = 5)
    )
  )

  val leaseholdResidentialOct21OnwardsFTBNonUKResPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 2),
      Slice(from = 300000,   to = Some(500000),   rate = 7)
    )
  )

  val leaseholdResidentialJuly21OnwardsFTBNonUKResPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 2),
      Slice(from = 300000,   to = Some(500000),   rate = 7)
    )
  )

  val leaseholdResidentialJuly21OnwardsFTBPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 0),
      Slice(from = 300000,   to = Some(500000),   rate = 5)
    )
  )

  val leaseholdResidentialNov17OnwardsFTBPremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(300000),   rate = 0),
      Slice(from = 300000,   to = Some(500000),   rate = 5)
    )
  )

  //Shared Ownership & First Time Buyer Rates (FTB)

  // This rate can be used for after Sept 22
  val leaseholdResidentialJuly21FTBSharedLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(250000), rate = 0),
      Slice(from = 250000, to = None,         rate = 0)
    )
  )

  val leaseholdResidentialNov17FTBSharedLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(125000), rate = 0),
      Slice(from = 125000, to = None,         rate = 0)
    )
  )

  val leaseholdMixedAfterMarch172016: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )

  val leaseHoldMixedNonResidentialReliefFrom15PercentAfterApril2013AndBeforeMarch2016: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0, to = Some(150000), rate = 0),
      Slice(from = 150000, to = None, rate = 1)
    )
  )

  val leaseholdMixedNonResBeforeMar08RentRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(150000), rate = 0),
      Slice(from = 150000, to = None,         rate = 1)
    )
  )

  val leaseholdMixedNonResApr2013toMar2016Above1kRentNPVRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )

  val leaseholdMixedNonResMar2008toMar2016Below1kRentNPVRates = SliceTable(
    slices = Seq(
      Slice(from = 0,       to = Some(150000),  rate = 0),
      Slice(from = 150000,  to = None,          rate = 1)
    )
  )
  val leaseholdMixedOrNonResidentialMar16OnwardsLeaseRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,      to = Some(150000), rate = 0),
      Slice(from = 150000, to = Some(500000), rate = 1),
      Slice(from = 500000, to = None,         rate = 2)
    )
  )
  val leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016NPVRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(150000),   rate = 0),
      Slice(from = 150000,   to = Some(5000000),  rate = 1),
      Slice(from = 5000000,  to = None,           rate = 2)
    )
  )

  val leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016PremiumRates: SliceTable = SliceTable(
    slices = Seq(
      Slice(from = 0,        to = Some(150000),   rate = 0),
      Slice(from = 150000,   to = Some(250000),   rate = 2),
      Slice(from = 250000,   to = None,           rate = 5),
    )
  )

}
