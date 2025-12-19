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

package generators

import models.*
import models.prelimQuestions.TransactionType
import models.purchaser.{CompanyFormOfId, DoesPurchaserHaveNI, PurchaserConfirmIdentity, WhoIsMakingThePurchase, PurchaserTypeOfCompany, PurchaserAndVendorConnected, IsPurchaserActingAsTrustee}
import models.vendor.whoIsTheVendor
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryCompanyFormOfId: Arbitrary[CompanyFormOfId] =
    Arbitrary {
      for {
        referenceId <- arbitrary[String]
        countryIssued <- arbitrary[String]
      } yield CompanyFormOfId(referenceId, countryIssued)
    }

  implicit lazy val arbitraryPurchaserAndVendorConnected: Arbitrary[PurchaserAndVendorConnected] =
    Arbitrary {
      Gen.oneOf(PurchaserAndVendorConnected.values.toSeq)
    }

  implicit lazy val arbitraryIsPurchaserActingAsTrustee: Arbitrary[IsPurchaserActingAsTrustee] =
    Arbitrary {
      Gen.oneOf(IsPurchaserActingAsTrustee.values.toSeq)
    }

  implicit lazy val arbitraryPurchaserConfirmIdentity: Arbitrary[PurchaserConfirmIdentity] =
    Arbitrary {
      Gen.oneOf(PurchaserConfirmIdentity.values.toSeq)
    }

  implicit lazy val arbitraryDoesPurchaserHaveNI: Arbitrary[DoesPurchaserHaveNI] =
    Arbitrary {
      Gen.oneOf(DoesPurchaserHaveNI.values.toSeq)
    }

  implicit lazy val arbitraryPurchaserTypeOfCompany: Arbitrary[PurchaserTypeOfCompany] =
     Arbitrary {
       Gen.oneOf(PurchaserTypeOfCompany.values.toSeq)
     }

  implicit lazy val arbitraryWhoIsMakingThePurchase: Arbitrary[WhoIsMakingThePurchase] =
    Arbitrary {
      Gen.oneOf(WhoIsMakingThePurchase.values.toSeq)
    }

  implicit lazy val arbitrarywhoIsTheVendor: Arbitrary[whoIsTheVendor] =
    Arbitrary {
      Gen.oneOf(whoIsTheVendor.values.toSeq)
    }

  implicit lazy val arbitraryTransactionType: Arbitrary[TransactionType] =
    Arbitrary {
      Gen.oneOf(TransactionType.values.toSeq)
    }

}
