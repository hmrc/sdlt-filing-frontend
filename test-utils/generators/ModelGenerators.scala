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
import models.land.*
import models.prelimQuestions.TransactionType
import models.purchaser.*
import models.purchaserAgent.*
import models.vendor.whoIsTheVendor
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  
  implicit lazy val arbitraryLandTypeOfProperty: Arbitrary[LandTypeOfProperty] =
    Arbitrary {
      Gen.oneOf(LandTypeOfProperty.values.toSeq)
    }

  implicit lazy val arbitraryLandInterestTransferredOrCreated: Arbitrary[LandInterestTransferredOrCreated] =
    Arbitrary {
      Gen.oneOf(LandInterestTransferredOrCreated.values.toSeq)
    }

  implicit lazy val arbitraryPurchaserAgentAuthorised: Arbitrary[PurchaserAgentAuthorised] =
    Arbitrary {
      Gen.oneOf(PurchaserAgentAuthorised.values.toSeq)
    }
  
  implicit lazy val arbitraryPurchaserAgentsContactDetails: Arbitrary[PurchaserAgentsContactDetails] =
    Arbitrary {
      for {
        phone <- arbitrary[Option[String]]
        email <- arbitrary[Option[String]]
        valid <- if (phone.isEmpty && email.isEmpty) {
          Gen.oneOf(
            arbitrary[String].map(s => PurchaserAgentsContactDetails(Some(s), None)),
            arbitrary[String].map(s => PurchaserAgentsContactDetails(None, Some(s)))
          )
        }
        else {
          Gen.const(PurchaserAgentsContactDetails(phone, email))
        }
      } yield valid
    }

  implicit lazy val arbitraryPurchaserRemove: Arbitrary[PurchaserRemove] =
    Arbitrary {
      Gen.oneOf(PurchaserRemove.values.toSeq)
    }

  implicit lazy val arbitrarySelectPurchaserAgent: Arbitrary[SelectPurchaserAgent] =
    Arbitrary {
      Gen.oneOf(SelectPurchaserAgent.values.toSeq)
    }

  implicit lazy val arbitraryNoReturnReference: Arbitrary[NoReturnReference] =
    Arbitrary {
      Gen.oneOf(NoReturnReference.values.toSeq)
    }

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
