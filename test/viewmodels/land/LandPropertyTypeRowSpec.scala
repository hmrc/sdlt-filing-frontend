/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.land

import base.SpecBase

class LandPropertyTypeRowSpec extends SpecBase {

  private val row = LandPropertyTypeRow(
    landId              = "LND001",
    label               = "Baker Street, NW1 6XE",
    propertyTypeDisplay = "01 - Residential",
    updateUrl           = "/update/LND001",
    removeUrl           = "/remove/LND001",
    canRemove           = true,
    isMismatch          = false
  )

  "LandPropertyTypeRow" - {

    "must expose every field through its accessor" in {
      row.landId              mustBe "LND001"
      row.label               mustBe "Baker Street, NW1 6XE"
      row.propertyTypeDisplay mustBe "01 - Residential"
      row.updateUrl           mustBe "/update/LND001"
      row.removeUrl           mustBe "/remove/LND001"
      row.canRemove           mustBe true
      row.isMismatch          mustBe false
    }

    "must be equal to another instance with the same field values" in {
      val other = LandPropertyTypeRow(
        landId              = "LND001",
        label               = "Baker Street, NW1 6XE",
        propertyTypeDisplay = "01 - Residential",
        updateUrl           = "/update/LND001",
        removeUrl           = "/remove/LND001",
        canRemove           = true,
        isMismatch          = false
      )

      row mustBe other
      row.hashCode() mustBe other.hashCode()
    }

    "must not be equal when any field differs" in {
      row must not be row.copy(landId              = "LND002")
      row must not be row.copy(label               = "Cardiff Castle")
      row must not be row.copy(propertyTypeDisplay = "02 - Mixed")
      row must not be row.copy(updateUrl           = "/somewhere/else")
      row must not be row.copy(removeUrl           = "/somewhere/else")
      row must not be row.copy(canRemove           = false)
      row must not be row.copy(isMismatch          = true)
    }

    "must allow targeted updates via copy" in {
      val flagged = row.copy(isMismatch = true)

      flagged.isMismatch mustBe true
      flagged.landId     mustBe row.landId
      flagged.label      mustBe row.label
    }

    "must distinguish mismatch state from canRemove state" in {
      val mismatchOnly = row.copy(isMismatch = true, canRemove = false)

      mismatchOnly.isMismatch mustBe true
      mismatchOnly.canRemove  mustBe false
    }
  }
}