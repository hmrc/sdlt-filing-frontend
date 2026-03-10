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

package utils

import base.SpecBase
import models.Land

class LinkedListSortServiceSpec extends SpecBase {
  
  val service = new LinkedListSortService()
  
  private def createLand(landId: String, nextLandId: Option[String] = None): Land = {
    Land(
      landID = Some(landId),
      returnID = Some("RET123456789"),
      propertyType = Some("01"),
      interestCreatedTransferred = Some("FG"),
      houseNumber = Some("123"),
      address1 = Some("Baker Street"),
      address2 = Some("Marylebone"),
      address3 = Some("London"),
      address4 = None,
      postcode = Some("NW1 6XE"),
      landArea = Some("250.5"),
      areaUnit = Some("SQMETRE"),
      localAuthorityNumber = Some("5900"),
      mineralRights = Some("NO"),
      NLPGUPRN = Some("10012345678"),
      willSendPlanByPost = Some("NO"),
      titleNumber = Some("TGL12456"),
      landResourceRef = Some("LDN-REF-001"),
      nextLandID = nextLandId,
      DARPostcode = Some("NW1 6XE")
    )
  }

  "LinkedListSort" - {
    ".sorter" - {
      "when the object is Land" - {
        "must sort a list with one item" in {
          
          val land1 = createLand(landId = "LDN001", nextLandId = None)
          val mainland = land1
          val seqOfLands = Seq(land1)
          
          val sortedLandList = List(land1)
          
          service.sorter[Land](list = seqOfLands, mainObject = mainland)(_.landID.get, _.nextLandID) mustBe sortedLandList
        }

        "must sort a list with multiple items" in {

          val land1 = createLand(landId = "LDN001", nextLandId = Some("LDN002"))
          val land2 = createLand(landId = "LDN002", nextLandId = Some("LDN003"))
          val land3 = createLand(landId = "LDN003", nextLandId = Some("LDN004"))
          val land4 = createLand(landId = "LDN004", nextLandId = Some("LDN005"))
          val land5 = createLand(landId = "LDN005", nextLandId = None)
          val mainland = land1
          
          val seqOfLands = Seq(land4, land2, land1, land5, land3)

          val sortedLandList = List(land1, land2, land3, land4, land5)

          service.sorter[Land](list = seqOfLands, mainObject = mainland)(_.landID.get, _.nextLandID) mustBe sortedLandList
        }
      }
    }
  }
  
}
