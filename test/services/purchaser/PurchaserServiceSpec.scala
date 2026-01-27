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
import org.scalatest.prop.TableDrivenPropertyChecks.*
import pages.purchaser.{AddPurchaserPhoneNumberPage, ConfirmNameOfThePurchaserPage, DoesPurchaserHaveNIPage, EnterPurchaserPhoneNumberPage, NameOfPurchaserPage, PurchaserConfirmIdentityPage, PurchaserDateOfBirthPage, PurchaserNationalInsurancePage, PurchaserTypeOfCompanyPage, PurchaserUTRPage, WhoIsMakingThePurchasePage}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers.*

import scala.concurrent.Future
import scala.util.Success
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.time.LocalDate


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

        "must return PurchaserPartnershipUtr page for type PartnershipUTR" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.PartnershipUTR, CheckMode)

          result mustEqual controllers.purchaser.routes.PurchaserPartnershipUtrController.onPageLoad(CheckMode)
        }

        "must return PurchaserCorporationTaxUTR page for type CorporationTaxUTR" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.CorporationTaxUTR, CheckMode)

          result mustEqual controllers.purchaser.routes.PurchaserCorporationTaxUTRController.onPageLoad(CheckMode)
        }

        "must return Check Your Answers page for type VatRegistrationNumber" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.VatRegistrationNumber, CheckMode)

          result mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
        }

        "must return Check Your Answers page for type AnotherFormOfID" in {
          val result = service.confirmIdentityNextPage(PurchaserConfirmIdentity.AnotherFormOfID, CheckMode)

          result mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()
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

      "in NormalMode" - {

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
                  continueRoute,
                  NormalMode
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
                  continueRoute,
                  NormalMode
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
                  continueRoute,
                  NormalMode
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
                  continueRoute,
                  NormalMode
                )
                redirectLocation(Future.successful(result)) mustBe Some(
                  controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
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
                  continueRoute,
                  NormalMode
                )
                redirectLocation(Future.successful(result)) mustBe Some(
                  controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
                )
              }
            }

            "when fullReturn is not present" - {
              "must stay on the page" in {
                val result = service.checkPurchaserTypeAndCompanyDetails(
                  WhoIsMakingThePurchase.Company,
                  userAnswersWithCompanyPurchaser,
                  continueRoute,
                  NormalMode
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
                continueRoute,
                NormalMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
              )
            }
          }

          "when the value in user answers is not present" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                emptyUserAnswers,
                continueRoute,
                NormalMode
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
                continueRoute,
                NormalMode
              )
              result mustBe continueRoute
            }
          }

          "when the value in user answers is Company" - {
            "must redirect to Generic Error Page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Individual,
                userAnswersWithCompanyPurchaser,
                continueRoute,
                NormalMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
              )
            }
          }

          "when the value in user answers is not present" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Individual,
                emptyUserAnswers,
                continueRoute,
                NormalMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
              )
            }
          }
        }
      }

      "in CheckMode" - {

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
                  continueRoute,
                  CheckMode
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
                  continueRoute,
                  CheckMode
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
                  continueRoute,
                  CheckMode
                )
                result mustBe continueRoute
              }
            }

            "when there is one purchaser and companyTypePensionfund is present" - {
              "must continue" in {
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
                  continueRoute,
                  CheckMode)

                result mustBe continueRoute
              }
            }

            "when there are multiple purchasers" - {
              "must continue" in {
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
                  continueRoute,
                  CheckMode
                )
                result mustBe continueRoute
              }
            }

            "when fullReturn is not present" - {
              "must stay on the page" in {
                val result = service.checkPurchaserTypeAndCompanyDetails(
                  WhoIsMakingThePurchase.Company,
                  userAnswersWithCompanyPurchaser,
                  continueRoute,
                  CheckMode
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
                continueRoute,
                CheckMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
              )
            }
          }

          "when the value in user answers is not present" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Company,
                emptyUserAnswers,
                continueRoute,
                CheckMode
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
                continueRoute,
                CheckMode
              )
              result mustBe continueRoute
            }
          }

          "when the value in user answers is Company" - {
            "must redirect to Generic Error Page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Individual,
                userAnswersWithCompanyPurchaser,
                continueRoute,
                CheckMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
              )
            }
          }

          "when the value in user answers is not present" - {
            "must redirect to WhoIsMakingThePurchase page" in {
              val result = service.checkPurchaserTypeAndCompanyDetails(
                WhoIsMakingThePurchase.Individual,
                emptyUserAnswers,
                continueRoute,
                CheckMode
              )
              redirectLocation(Future.successful(result)) mustBe Some(
                controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
              )
            }
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

      "in NormalMode" - {

        "must continue when mainPurchaserID does not exist and adding the main purchaser" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn))
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, NormalMode)
          result mustBe continueRoute
        }

        "must continue when main purchaser is incomplete and is being edited" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
              emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH001"))))
            ))
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.Yes).success.value
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, NormalMode)
          result mustBe continueRoute
        }

        "must redirect to CYA when main purchaser is incomplete and is not being edited" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
              emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH002"))))
            ))
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, NormalMode)
          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
          )
        }

        "must redirect to CYA when main purchaser is complete" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturnConstants.completeFullReturn))
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, NormalMode)
          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
          )
        }
      }

      "in CheckMode" - {

        "must continue when mainPurchaserID does not exist and adding the main purchaser" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn))
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, CheckMode)
          result mustBe continueRoute
        }

        "must continue when main purchaser is incomplete and is being edited" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
              emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH001"))))
            ))
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.Yes).success.value
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, CheckMode)
          result mustBe continueRoute
        }

        "must continue when main purchaser is incomplete and is not being edited" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(
              emptyFullReturn.copy(purchaser = Some(Seq(incompletePurchaser)), returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PURCH002"))))
            ))
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, CheckMode)
          result mustBe continueRoute
        }

        "must continue when main purchaser is complete" in {
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturnConstants.completeFullReturn))
          val result = service.continueIfAddingMainPurchaser(userAnswers, continueRoute, CheckMode)
          result mustBe continueRoute
        }
      }
    }

    "continueIfAddingMainPurchaserWithPurchaserTypeCheck" - {

      "in NormalMode" - {
        "must continue when purchaser type and main purchaser conditions are met" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Company,
            userAnswers,
            continueRoute,
            NormalMode
          )
          result mustBe continueRoute
        }

        "must redirect to CYA page when purchaser type does not match" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Company,
            userAnswers,
            continueRoute,
            NormalMode
          )
          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
          )
        }

        "must redirect to CYA page when main purchaser conditions are not met" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Individual,
            userAnswers,
            continueRoute,
            NormalMode
          )
          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
          )
        }
      }

      "in CheckMode" - {
        "must continue when purchaser type and main purchaser conditions are met" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Company,
            userAnswers,
            continueRoute,
            CheckMode
          )
          result mustBe continueRoute
        }

        "must redirect to CYA page when purchaser type does not match" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Company,
            userAnswers,
            continueRoute,
            CheckMode
          )
          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
          )
        }

        "must continue when main purchaser conditions are not met" in {
          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
            .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          val result = service.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            WhoIsMakingThePurchase.Individual,
            userAnswers,
            continueRoute,
            CheckMode
          )
          result mustBe continueRoute
        }
      }
    }

    "companyConditionalSummaryRows" - {
      "must return summary rows when PurchaserConfirmIdentity is VatRegistrationNumber" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
            buildingAssociation = "NO",
            centralGovernment = "NO",
            individualOther = "NO",
            insuranceAssurance = "NO",
            localAuthority = "NO",
            partnership = "NO",
            propertyCompany = "NO",
            publicCorporation = "NO",
            otherCompany = "NO",
            otherFinancialInstitute = "NO",
            otherIncludingCharity = "NO",
            superannuationOrPensionFund = "NO",
            unincorporatedBuilder = "NO",
            unincorporatedSoleTrader = "NO")).success.value
          .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.VatRegistrationNumber).success.value

        val rows: Seq[SummaryListRow] = service.companyConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.confirmIdentity.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.registrationNumber.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")

      }

      "must return summary rows when PurchaserConfirmIdentity is CorporationTaxUTR" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
            buildingAssociation = "NO",
            centralGovernment = "NO",
            individualOther = "NO",
            insuranceAssurance = "NO",
            localAuthority = "NO",
            partnership = "NO",
            propertyCompany = "NO",
            publicCorporation = "NO",
            otherCompany = "NO",
            otherFinancialInstitute = "NO",
            otherIncludingCharity = "NO",
            superannuationOrPensionFund = "NO",
            unincorporatedBuilder = "NO",
            unincorporatedSoleTrader = "NO")).success.value
          .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.CorporationTaxUTR).success.value

        val rows: Seq[SummaryListRow] = service.companyConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.confirmIdentity.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.corporationTaxUTR.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")

      }

      "must return summary rows when PurchaserConfirmIdentity is PartnershipUTR" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
            buildingAssociation = "NO",
            centralGovernment = "NO",
            individualOther = "NO",
            insuranceAssurance = "NO",
            localAuthority = "NO",
            partnership = "NO",
            propertyCompany = "NO",
            publicCorporation = "NO",
            otherCompany = "NO",
            otherFinancialInstitute = "NO",
            otherIncludingCharity = "NO",
            superannuationOrPensionFund = "NO",
            unincorporatedBuilder = "NO",
            unincorporatedSoleTrader = "NO")).success.value
          .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.PartnershipUTR).success.value

        val rows: Seq[SummaryListRow] = service.companyConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.confirmIdentity.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.corporationTaxUTR.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")

      }

      "must return summary rows when PurchaserConfirmIdentity is AnotherFormOfID" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
            buildingAssociation = "NO",
            centralGovernment = "NO",
            individualOther = "NO",
            insuranceAssurance = "NO",
            localAuthority = "NO",
            partnership = "NO",
            propertyCompany = "NO",
            publicCorporation = "NO",
            otherCompany = "NO",
            otherFinancialInstitute = "NO",
            otherIncludingCharity = "NO",
            superannuationOrPensionFund = "NO",
            unincorporatedBuilder = "NO",
            unincorporatedSoleTrader = "NO")).success.value
          .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.AnotherFormOfID).success.value

        val rows: Seq[SummaryListRow] = service.companyConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.confirmIdentity.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.companyFormOfId.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")

      }

      "must return summary rows when PurchaserConfirmIdentity is empty and UTR is present in the session" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value
          .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
            buildingAssociation = "NO",
            centralGovernment = "NO",
            individualOther = "NO",
            insuranceAssurance = "NO",
            localAuthority = "NO",
            partnership = "NO",
            propertyCompany = "NO",
            publicCorporation = "NO",
            otherCompany = "NO",
            otherFinancialInstitute = "NO",
            otherIncludingCharity = "NO",
            superannuationOrPensionFund = "NO",
            unincorporatedBuilder = "NO",
            unincorporatedSoleTrader = "NO")).success.value
          .set(PurchaserUTRPage, "11111111").success.value

        val rows: Seq[SummaryListRow] = service.companyConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.confirmIdentity.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.corporationTaxUTR.checkYourAnswersLabel")
        rows.map(_.value.content.asHtml.toString) must contain("11111111")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")
      }
    }

    "individualConditionalSummaryRows" - {
      "must return summary rows" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.Yes).success.value
          .set(PurchaserNationalInsurancePage, "FFFFFFF").success.value
          .set(PurchaserDateOfBirthPage, java.time.LocalDate.now()).success.value

        val rows: Seq[SummaryListRow] = service.individualConditionalSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.doesPurchaserHaveNI.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.nationalInsurance.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.dateOfBirth.checkYourAnswersLabel")

      }

      "must return summary rows with NiNo as No" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No).success.value
          .set(PurchaserNationalInsurancePage, "FFFFFFF").success.value
          .set(PurchaserDateOfBirthPage, java.time.LocalDate.now()).success.value

        val rows: Seq[SummaryListRow] = service.individualConditionalSummaryRows(userAnswers)
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.doesPurchaserHaveNI.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must not contain ("purchaser.nationalInsurance.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must not contain ("purchaser.dateOfBirth.checkYourAnswersLabel")

      }
    }

    "initialSummaryRows" - {
      "must return summary rows with add contact no as YES" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test"), name = "Test")).success.value
          .set(AddPurchaserPhoneNumberPage, true).success.value
          .set(EnterPurchaserPhoneNumberPage, "07477777777").success.value

        val rows: Seq[SummaryListRow] = service.initialSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.nameOfThePurchaser.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.enterPhoneNumber.checkYourAnswersLabel")

      }

      "must return summary rows with with add contact no as NO" in {
        implicit val messages: Messages = stubMessages()
        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test"), name = "Test")).success.value
          .set(AddPurchaserPhoneNumberPage, false).success.value
          .set(EnterPurchaserPhoneNumberPage, "07477777777").success.value

        val rows: Seq[SummaryListRow] = service.initialSummaryRows(userAnswers)

        rows.map(_.key.content.asHtml.toString) must contain("purchaser.nameOfThePurchaser.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must contain("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel")
        rows.map(_.key.content.asHtml.toString) must not contain ("purchaser.enterPhoneNumber.checkYourAnswersLabel")

      }
    }

    "purchaserSessionOptionalQuestionsValidation" - {

      val userAnswers: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturnConstants.completeFullReturn))

      "when purchaser is main" - {
        "when type is Individual" - {
          val purchaserSessionQuestions = PurchaserSessionQuestions(
            PurchaserCurrent(
              purchaserAndCompanyId = Some(PurchaserAndCompanyId(purchaserID = "PUR001", companyDetailsID = Some("COMPDET001"))),
              ConfirmNameOfThePurchaser = Some(ConfirmNameOfThePurchaser.Yes),
              whoIsMakingThePurchase = "Individual",
              nameOfPurchaser = NameOfPurchaser(forename1 = Some("Name1"), forename2 = Some("Name2"), name = "Samsung"),
              purchaserAddress = PurchaserSessionAddress(
                houseNumber = Some("1"),
                line1 = Some("Street 1"),
                line2 = Some("Street 2"),
                line3 = Some("Street 3"),
                line4 = Some("Street 4"),
                line5 = Some("Street 5"),
                postcode = Some("CR7 8LU"),
                country = Some(PurchaserSessionCountry(
                  code = Some("GB"),
                  name = Some("UK")
                )),
                addressValidated = Some(true)),
              addPurchaserPhoneNumber = true,
              enterPurchaserPhoneNumber = Some("+447874363636"),
              doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
              nationalInsuranceNumber = Some("AA123456A"),
              purchaserFormOfIdIndividual = Some(PurchaserFormOfIdIndividual(idNumberOrReference = "ref", countryIssued = "country")),
              purchaserDateOfBirth = Some(LocalDate.of(2000, 2, 2)),
              purchaserConfirmIdentity = Some(PurchaserConfirmIdentity.VatRegistrationNumber),
              registrationNumber = Some("VAT1234"),
              purchaserUTRPage = Some("UTR1234"),
              purchaserFormOfIdCompany = Some(CompanyFormOfId(referenceId = "ID12345", countryIssued = "country")),
              purchaserTypeOfCompany = Some(
                PurchaserTypeOfCompanyAnswers(
                  bank = "YES",
                  buildingAssociation = "NO",
                  centralGovernment = "NO",
                  individualOther = "NO",
                  insuranceAssurance = "NO",
                  localAuthority = "NO",
                  partnership = "NO",
                  propertyCompany = "NO",
                  publicCorporation = "NO",
                  otherCompany = "NO",
                  otherFinancialInstitute = "NO",
                  otherIncludingCharity = "NO",
                  superannuationOrPensionFund = "NO",
                  unincorporatedBuilder = "NO",
                  unincorporatedSoleTrader = "NO")
              ),
              isPurchaserActingAsTrustee = Some("yes"),
              purchaserAndVendorConnected = Some("yes"),
            ))

          "when phone number" - {
            val dataWithPhoneNumberYesAndPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = true,
                  enterPurchaserPhoneNumber = Some("+447874363636")
                )
            )
            val dataWithPhoneNumberYesAndNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = true,
                  enterPurchaserPhoneNumber = None
                )
            )
            val dataWithPhoneNumberNoAndPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = false,
                  enterPurchaserPhoneNumber = Some("+447874363636")
                )
            )
            val dataWithPhoneNumberNoAndNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = false,
                  enterPurchaserPhoneNumber = None
                )
            )

            val phoneNumberCases = Table(
              ("phoneNumberAnswer", "phoneNumberPresent", "sessionData", "result"),
              ("yes", "present", dataWithPhoneNumberYesAndPresent, true),
              ("yes", "not present", dataWithPhoneNumberYesAndNotPresent, false),
              ("no", "present", dataWithPhoneNumberNoAndPresent, true),
              ("no", "not present", dataWithPhoneNumberNoAndNotPresent, true)
            )

            forAll(phoneNumberCases) { (phoneNumberAnswer, phoneNumberPresent, sessionData, result) =>

              s"when user answered $phoneNumberAnswer and phone number is $phoneNumberPresent" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when nino or form of id" - {
            val dataWithNinoAnswerYesWithNinoNotPresentAndDobPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
                nationalInsuranceNumber = None,
                purchaserDateOfBirth = Some(LocalDate.of(2000, 2, 2)),
                purchaserFormOfIdIndividual = None
              )
            )
            val dataWithNinoAnswerYesWithNinoPresentAndDobNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
                nationalInsuranceNumber = Some("123456"),
                purchaserDateOfBirth = None,
                purchaserFormOfIdIndividual = None
              )
            )
            val dataWithNinoAnswerYesWithNinoNotPresentAndDobNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
                nationalInsuranceNumber = None,
                purchaserDateOfBirth = None,
                purchaserFormOfIdIndividual = None
              )
            )
            val dataWithNinoAnswerYesWithNinoPresentAndDobPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
                nationalInsuranceNumber = Some("123456"),
                purchaserDateOfBirth = Some(LocalDate.of(2000, 2, 2)),
                purchaserFormOfIdIndividual = None
              )
            )
            val dataWithNinoAnswerNoWithFormOfIdNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = None,
                nationalInsuranceNumber = None,
                purchaserDateOfBirth = None,
                purchaserFormOfIdIndividual = None
              )
            )
            val dataWithNinoAnswerNoWithFormOfIdPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                doesPurchaserHaveNI = None,
                nationalInsuranceNumber = None,
                purchaserDateOfBirth = None,
                purchaserFormOfIdIndividual = Some(PurchaserFormOfIdIndividual(idNumberOrReference = "ref", countryIssued = "country"))
              )
            )

            val ninoCases = Table(
              ("ninoAnswer", "ninoPresent", "dobPresent", "formOfIdPresent", "sessionData", "result"),
              ("yes", "not present", "present", "not present", dataWithNinoAnswerYesWithNinoNotPresentAndDobPresent, false),
              ("yes", "present", "not present", "not present", dataWithNinoAnswerYesWithNinoPresentAndDobNotPresent, false),
              ("yes", "not present", "not present", "not present", dataWithNinoAnswerYesWithNinoNotPresentAndDobNotPresent, false),
              ("yes", "present", "present", "not present", dataWithNinoAnswerYesWithNinoPresentAndDobPresent, true),
              ("no", "not present", "not present", "not present", dataWithNinoAnswerNoWithFormOfIdNotPresent, false),
              ("no", "not present", "not present", "present", dataWithNinoAnswerNoWithFormOfIdPresent, true),
            )

            forAll(ninoCases) { (ninoAnswer, ninoPresent, dobPresent, formOfIdPresent, sessionData, result) =>

              s"when user answered $ninoAnswer, nino is $ninoPresent, dob is $dobPresent and form of id is $formOfIdPresent" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when acting as trustee" - {
            val dataWithActingAsTrusteePresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                isPurchaserActingAsTrustee = Some("YES")
              )
            )
            val dataWithActingAsTrusteeNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                isPurchaserActingAsTrustee = None
              )
            )

            val actingAsTrusteeCases = Table(
              ("actingAsTrustee", "sessionData", "result"),
              ("present", dataWithActingAsTrusteePresent, true),
              ("not present", dataWithActingAsTrusteeNotPresent, false),
            )

            forAll(actingAsTrusteeCases) { (actingAsTrustee, sessionData, result) =>

              s"when acting as trustee answer is $actingAsTrustee" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when purchaser or vendor connected" - {
            val dataWithPurchaserOrVendorConnectedPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserAndVendorConnected = Some("YES")
              )
            )
            val dataWithPurchaserOrVendorConnectedNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserAndVendorConnected = None
              )
            )

            val purchaserOrVendorConnectedCases = Table(
              ("purchaserOrVendorConnected", "sessionData", "result"),
              ("present", dataWithPurchaserOrVendorConnectedPresent, true),
              ("not present", dataWithPurchaserOrVendorConnectedNotPresent, false),
            )

            forAll(purchaserOrVendorConnectedCases) { (purchaserOrVendorConnected, sessionData, result) =>

              s"when purchaser or vendor connected answer is $purchaserOrVendorConnected" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

        }

        "when type is Company" - {
          val purchaserSessionQuestions = PurchaserSessionQuestions(
            PurchaserCurrent(
              purchaserAndCompanyId = Some(PurchaserAndCompanyId(purchaserID = "PUR001", companyDetailsID = Some("COMPDET001"))),
              ConfirmNameOfThePurchaser = Some(ConfirmNameOfThePurchaser.Yes),
              whoIsMakingThePurchase = "Company",
              nameOfPurchaser = NameOfPurchaser(forename1 = Some("Name1"), forename2 = Some("Name2"), name = "Samsung"),
              purchaserAddress = PurchaserSessionAddress(
                houseNumber = Some("1"),
                line1 = Some("Street 1"),
                line2 = Some("Street 2"),
                line3 = Some("Street 3"),
                line4 = Some("Street 4"),
                line5 = Some("Street 5"),
                postcode = Some("CR7 8LU"),
                country = Some(PurchaserSessionCountry(
                  code = Some("GB"),
                  name = Some("UK")
                )),
                addressValidated = Some(true)),
              addPurchaserPhoneNumber = true,
              enterPurchaserPhoneNumber = Some("+447874363636"),
              doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
              nationalInsuranceNumber = Some("AA123456A"),
              purchaserFormOfIdIndividual = Some(PurchaserFormOfIdIndividual(idNumberOrReference = "ref", countryIssued = "country")),
              purchaserDateOfBirth = Some(LocalDate.of(2000, 2, 2)),
              purchaserConfirmIdentity = Some(PurchaserConfirmIdentity.VatRegistrationNumber),
              registrationNumber = Some("VAT1234"),
              purchaserUTRPage = Some("UTR1234"),
              purchaserFormOfIdCompany = Some(CompanyFormOfId(referenceId = "ID12345", countryIssued = "country")),
              purchaserTypeOfCompany = Some(
                PurchaserTypeOfCompanyAnswers(
                  bank = "YES",
                  buildingAssociation = "NO",
                  centralGovernment = "NO",
                  individualOther = "NO",
                  insuranceAssurance = "NO",
                  localAuthority = "NO",
                  partnership = "NO",
                  propertyCompany = "NO",
                  publicCorporation = "NO",
                  otherCompany = "NO",
                  otherFinancialInstitute = "NO",
                  otherIncludingCharity = "NO",
                  superannuationOrPensionFund = "NO",
                  unincorporatedBuilder = "NO",
                  unincorporatedSoleTrader = "NO")
              ),
              isPurchaserActingAsTrustee = Some("yes"),
              purchaserAndVendorConnected = Some("yes"),
            ))

          "when phone number" - {
            val dataWithPhoneNumberYesAndPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = true,
                  enterPurchaserPhoneNumber = Some("+447874363636")
                )
            )
            val dataWithPhoneNumberYesAndNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = true,
                  enterPurchaserPhoneNumber = None
                )
            )
            val dataWithPhoneNumberNoAndPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = false,
                  enterPurchaserPhoneNumber = Some("+447874363636")
                )
            )
            val dataWithPhoneNumberNoAndNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  addPurchaserPhoneNumber = false,
                  enterPurchaserPhoneNumber = None
                )
            )

            val phoneNumberCases = Table(
              ("phoneNumberAnswer", "phoneNumberPresent", "sessionData", "result"),
              ("yes", "present", dataWithPhoneNumberYesAndPresent, true),
              ("yes", "not present", dataWithPhoneNumberYesAndNotPresent, false),
              ("no", "present", dataWithPhoneNumberNoAndPresent, true),
              ("no", "not present", dataWithPhoneNumberNoAndNotPresent, true)
            )

            forAll(phoneNumberCases) { (phoneNumberAnswer, phoneNumberPresent, sessionData, result) =>

              s"when user answered $phoneNumberAnswer and phone number is $phoneNumberPresent" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when form of id" - {
            val dataWithUTRPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  purchaserUTRPage = Some("utr1234"),
                  registrationNumber = None,
                  purchaserFormOfIdCompany = None
                )
            )

            val dataWithVATPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  purchaserUTRPage = None,
                  registrationNumber = Some("vat1234"),
                  purchaserFormOfIdCompany = None
                )
            )

            val dataWithAnotherFormIdPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  purchaserUTRPage = None,
                  registrationNumber = None,
                  purchaserFormOfIdCompany = Some(CompanyFormOfId(referenceId = "ID12345", countryIssued = "country"))
                )
            )

            val dataWithNotFormOfIdPresent = purchaserSessionQuestions.copy(
              purchaserCurrent =
                purchaserSessionQuestions.purchaserCurrent.copy(
                  purchaserUTRPage = None,
                  registrationNumber = None,
                  purchaserFormOfIdCompany = None
                )
            )


            val formOfIdCases = Table(
              ("UTR", "VAT", "AnotherFormOfId", "sessionData", "result"),
              ("present", "not present", "not present", dataWithUTRPresent ,true),
              ("not present", "present", "not present", dataWithVATPresent, true),
              ("not present", "not present", "present", dataWithAnotherFormIdPresent, true),
              ("not present", "not present", "not present", dataWithNotFormOfIdPresent,false),
            )

            forAll(formOfIdCases) { (UTR, VAT, AnotherFormOfId, sessionData, result) =>

              s"when UTR is $UTR, VAT is $VAT and Another Form Of Id is $AnotherFormOfId" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when company type" - {
            val dataWithCompanyTypePresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserTypeOfCompany = Some(PurchaserTypeOfCompanyAnswers(bank = "YES",
                  buildingAssociation = "YES",
                  centralGovernment = "NO",
                  individualOther = "NO",
                  insuranceAssurance = "NO",
                  localAuthority = "NO",
                  partnership = "NO",
                  propertyCompany = "NO",
                  publicCorporation = "NO",
                  otherCompany = "NO",
                  otherFinancialInstitute = "NO",
                  otherIncludingCharity = "NO",
                  superannuationOrPensionFund = "NO",
                  unincorporatedBuilder = "NO",
                  unincorporatedSoleTrader = "NO")),
              )
            )
            val dataWithCompanyTypeNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserTypeOfCompany = None
              )
            )

            val companyTypeCases = Table(
              ("purchaserTypeOfCompany", "sessionData", "result"),
              ("present", dataWithCompanyTypePresent, true),
              ("not present", dataWithCompanyTypeNotPresent, false),
            )

            forAll(companyTypeCases) { (purchaserTypeOfCompany, sessionData, result) =>

              s"when purchaser or vendor connected answer is $purchaserTypeOfCompany" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when acting as trustee" - {
            val dataWithActingAsTrusteePresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                isPurchaserActingAsTrustee = Some("YES")
              )
            )
            val dataWithActingAsTrusteeNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                isPurchaserActingAsTrustee = None
              )
            )

            val actingAsTrusteeCases = Table(
              ("actingAsTrustee", "sessionData", "result"),
              ("present", dataWithActingAsTrusteePresent, true),
              ("not present", dataWithActingAsTrusteeNotPresent, false),
            )

            forAll(actingAsTrusteeCases) { (actingAsTrustee, sessionData, result) =>

              s"when acting as trustee answer is $actingAsTrustee" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }

          "when purchaser or vendor connected" - {
            val dataWithPurchaserOrVendorConnectedPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserAndVendorConnected = Some("YES")
              )
            )
            val dataWithPurchaserOrVendorConnectedNotPresent = purchaserSessionQuestions.copy(
              purchaserCurrent = purchaserSessionQuestions.purchaserCurrent.copy(
                purchaserAndVendorConnected = None
              )
            )

            val purchaserOrVendorConnectedCases = Table(
              ("purchaserOrVendorConnected", "sessionData", "result"),
              ("present", dataWithPurchaserOrVendorConnectedPresent, true),
              ("not present", dataWithPurchaserOrVendorConnectedNotPresent, false),
            )

            forAll(purchaserOrVendorConnectedCases) { (purchaserOrVendorConnected, sessionData, result) =>

              s"when purchaser or vendor connected answer is $purchaserOrVendorConnected" - {
                s"must return $result" in {
                  service.purchaserSessionOptionalQuestionsValidation(
                    sessionData = sessionData,
                    userAnswers = userAnswers
                  ) mustBe result
                }
              }
            }
          }
        }
      }

      "when purchaser is not main" - {

        "when phone number" - {

          val dataWithPhoneNumberYesAndPresent: PurchaserSessionQuestions =
            PurchaserSessionQuestions(purchaserCurrent =
              PurchaserCurrent(
                purchaserAndCompanyId = Some(PurchaserAndCompanyId(purchaserID = "PUR002", companyDetailsID = Some("COMPDET001"))),
                ConfirmNameOfThePurchaser = Some(ConfirmNameOfThePurchaser.Yes),
                whoIsMakingThePurchase = "Company",
                nameOfPurchaser = NameOfPurchaser(forename1 = Some("Name1"), forename2 = Some("Name2"), name = "Samsung"),
                purchaserAddress = PurchaserSessionAddress(
                  houseNumber = Some("1"),
                  line1 = Some("Street 1"),
                  line2 = Some("Street 2"),
                  line3 = Some("Street 3"),
                  line4 = Some("Street 4"),
                  line5 = Some("Street 5"),
                  postcode = Some("CR7 8LU"),
                  country = Some(PurchaserSessionCountry(
                    code = Some("GB"),
                    name = Some("UK")
                  )),
                  addressValidated = Some(true)),
                addPurchaserPhoneNumber = true,
                enterPurchaserPhoneNumber = Some("+447874363636")
              ))
          val dataWithPhoneNumberYesAndNotPresent = dataWithPhoneNumberYesAndPresent.copy(
            purchaserCurrent =
              dataWithPhoneNumberYesAndPresent.purchaserCurrent.copy(
                addPurchaserPhoneNumber = true,
                enterPurchaserPhoneNumber = None
              )
          )
          val dataWithPhoneNumberNoAndPresent = dataWithPhoneNumberYesAndPresent.copy(
            purchaserCurrent =
              dataWithPhoneNumberYesAndPresent.purchaserCurrent.copy(
                addPurchaserPhoneNumber = false,
                enterPurchaserPhoneNumber = Some("+447874363636")
              )
          )
          val dataWithPhoneNumberNoAndNotPresent = dataWithPhoneNumberYesAndPresent.copy(
            purchaserCurrent =
              dataWithPhoneNumberYesAndPresent.purchaserCurrent.copy(
                addPurchaserPhoneNumber = false,
                enterPurchaserPhoneNumber = None
              )
          )

          val cases = Table(
            ("phoneNumberAnswer", "phoneNumberPresent", "sessionData", "result"),
            ("yes", "present", dataWithPhoneNumberYesAndPresent, true),
            ("yes", "not present", dataWithPhoneNumberYesAndNotPresent, false),
            ("no", "present", dataWithPhoneNumberNoAndPresent, true),
            ("no", "not present", dataWithPhoneNumberNoAndNotPresent, true)
          )

          forAll(cases) { (phoneNumberAnswer, phoneNumberPresent, sessionData, result) =>

            s"when user answered $phoneNumberAnswer and phone number is $phoneNumberPresent" - {
              s"must return $result" in {
                service.purchaserSessionOptionalQuestionsValidation(
                  sessionData = sessionData,
                  userAnswers = userAnswers
                ) mustBe result
              }
            }
          }
        }
      }
    }
  }
}