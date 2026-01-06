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

package services.purchaser

import base.SpecBase
import constants.FullReturnConstants
import models.*
import models.purchaser.*
import pages.purchaser.{ConfirmNameOfThePurchaserPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers.*

import scala.concurrent.Future
import scala.util.Success

class PurchaserServiceSpec extends SpecBase {

  private val service = new PurchaserService()

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None
  )

  private def userAnswersWithIndividualPurchaser: UserAnswers =
    emptyUserAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

  private def userAnswersWithCompanyPurchaser: UserAnswers =
    emptyUserAnswers.set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

  val continueRoute: Result = Ok("Continue route")

  "PurchaserService" - {

    "confirmIdentityNextPage" - {

      "in CheckMode" - {

        "must return PurchaserBeforeYouStart for any identity type" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.PartnershipUTR, CheckMode)

          result mustEqual controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad()
        }
      }

      "in NormalMode" - {

        "must return NameOfPurchaser for PartnershipUTR" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.PartnershipUTR, NormalMode)

          result mustEqual controllers.purchaser.routes.PurchaserPartnershipUtrController.onPageLoad(NormalMode)
        }

        "must return NameOfPurchaser for CorporationTaxUTR" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.CorporationTaxUTR, NormalMode)

          result mustEqual controllers.purchaser.routes.PurchaserCorporationTaxUTRController.onPageLoad(NormalMode)
        }

        "must return NameOfPurchaser for VatRegistrationNumber" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.VatRegistrationNumber, NormalMode)

          result mustEqual controllers.purchaser.routes.RegistrationNumberController.onPageLoad(NormalMode)
        }

        "must return NameOfPurchaser for AnotherFormOfID" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.AnotherFormOfID, NormalMode)

          result mustEqual controllers.purchaser.routes.CompanyFormOfIdController.onPageLoad(NormalMode)
        }

        "must return ReturnTaskList for Divider" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.Divider, NormalMode)

          result mustEqual controllers.routes.ReturnTaskListController.onPageLoad()
        }
      }
    }

    "whoIsMakingThePurchase" - {

      "must return Company when isCompany is 'yes'" in {
        val result = service.whoIsMakingThePurchase(Some("yes"))

        result mustEqual WhoIsMakingThePurchase.Company
      }

      "must return Company when isCompany is 'YES'" in {
        val result = service.whoIsMakingThePurchase(Some("YES"))

        result mustEqual WhoIsMakingThePurchase.Company
      }

      "must return Individual when isCompany is 'no'" in {
        val result = service.whoIsMakingThePurchase(Some("no"))

        result mustEqual WhoIsMakingThePurchase.Individual
      }

      "must return Individual when isCompany is 'NO'" in {
        val result = service.whoIsMakingThePurchase(Some("NO"))

        result mustEqual WhoIsMakingThePurchase.Individual
      }

      "must return Company when isCompany is None" in {
        val result = service.whoIsMakingThePurchase(None)

        result mustEqual WhoIsMakingThePurchase.Company
      }

      "must return Company when isCompany is any other value" in {
        val result = service.whoIsMakingThePurchase(Some("maybe"))

        result mustEqual WhoIsMakingThePurchase.Company
      }
    }

    "populatePurchaserNameInSession" - {

      "when purchaserCheck is 'Yes'" - {

        "must successfully populate session with company purchaser" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH001"),
            forename1 = None,
            forename2 = None,
            surname = None,
            companyName = Some("ACME Corporation"),
            isCompany = Some("YES"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "ACME Corporation"
          ))

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must successfully populate session with individual purchaser with full name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH002"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          ))

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must successfully populate session with individual purchaser without middle name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH003"),
            forename1 = Some("Jane"),
            forename2 = None,
            surname = Some("Doe"),
            companyName = None,
            isCompany = Some("no"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("Jane"),
            forename2 = None,
            name = "Doe"
          ))

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must handle purchaser with only surname (no forenames)" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH004"),
            forename1 = None,
            forename2 = None,
            surname = Some("Madonna"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Madonna"
          ))

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Individual)
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must prioritize company name over surname when both are present" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH005"),
            forename1 = Some("John"),
            forename2 = None,
            surname = Some("Smith"),
            companyName = Some("Smith & Co Ltd"),
            isCompany = Some("YES"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Smith & Co Ltd"
          ))

          updatedAnswers.get(WhoIsMakingThePurchasePage) mustBe Some(WhoIsMakingThePurchase.Company)
        }

        "must use first purchaser when multiple purchasers exist" in {
          val firstPurchaser = Purchaser(
            purchaserID = Some("PURCH006"),
            forename1 = Some("First"),
            forename2 = None,
            surname = Some("Purchaser"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val secondPurchaser = Purchaser(
            purchaserID = Some("PURCH007"),
            forename1 = Some("Second"),
            forename2 = None,
            surname = Some("Buyer"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(firstPurchaser, secondPurchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("First"),
            forename2 = None,
            name = "Purchaser"
          ))
        }

        "must only set confirmation when purchaser has no surname" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH008"),
            forename1 = Some("John"),
            forename2 = Some("Middle"),
            surname = None,
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must only set confirmation when purchaser has no purchaserID" in {
          val purchaser = Purchaser(
            purchaserID = None,
            forename1 = Some("John"),
            forename2 = None,
            surname = Some("Smith"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must only set confirmation when IsCompany is None" in {
          val purchaser = Purchaser(
            purchaserID = None,
            forename1 = Some("John"),
            forename2 = None,
            surname = Some("Smith"),
            companyName = None,
            isCompany = None,
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must only set confirmation when no purchaser exists in fullReturn" in {
          val fullReturn = emptyFullReturn.copy(purchaser = None)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must only set confirmation when purchaser list is empty" in {
          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq.empty))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must only set confirmation when fullReturn is None" in {
          val userAnswers = emptyUserAnswers

          val result = service.populatePurchaserNameInSession("yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }
      }

      "when purchaserCheck is 'No'" - {

        "must only set confirmation to No without setting name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH009"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("no", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }

        "must only set confirmation to No when no purchaser exists" in {
          val userAnswers = emptyUserAnswers

          val result = service.populatePurchaserNameInSession("no", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }
      }

      "when purchaserCheck is any other value" - {

        "must set confirmation to No" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH010"),
            forename1 = Some("John"),
            forename2 = None,
            surname = Some("Smith"),
            companyName = None,
            isCompany = Some("NO"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("Invalid", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }
      }
    }

    "checkPurchaserTypeAndCompanyDetails" - {

      "when the purchaser type is set to Company" - {

        "when the value in user answers is Company" - {

          "when no purchaser exists in fullReturn" - {
            "must stay on the page" in {
              val userAnswersWithoutPurchaser = userAnswersWithCompanyPurchaser
                .copy(fullReturn = Some(FullReturn(
                  stornId = "test",
                  returnResourceRef = "testref",
                  purchaser = None
                )))

              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithoutPurchaser,
                continueRoute
              )
              result mustBe continueRoute
            }
          }

          "when purchaser list is empty" - {
            "must stay on the page" in {
              val userAnswersWithEmptyPurchasers = userAnswersWithCompanyPurchaser
                .copy(fullReturn = Some(FullReturn(
                  stornId = "test",
                  returnResourceRef = "testref",
                  purchaser = Some(Seq.empty)
                )))

              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithEmptyPurchasers,
                continueRoute
              )
              result mustBe continueRoute
            }
          }

          "when there is one purchaser and companyTypePensionfund is not present" - {
            "must stay on the page" in {
              val singlePurchaser = Purchaser(
                purchaserID = Some("PURCH001"),
                isCompany = Some("YES"),
                companyName = Some("Test Company")
              )

              val userAnswersWithOnePurchaser = userAnswersWithCompanyPurchaser
                .copy(fullReturn = Some(FullReturn(
                  stornId = "test",
                  returnResourceRef = "testref",
                  purchaser = Some(Seq(singlePurchaser)),
                  companyDetails = None
                )))

              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithOnePurchaser,
                continueRoute
              )
              result mustBe continueRoute
            }
          }

          "when there is one purchaser and companyTypePensionfund is present" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val singlePurchaser = Purchaser(
                purchaserID = Some("PURCH001"),
                isCompany = Some("YES"),
                companyName = Some("Test Company")
              )

              val userAnswersWithPensionFund = userAnswersWithCompanyPurchaser
                .copy(fullReturn = Some(FullReturn(
                  stornId = "test",
                  returnResourceRef = "testref",
                  purchaser = Some(Seq(singlePurchaser)),
                  companyDetails = Some(CompanyDetails(
                    companyTypePensionfund = Some("YES")
                  ))
                )))

              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithPensionFund,
                continueRoute
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
              )
            }
          }

          "when there are multiple purchasers" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val firstPurchaser = Purchaser(
                purchaserID = Some("PURCH001"),
                isCompany = Some("YES"),
                companyName = Some("First Company")
              )

              val secondPurchaser = Purchaser(
                purchaserID = Some("PURCH002"),
                isCompany = Some("YES"),
                companyName = Some("Second Company")
              )

              val userAnswersWithMultiplePurchasers = userAnswersWithCompanyPurchaser
                .copy(fullReturn = Some(FullReturn(
                  stornId = "test",
                  returnResourceRef = "testref",
                  purchaser = Some(Seq(firstPurchaser, secondPurchaser))
                )))

              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithMultiplePurchasers,
                continueRoute
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
              )
            }
          }

          "when fullReturn is not present" - {
            "must stay on the page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                userAnswersWithCompanyPurchaser,
                continueRoute
              )
              result mustBe continueRoute
            }
          }
        }

        "when the value in user answers is Individual" - {
          "must redirect to Generic Error Page" in {
            val result = service.checkPurchaserTypeAndCompanyDetails(
              WhoIsMakingThePurchase.Company,
              userAnswersWithIndividualPurchaser,
              continueRoute
            )
            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.routes.GenericErrorController.onPageLoad().url
            )
          }
        }

        "when the value in user answers is not present" - {
          "must redirect to WhoIsMakingThePurchase page" in {
            val result = service.checkPurchaserTypeAndCompanyDetails(
              WhoIsMakingThePurchase.Company,
              emptyUserAnswers,
              continueRoute
            )
            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
            )
          }
        }
      }

      "when the purchaser type is set to Individual" - {

        "when the value in user answers is Individual" - {
          "must stay on the page" in {
            val result = service.checkPurchaserTypeAndCompanyDetails(
              WhoIsMakingThePurchase.Individual,
              userAnswersWithIndividualPurchaser,
              continueRoute
            )
            result mustBe continueRoute
          }
        }

        "when the value in user answers is Company" - {
          "must redirect to Generic Error Page" in {
            val result = service.checkPurchaserTypeAndCompanyDetails(
              WhoIsMakingThePurchase.Individual,
              userAnswersWithCompanyPurchaser,
              continueRoute
            )
            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.routes.GenericErrorController.onPageLoad().url
            )
          }
        }

        "when the value in user answers is not present" - {
          "must redirect to WhoIsMakingThePurchase page" in {
            val result = service.checkPurchaserTypeAndCompanyDetails(
              WhoIsMakingThePurchase.Individual,
              emptyUserAnswers,
              continueRoute
            )
            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
            )
          }
        }
      }
    }
    
    "continueIfAddingMainPurchaser" - {
      val incompletePurchaser = Purchaser(
        purchaserID = Some("PURCH001"),
        isCompany = Some("YES"),
        companyName = Some("Test Company")
      )
      
      "must continue when mainPurchaserID does not exist and adding the main purchaser" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn))
        val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute)
        result mustBe continueRoute
      }
      
      "must continue when main purchaser is incomplete and is being edited" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH001"))))
          ))
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.Yes).success.value
        val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute)
        result mustBe continueRoute
      }
      
      "must redirect to CYA when main purchaser is incomplete and is not being edited" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
            emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH002"))))
          ))
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
        val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute)
        redirectLocation(Future.successful(result)) mustBe Some(
          controllers.routes.GenericErrorController.onPageLoad().url
        )
      }

      "must redirect to CYA when main purchaser is complete" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturnConstants.completeFullReturn))
        val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute)
        redirectLocation(Future.successful(result)) mustBe Some(
          controllers.routes.GenericErrorController.onPageLoad().url
        )
      }
    }

    "continueIfAddingMainPurchaserWithPurchaserTypeCheck" - {

      "must continue when purchaser type and main purchaser conditions are met" in {
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
        val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
          WhoIsMakingThePurchase.Company,
          userAnswers,
          continueRoute
        )
        result mustBe continueRoute
      }

      "must redirect to CYA page when purchaser type does not match" in {
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
        val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
          WhoIsMakingThePurchase.Company,
          userAnswers,
          continueRoute
        )
        redirectLocation(Future.successful(result)) mustBe Some(
          controllers.routes.GenericErrorController.onPageLoad().url
        )
      }

      "must redirect to CYA page when main purchaser conditions are not met" in {
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
        val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
          WhoIsMakingThePurchase.Individual,
          userAnswers,
          continueRoute
        )
        redirectLocation(Future.successful(result)) mustBe Some(
          controllers.routes.GenericErrorController.onPageLoad().url
        )
      }
    }
  }
}