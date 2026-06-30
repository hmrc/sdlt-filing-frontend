/*
 * Copyright 2025 HM Revenue & Customs
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
import models.{CompanyDetails, FullReturn, Land, Lease, Purchaser, ReturnAgent, ReturnInfo, Submission, TaxCalculation, Transaction, Vendor}
import org.scalatest.matchers.must.Matchers
import utils.PdfHelper.hasSdlt4Answers

class PdfHelperSpec extends SpecBase with Matchers {

  private val baseReturn: FullReturn = FullReturn(
    stornId = "STORN001",
    returnResourceRef = "RRF-001"
  )

  private val fullReturn = FullReturn(
    stornId = "STORN999",
    returnResourceRef = "RRF-999",
    returnInfo = Some(ReturnInfo(
      returnID = Some("RET999"),
      landCertForEachProp = Some("YES"),
      mainLandID = Some("LND001")
    )),
    transaction = Some(Transaction(
      includesStock = Some("no"),
      includesGoodwill = Some("no"),
      includesOther = Some("no"),
      includesChattel = Some("no"),
      isDependantOnFutureEvent = Some("no"),
      agreedToDeferPayment = Some("no")
    )),
    companyDetails = Some(CompanyDetails(
      companyTypeBank = Some("no"),
      companyTypeBuilder = Some("no"),
      companyTypeBuildsoc = Some("no"),
      companyTypeCentgov = Some("no"),
      companyTypeIndividual = Some("no"),
      companyTypeInsurance = Some("no"),
      companyTypeLocalauth = Some("no"),
      companyTypeOthercharity = Some("no"),
      companyTypeOthercompany = Some("no"),
      companyTypeOtherfinancial = Some("no"),
      companyTypePartnership = Some("no"),
      companyTypeProperty = Some("no"),
      companyTypePubliccorp = Some("no"),
      companyTypeSoletrader = Some("no"),
      companyTypePensionfund = Some("no")
    ))
  )

  ".hasSdlt4Answers" - {

    "must return false for an empty return" in {
      hasSdlt4Answers(baseReturn, "01") mustBe false
    }

    "must return false for an full return with no matches" in {
      hasSdlt4Answers(fullReturn, "01") mustBe false
    }

    "must return true when registrationNumber and placeOfRegistration are defined" in {
      val r = baseReturn.copy(
        purchaser = Some(Seq(Purchaser(
          registrationNumber = Some("1234"),
          placeOfRegistration = Some("Germany")
        )))
      )
      hasSdlt4Answers(r, "01") mustBe true
    }

    "must return true when property type is mixed or non-res && has sale of business" in {
      val transactions = Seq(
        Transaction(includesStock = Some("YES")),
        Transaction(includesGoodwill = Some("Yes")),
        Transaction(includesOther = Some("yes")),
        Transaction(includesChattel = Some("Yes"))
      )

      transactions.foreach { transaction =>
        val r = baseReturn.copy(transaction = Some(transaction))

        Seq("02", "03").foreach { pType =>
          hasSdlt4Answers(r, pType) mustBe true
        }
      }
    }

    "must return false when property type is residential or residential add prop && has sale of business" in {
      val r = baseReturn.copy(
        transaction = Some(Transaction(
          includesStock = Some("Yes")
        ))
      )

      Seq("01", "04").foreach { pType =>
        hasSdlt4Answers(r, pType) mustBe false
      }
    }

    "must return true when depends on future event or agreed to defer are yes" in {
      val transactions = Seq(
        Transaction(isDependantOnFutureEvent = Some("YES")),
        Transaction(agreedToDeferPayment = Some("Yes"))
      )

      transactions.foreach { transaction =>
        val r = baseReturn.copy(transaction = Some(transaction))
          hasSdlt4Answers(r, "01") mustBe true
      }
    }

    "must return true when return has company details" in {
      val companyDetails = Seq(
       CompanyDetails(companyTypeBank = Some("Yes")),
       CompanyDetails(companyTypeBuilder = Some("Yes")),
       CompanyDetails(companyTypeBuildsoc = Some("Yes")),
       CompanyDetails(companyTypeCentgov = Some("Yes")),
       CompanyDetails(companyTypeIndividual = Some("Yes")),
       CompanyDetails(companyTypeInsurance = Some("Yes")),
       CompanyDetails(companyTypeLocalauth = Some("Yes")),
       CompanyDetails(companyTypeOthercharity = Some("Yes")),
       CompanyDetails(companyTypeOthercompany = Some("Yes")),
       CompanyDetails(companyTypeOtherfinancial = Some("Yes")),
       CompanyDetails(companyTypePartnership = Some("Yes")),
       CompanyDetails(companyTypeProperty = Some("Yes")),
       CompanyDetails(companyTypePubliccorp = Some("Yes")),
       CompanyDetails(companyTypeSoletrader = Some("Yes")),
       CompanyDetails(companyTypePensionfund = Some("Yes")),
      )

      companyDetails.foreach { companyType =>
        val r = baseReturn.copy(companyDetails = Some(companyType))
        hasSdlt4Answers(r, "01") mustBe true
      }
    }

  }
}
