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

class SortServiceSpec extends SpecBase {
  
  val service = new SortService()
  
  private def createLand(landId: Option[String], lastUpdatedDate: Option[String]): Land = {
    Land(
      landID = landId,
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
      nextLandID = Some("LND002"),
      DARPostcode = Some("NW1 6XE"),
      lastUpdateDate = lastUpdatedDate
    )
  }

  private def mainLandID: String = "LND001"

  "SortService" - {
    ".sortByLastUpdatedDate" - {
      "when the object is Land" - {
        "must sort a list with one item" in {
          
          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = Some("2025-11-09 19:53:34"))
          val seqOfLands = Seq(land1)
          
          val sortedLandList = Seq(land1)
          
          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, Some(mainLandID))(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must return the list as is when there is one item and lastUpdateDate is missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = None)
          val seqOfLands = Seq(land1)

          val sortedLandList = Seq(land1)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, Some(mainLandID))(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must return the list as is when there is one item and mainLandID is missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = Some("2025-11-09 19:53:34"))
          val seqOfLands = Seq(land1)

          val sortedLandList = Seq(land1)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, mainObjectId = None)(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must return the list as is when there is one item and both lastUpdateDate and mainLandID are missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = None)
          val seqOfLands = Seq(land1)

          val sortedLandList = Seq(land1)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, mainObjectId = None)(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must sort a list with multiple items" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = Some("2022-11-09 19:53:34"))
          val land2 = createLand(landId = Some("LND002"), lastUpdatedDate = Some("2023-11-09 19:53:34"))
          val land3 = createLand(landId = Some("LND003"), lastUpdatedDate = Some("2024-12-09 19:53:34"))
          val land4 = createLand(landId = Some("LND004"), lastUpdatedDate = Some("2025-11-09 19:53:34"))
          val land5 = createLand(landId = Some("LND005"), lastUpdatedDate = Some("2025-11-09 19:54:34"))

          val seqOfLands = Seq(land4, land2, land1, land5, land3)

          val sortedLandList = Seq(land1, land2, land3, land4, land5)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, Some(mainLandID))(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must sort a list with multiple items when some lastUpdateDate are missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = Some("2025-11-09 19:53:34"))
          val land2 = createLand(landId = Some("LND002"), lastUpdatedDate = None)
          val land3 = createLand(landId = Some("LND003"), lastUpdatedDate = None)
          val land4 = createLand(landId = Some("LND004"), lastUpdatedDate = Some("2024-11-09 19:53:34"))
          val land5 = createLand(landId = Some("LND005"), lastUpdatedDate = Some("2024-11-09 19:56:34"))

          val seqOfLands = Seq(land4, land2, land1, land5, land3)

          val sortedLandList = Seq(land1, land2, land3, land4, land5)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, Some(mainLandID))(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must sort a list with multiple items when mainLandID is missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = Some("2022-11-09 19:53:34"))
          val land2 = createLand(landId = Some("LND002"), lastUpdatedDate = Some("2023-11-09 19:53:34"))
          val land3 = createLand(landId = Some("LND003"), lastUpdatedDate = Some("2024-12-09 19:53:34"))
          val land4 = createLand(landId = Some("LND004"), lastUpdatedDate = Some("2025-11-09 19:53:34"))
          val land5 = createLand(landId = Some("LND005"), lastUpdatedDate = Some("2025-11-09 19:54:34"))

          val seqOfLands = Seq(land4, land2, land1, land5, land3)

          val sortedLandList = Seq(land1, land2, land3, land4, land5)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, mainObjectId = None)(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }

        "must return the list with multiple items as is when all lastUpdateDate and mainLandID are missing" in {

          val land1 = createLand(landId = Some("LND001"), lastUpdatedDate = None)
          val land2 = createLand(landId = Some("LND002"), lastUpdatedDate = None)
          val land3 = createLand(landId = Some("LND003"), lastUpdatedDate = None)
          val land4 = createLand(landId = Some("LND004"), lastUpdatedDate = None)
          val land5 = createLand(landId = Some("LND005"), lastUpdatedDate = None)

          val seqOfLands = Seq(land4, land2, land1, land5, land3)

          val sortedLandList = Seq(land4, land2, land1, land5, land3)

          service.sortByMainObjectLastUpdateDate[Land](list = seqOfLands, mainObjectId = None)(_.lastUpdateDate, _.landID) mustBe sortedLandList
        }
      }
    }
  }
}
