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
import models.{FullReturn, Land, ReturnInfo, Transaction, UserAnswers}
import utils.SelfAssessedHelper.isResidentialBeforeMarch2012Date

class SelfAssessedHelperSpec extends SpecBase {

  private def answersWith(effectiveDate: Option[String], propertyType: Option[String]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "STORN",
      returnResourceRef = "REF",
      returnInfo        = Some(ReturnInfo(mainLandID = Some("L1"))),
      transaction       = Some(Transaction(effectiveDate = effectiveDate)),
      land              = Some(Seq(Land(landID = Some("L1"), propertyType = propertyType)))
    )))

  private def answersWith(effectiveDate: String, propertyType: String): UserAnswers =
    answersWith(Some(effectiveDate), Some(propertyType))

  ".isResidentialBeforeMarch2012Date" - {

    "is true for a residential ('01') property before 22/03/2012" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "01")) mustBe true
    }

    "is true for additional residential ('04') before 22/03/2012" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "04")) mustBe true
    }

    "is true the day before the boundary (2012-03-21)" in {
      isResidentialBeforeMarch2012Date(answersWith("2012-03-21", "01")) mustBe true
    }

    "is false on the boundary (2012-03-22)" in {
      isResidentialBeforeMarch2012Date(answersWith("2012-03-22", "01")) mustBe false
    }

    "is false after the boundary" in {
      isResidentialBeforeMarch2012Date(answersWith("2020-01-01", "01")) mustBe false
    }

    "is false for non-residential property types" in {
      Seq("02", "03").foreach { pt =>
        isResidentialBeforeMarch2012Date(answersWith("2010-06-15", pt)) mustBe false
      }
    }

    "accepts dd/MM/yyyy and yyyy/MM/dd date formats" in {
      isResidentialBeforeMarch2012Date(answersWith("15/06/2010", "01")) mustBe true
      isResidentialBeforeMarch2012Date(answersWith("2010/06/15", "01")) mustBe true
    }

    "is false when the effective date cannot be parsed" in {
      isResidentialBeforeMarch2012Date(answersWith("not-a-date", "01")) mustBe false
    }

    "is false when fullReturn, transaction, or effective date is missing" in {
      isResidentialBeforeMarch2012Date(emptyUserAnswers) mustBe false
      isResidentialBeforeMarch2012Date(answersWith(None, Some("01"))) mustBe false
    }

    "is false when mainLandID does not match any land entry" in {
      val answers = answersWith("2010-06-15", "01").copy(fullReturn =
        answersWith("2010-06-15", "01").fullReturn.map(_.copy(returnInfo = Some(ReturnInfo(mainLandID = Some("OTHER")))))
      )
      isResidentialBeforeMarch2012Date(answers) mustBe false
    }

    "is false when only a secondary land is residential" in {
      val answers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "STORN", returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(effectiveDate = Some("2010-06-15"))),
        land = Some(Seq(
          Land(landID = Some("L1"), propertyType = Some("02")),
          Land(landID = Some("L2"), propertyType = Some("01"))
        ))
      )))
      isResidentialBeforeMarch2012Date(answers) mustBe false
    }
  }
}
