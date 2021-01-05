/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.data

import calculation.models.calculationtables.{Slab, SlabTable}

object SlabRatesTables {

  val freeholdResidentialMar12toDec14Rates = SlabTable(
    slabs = Seq(
      Slab(threshold = 2000000, rate = 7),
      Slab(threshold = 1000000, rate = 5),
      Slab(threshold = 500000,  rate = 4),
      Slab(threshold = 250000,  rate = 3),
      Slab(threshold = 125000,  rate = 1)
    )
  )

  val freeholdNonResidentialMar12toMar16Rates = SlabTable(
    slabs = Seq(
      Slab(threshold = 500000,  rate = 4),
      Slab(threshold = 250000,  rate = 3),
      Slab(threshold = 150000,  rate = 1)
    )
  )

  val leaseholdResidentialMar12toDec14PremiumRates = SlabTable(
    slabs = Seq(
      Slab(threshold = 2000000, rate = 7),
      Slab(threshold = 1000000, rate = 5),
      Slab(threshold = 500000,  rate = 4),
      Slab(threshold = 250000,  rate = 3),
      Slab(threshold = 125000,  rate = 1)
    )
  )

  val leaseholdNonResidentialMar12toMar16PremiumRates = SlabTable(
    slabs = Seq(
      Slab(threshold = 500000, rate = 4),
      Slab(threshold = 250000, rate = 3),
      Slab(threshold = 0,      rate = 1)
    )
  )

}
