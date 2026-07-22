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

package viewmodels.tasklist

import base.SpecBase
import config.FrontendAppConfig
import constants.FullReturnConstants.*
import models.{CompanyDetails, Purchaser, ReturnInfo}
import org.scalatest.prop.TableDrivenPropertyChecks.*
import play.api.i18n.Messages
import play.api.test.Helpers.running

class PurchaserTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnCompleteWithOneMainPurchaser = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1)))
  private val fullReturnCompleteWithMultiplePurchasers = completeFullReturn
  private val fullReturnSomeMandatoryFieldsMissing = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1.copy(
      isCompany = Some("NO"),
      address1 = None,
      isTrustee = Some("YES"),
      isConnectedToVendor = Some("YES"),
      registrationNumber = Some("6666677777"),
      placeOfRegistration = Some("Cyprus"),
      nino = None,
      dateOfBirth = None,
    ))))
  private val fullReturnAllMandatoryFieldsMissing = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1.copy(
      isCompany = None,
      address1 = None,
      surname = None,
      isTrustee = None,
      isConnectedToVendor = None,
      registrationNumber = None,
      placeOfRegistration = None,
      nino = None,
      dateOfBirth = None,
    ))))
  private val fullReturnMissingPurchaser = fullReturnComplete.copy(purchaser = None)

  "PurchaserTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when purchaser is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when purchaser is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".mandatoryFieldsDefined" - {

      "return correct sequence when purchaser is a company and companyDetails is defined" in {
        val cases = Table(
          (
            "isCompanyValue",
            "address1Defined",
            "isTrusteeDefined",
            "isConnectedToVendorDefined",
            "companyNameDefined",
            "hasVATOrUTR",
            "hasRegAndPlace",
            "expectedResult"
          ),
          // all fields defined
          (Some("YES"), true, true, true, true, true, true, Seq(true, true, true, true, true, true)),
          // missing companyName, but VAT/UTR present
          (Some("YES"), true, true, true, false, true, false, Seq(true, true, true, true, false, true)),
          // no VAT/UTR, but registration and place present
          (Some("YES"), true, true, true, true, false, true, Seq(true, true, true, true, true, true)),
          // no VAT/UTR and no registration and place present
          (Some("YES"), true, true, true, true, false, false, Seq(true, true, true, true, true, false))
        )

        forAll(cases) { (
                          isCompanyValue: Option[String],
                          address1Defined: Boolean,
                          isTrusteeDefined: Boolean,
                          isConnectedToVendorDefined: Boolean,
                          companyNameDefined: Boolean,
                          hasVATOrUTR: Boolean,
                          hasRegAndPlace: Boolean,
                          expectedResult: Seq[Boolean]
                        ) =>
          val mainPurchaser = Purchaser(
            purchaserID = Some("PUR001"),
            isCompany = isCompanyValue,
            address1 = if (address1Defined) Some("address 1") else None,
            isTrustee = if (isTrusteeDefined) Some("YES") else None,
            isConnectedToVendor = if (isConnectedToVendorDefined) Some("YES") else None,
            companyName = if (companyNameDefined) Some("Company Co") else None,
            registrationNumber = if (hasRegAndPlace) Some("6666677777") else None,
            placeOfRegistration = if (hasRegAndPlace) Some("Cyprus") else None,
            nino = None,
            dateOfBirth = None,
            surname = Some("SMITH")
          )

          val companyDetails =
            if (hasVATOrUTR) {
              Some(CompanyDetails(VATReference = Some("VAT"), UTR = None))
            } else {
              Some(CompanyDetails(VATReference = None, UTR = None))
            }

          val fullReturn = completeFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001"))),
            purchaser = Some(Seq(mainPurchaser)),
            companyDetails = companyDetails
          )

          val result = PurchaserTaskList.mandatoryFieldsDefined(fullReturn)

          result mustBe expectedResult
        }
      }

      "return correct sequence when purchaser is a company and companyDetails is not defined" in {
          val mainPurchaser = Purchaser(
            purchaserID = Some("PUR001"),
            isCompany = Some("Yes"),
            address1 =  Some("address 1"),
            isTrustee = Some("YES"),
            isConnectedToVendor = Some("YES"),
            companyName = Some("Company Co")
          )

          val fullReturn = completeFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001"))),
            purchaser = Some(Seq(mainPurchaser)),
            companyDetails = None
          )

          val result = PurchaserTaskList.mandatoryFieldsDefined(fullReturn)

          result mustBe Seq(true, true, true, true, false)
        }

      "return correct sequence when purchaser is a individual" in {
        val cases = Table(
          (
            "isCompanyValue",
            "address1Defined",
            "isTrusteeDefined",
            "isConnectedToVendorDefined",
            "surnameDefined",
            "ninoDefined",
            "dobDefined",
            "regDefined",
            "placeDefined",
            "expectedResult"
          ),
          // nino and DOB defined
          (
            Some("NO"), true, true, true, true, true, true, false, false,
            Seq(true, true, true, true, true, true, true)
          ),
          // nino defined and DOB missing
          (
            Some("NO"), true, true, true, true, true, false, false, false,
            Seq(true, true, true, true, true, true, false)
          ),
          // no nino, registration number and place defined
          (
            Some("NO"), true, true, true, true, false, false, true, true,
            Seq(true, true, true, true, true, true)
          ),
          // no nino, registration number defined, but place not defined
          (
            Some("NO"), true, true, true, true, false, false, true, false,
            Seq(true, true, true, true, true, false)
          ),
          //  no nino, registration number not defined, but place defined
          (
            Some("NO"), true, true, true, true, false, false, false, true,
            Seq(true, true, true, true, true, false)
          )
        )

        forAll(cases) { (
                          isCompanyValue: Option[String],
                          address1Defined: Boolean,
                          isTrusteeDefined: Boolean,
                          isConnectedToVendorDefined: Boolean,
                          surnameDefined: Boolean,
                          ninoDefined: Boolean,
                          dobDefined: Boolean,
                          regDefined: Boolean,
                          placeDefined: Boolean,
                          expectedResult: Seq[Boolean]
                        ) =>
          val mainPurchaser = Purchaser(
            purchaserID = Some("PUR001"),
            isCompany = isCompanyValue,
            address1 = if (address1Defined) Some("address 1") else None,
            isTrustee = if (isTrusteeDefined) Some("YES") else None,
            isConnectedToVendor = if (isConnectedToVendorDefined) Some("YES") else None,
            companyName = None,
            registrationNumber = if (regDefined) Some("6666677777") else None,
            placeOfRegistration = if (placeDefined) Some("Cyprus") else None,
            nino = if (ninoDefined) Some("AB123456C") else None,
            dateOfBirth = if (dobDefined) Some("2000-01-01") else None,
            surname = if (surnameDefined) Some("SMITH") else None
          )

          val fullReturn = completeFullReturn.copy(
            returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR001"))),
            purchaser = Some(Seq(mainPurchaser)),
            companyDetails = None
          )

          val result = PurchaserTaskList.mandatoryFieldsDefined(fullReturn)

          result mustBe expectedResult
        }
      }
    }

    ".isPurchaserComplete" - {

      "must return true if purchaser exists and mandatory fields are defined" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnComplete)

        result mustBe true
      }

      "must return false if purchaser exists but some mandatory field are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnSomeMandatoryFieldsMissing)

        result mustBe false
      }

      "must return false if purchaser exists but all mandatory fields are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnAllMandatoryFieldsMissing)

        result mustBe false
      }
    }

    ".buildPurchaserRow" - {

      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result.tagId mustBe "purchaserQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
        }
      }

      "must have Purchaser Before You Start url and show 'Not started yet' status when main purchaser is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnMissingPurchaser)

          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Purchaser Before You Start url and show 'Not started yet' status when no mandatory fields are present in main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnAllMandatoryFieldsMissing)

          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Purchaser Before You Start url and show 'In progress' status when some mandatory fields are present in main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnSomeMandatoryFieldsMissing)

          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Purchaser Overview url when and show 'Complete' status when all mandatory fields are present in main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnCompleteWithOneMainPurchaser)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

          result.status mustBe TLCompleted
        }
      }

      "must have Purchaser Overview url when main purchaser complete among other purchasers" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnCompleteWithMultiplePurchasers)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must show 'Complete' status when a main purchaser is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Not yet started' status when purchaser is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnMissingPurchaser)

          result.status mustBe TLNotStarted
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when purchaser present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(fullReturnCompleteWithOneMainPurchaser)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with not started row when purchaser absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(fullReturnMissingPurchaser)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
