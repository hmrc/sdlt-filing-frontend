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
import utils.SelfAssessedHelper.{isBetweenDates, isResidentialBeforeMarch2012Date}

import java.time.LocalDate

class SelfAssessedHelperSpec extends SpecBase {

  private val testDateRange = DateRange(LocalDate.of(2010, 3, 25), LocalDate.of(2012, 3, 25))

  private def answersWith(effectiveDate: String, propertyType: String = "01"): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "STORN",
      returnResourceRef = "REF",
      returnInfo        = Some(ReturnInfo(mainLandID = Some("L1"))),
      transaction       = Some(Transaction(effectiveDate = Some(effectiveDate))),
      land              = Some(Seq(Land(landID = Some("L1"), propertyType = Some(propertyType))))
    )))

  ".isResidentialBeforeMarch2012Date" - {

    "must return true when date is before 22 March 2012 and main land is residential ('01')" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "01")) mustBe true
    }

    "must return true when date is before 22 March 2012 and main land is additional residential ('04')" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "04")) mustBe true
    }

    "must return true on the day before the boundary (2012-03-21) for a residential property" in {
      isResidentialBeforeMarch2012Date(answersWith("2012-03-21", "01")) mustBe true
    }

    "must return false on the boundary day (2012-03-22) for a residential property" in {
      isResidentialBeforeMarch2012Date(answersWith("2012-03-22", "01")) mustBe false
    }

    "must return false when date is after 22 March 2012 even for a residential property" in {
      isResidentialBeforeMarch2012Date(answersWith("2020-01-01", "01")) mustBe false
    }

    "must return false when property is non-residential ('02') even with a pre-2012 date" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "02")) mustBe false
    }

    "must return false when property is mixed ('03') even with a pre-2012 date" in {
      isResidentialBeforeMarch2012Date(answersWith("2010-06-15", "03")) mustBe false
    }

    "must accept dates in dd/MM/yyyy format" in {
      isResidentialBeforeMarch2012Date(answersWith("15/06/2010", "01")) mustBe true
    }

    "must accept dates in yyyy/MM/dd format" in {
      isResidentialBeforeMarch2012Date(answersWith("2010/06/15", "01")) mustBe true
    }

    "must return false when the effective date is unparseable" in {
      isResidentialBeforeMarch2012Date(answersWith("not-a-date", "01")) mustBe false
    }

    "must return false when fullReturn is absent" in {
      isResidentialBeforeMarch2012Date(emptyUserAnswers) mustBe false
    }

    "must return false when transaction is absent" in {
      val answers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "STORN", returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"))))
      )))
      isResidentialBeforeMarch2012Date(answers) mustBe false
    }

    "must return false when effectiveDate is absent" in {
      val answers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "STORN", returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(effectiveDate = None)),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"))))
      )))
      isResidentialBeforeMarch2012Date(answers) mustBe false
    }

    "must return false when mainLandID does not match any land entry" in {
      val answers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "STORN", returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("OTHER"))),
        transaction = Some(Transaction(effectiveDate = Some("2010-06-15"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"))))
      )))
      isResidentialBeforeMarch2012Date(answers) mustBe false
    }

    "must return false when only a secondary land is residential and main land is not" in {
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

  ".isBetweenDates" - {

    "must return true when the effective date is between the date parameters" in {
      isBetweenDates(answersWith("2011-01-01"), testDateRange) mustBe true
    }

    "must return true when the effective date is on the start date" in {
      isBetweenDates(answersWith("2010-03-25"), testDateRange) mustBe true
    }

    "must return true when the effective date is a day before the end date" in {
      isBetweenDates(answersWith("2012-03-24"), testDateRange) mustBe true
    }

    "must return false when the effective date is outside the date range" in {
      isBetweenDates(answersWith("2016-03-24"), testDateRange) mustBe false
    }

    "must return false when the effective date is a day before the start date" in {
      isBetweenDates(answersWith("2010-03-24"), testDateRange) mustBe false
    }

    "must return false when the effective date is the same as the end date" in {
      isBetweenDates(answersWith("2012-03-25"), testDateRange) mustBe false
    }
  }
}
