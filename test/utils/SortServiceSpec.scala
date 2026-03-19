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

class SortServiceSpec extends SpecBase {
  
  val service = new SortService()

  case class TestObject(objectId: Option[String], lastUpdateDate: Option[String])

  private def mainObjectID: String = "LND001"

  "SortService" - {
    ".sortByLastUpdatedDate" - {
      "must sort a list with one item" in {
          
          val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = Some("2025-11-09 19:53:34"))
          val seqOfObjects= Seq(object1)
          
          val sortedObjectList = Seq(object1)
          
          service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, Some(mainObjectID))(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
        }

      "must return the list as is when there is one item and lastUpdateDate is missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = None)
        val seqOfObjects = Seq(object1)

        val sortedObjectList = Seq(object1)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, Some(mainObjectID))(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must return the list as is when there is one item and mainObjectID is missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = Some("2025-11-09 19:53:34"))
        val seqOfObjects = Seq(object1)

        val sortedObjectList = Seq(object1)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, mainObjectId = None)(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must return the list as is when there is one item and both lastUpdateDate and mainObjectID are missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = None)
        val seqOfObjects = Seq(object1)

        val sortedObjectList = Seq(object1)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, mainObjectId = None)(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must sort a list with multiple items" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = Some("2022-11-09 19:53:34"))
        val object2 = TestObject(objectId = Some("LND002"), lastUpdateDate = Some("2023-11-09 19:53:34"))
        val object3 = TestObject(objectId = Some("LND003"), lastUpdateDate = Some("2024-12-09 19:53:34"))
        val object4 = TestObject(objectId = Some("LND004"), lastUpdateDate = Some("2025-11-09 19:53:34"))
        val object5 = TestObject(objectId = Some("LND005"), lastUpdateDate = Some("2025-11-09 19:54:34"))

        val seqOfObjects = Seq(object4, object2, object1, object5, object3)

        val sortedObjectList = Seq(object1, object2, object3, object4, object5)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, Some(mainObjectID))(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must sort a list with multiple items when some lastUpdateDate are missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = Some("2022-11-09 19:53:34"))
        val object2 = TestObject(objectId = Some("LND002"), lastUpdateDate = None)
        val object3 = TestObject(objectId = Some("LND003"), lastUpdateDate = None)
        val object4 = TestObject(objectId = Some("LND004"), lastUpdateDate = Some("2025-11-09 19:53:34"))
        val object5 = TestObject(objectId = Some("LND005"), lastUpdateDate = Some("2025-11-09 19:54:34"))

        val seqOfObjects = Seq(object4, object2, object1, object5, object3)

        val sortedObjectList = Seq(object1, object2, object3, object4, object5)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, Some(mainObjectID))(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must sort a list with multiple items when mainObjectID is missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = Some("2022-11-09 19:53:34"))
        val object2 = TestObject(objectId = Some("LND002"), lastUpdateDate = Some("2023-11-09 19:53:34"))
        val object3 = TestObject(objectId = Some("LND003"), lastUpdateDate = Some("2024-12-09 19:53:34"))
        val object4 = TestObject(objectId = Some("LND004"), lastUpdateDate = Some("2025-11-09 19:53:34"))
        val object5 = TestObject(objectId = Some("LND005"), lastUpdateDate = Some("2025-11-09 19:54:34"))

        val seqOfObjects = Seq(object4, object2, object1, object5, object3)

        val sortedObjectList = Seq(object1, object2, object3, object4, object5)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, mainObjectId = None)(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }

      "must return the list with multiple items as is when all lastUpdateDate and mainObjectID are missing" in {

        val object1 = TestObject(objectId = Some("LND001"), lastUpdateDate = None)
        val object2 = TestObject(objectId = Some("LND002"), lastUpdateDate = None)
        val object3 = TestObject(objectId = Some("LND003"), lastUpdateDate = None)
        val object4 = TestObject(objectId = Some("LND004"), lastUpdateDate = None)
        val object5 = TestObject(objectId = Some("LND005"), lastUpdateDate = None)

        val seqOfObjects = Seq(object4, object2, object1, object5, object3)

        val sortedObjectList = Seq(object4, object2, object1, object5, object3)

        service.sortByMainObjectLastUpdateDate[TestObject](list = seqOfObjects, mainObjectId = None)(_.lastUpdateDate, _.objectId) mustBe sortedObjectList
      }
    }
  }
}
