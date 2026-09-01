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

  private val fullReturnCompleteWithMultiplePurchasers = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1, completePurchaser2, completePurchaser3)))

  private val fullReturnSomeMandatoryFieldsMissingMain = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1.copy(
      address1 = None
    ), completePurchaser2, completePurchaser3)))

  private val fullReturnAllMandatoryFieldsMissingMain = fullReturnComplete.copy(
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
    ), completePurchaser2, completePurchaser3)))

  private val prelimPurchaserCompany = Purchaser(
    isCompany = Some("YES"),
    address1 = Some("123 Fake Street"),
    companyName = Some("Company name"),
    purchaserID = Some("PUR001"),
    returnID = Some("RET123456789"),
    purchaserResourceRef = Some("PUR-REF-001")
  )

  private val prelimCompanyDetails = CompanyDetails(
    companyDetailsID = Some("CD001"),
    returnID = Some("RET123456789"),
    purchaserID = Some("PUR001")
  )

  private val prelimPurchaserIndividual = Purchaser(
    isCompany = Some("NO"),
    address1 = Some("123 Fake Street"),
    surname = Some("Smith"),
    purchaserID = Some("PUR001"),
    returnID = Some("RET123456789"),
    purchaserResourceRef = Some("PUR-REF-001")
  )

  private val fullReturnPrelimPurchaserCompany = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserCompany)),
    companyDetails = Some(prelimCompanyDetails)
  )

  private val fullReturnPrelimPurchaserIndividual = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual)),
    companyDetails = None
  )

  private val fullReturnPrelimPurchaserNotMain = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual.copy(purchaserID = Some("PUR999")))),
    companyDetails = None
  )

  private val fullReturnPrelimPurchaserPlusNino = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual.copy(nino = Some("AB123456C")))),
    companyDetails = None
  )

  private val fullReturnPrelimPurchaserPlusTrustee = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual.copy(isTrustee = Some("YES")))),
    companyDetails = None
  )

  private val fullReturnPrelimPurchaserWithSecondPurchaser = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual, completePurchaser2)),
    companyDetails = None
  )

  private val fullReturnPrelimIndividualWithCompanyDetails = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserIndividual)),
    companyDetails = Some(prelimCompanyDetails)
  )

  private val fullReturnPrelimCompanyWithVat = fullReturnComplete.copy(
    purchaser = Some(Seq(prelimPurchaserCompany)),
    companyDetails = Some(prelimCompanyDetails.copy(VATReference = Some("VAT123")))
  )

  private val fullReturnSomeMandatoryFieldsMissingOther = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1, completePurchaser2, completePurchaser3.copy(
      address1 = None
    ))))

  private val fullReturnAllMandatoryFieldsMissingOther = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1, completePurchaser2, completePurchaser3.copy(
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

      "must report the prelim answers as passing checks for an individual" in {
        val result = PurchaserTaskList.mandatoryFieldsDefined(fullReturnPrelimPurchaserIndividual)

        result mustBe Seq(true, true, false, false, true, false)
      }

      "must report the prelim answers as passing checks for a company" in {
        val result = PurchaserTaskList.mandatoryFieldsDefined(fullReturnPrelimPurchaserCompany)

        result mustBe Seq(true, true, false, false, true, false)
      }

      "must return a single failing check when there are no purchasers" in {
        PurchaserTaskList.mandatoryFieldsDefined(fullReturnMissingPurchaser) mustBe Seq(false)
      }
    }

    ".isPrelimPurchaser" - {

      "must return true when the single main purchaser is an individual with only prelim answers" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserIndividual) mustBe true
      }

      "must return true when the single main purchaser is a company with only prelim answers" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserCompany) mustBe true
      }

      "must return false when the purchaser is not the main purchaser" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserNotMain) mustBe false
      }

      "must return false when an identity field beyond the prelim answers is present" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserPlusNino) mustBe false
      }

      "must return false when the trustee question has been answered" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserPlusTrustee) mustBe false
      }

      "must return false when an individual has company details attached" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimIndividualWithCompanyDetails) mustBe false
      }

      "must return false when a company already has a VAT reference" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimCompanyWithVat) mustBe false
      }

      "must return false when a second purchaser is present" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnPrelimPurchaserWithSecondPurchaser) mustBe false
      }

      "must return false when the purchaser is complete" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnComplete) mustBe false
      }

      "must return false when there are no purchasers" in {
        PurchaserTaskList.isPrelimPurchaser(fullReturnMissingPurchaser) mustBe false
      }
    }

    ".purchaserChecks" - {

      "must return a single failing check when the individual purchaser is prelim only" in {
        PurchaserTaskList.purchaserChecks(fullReturnPrelimPurchaserIndividual) mustBe Seq(false)
      }

      "must return a single failing check when the company purchaser is prelim only" in {
        PurchaserTaskList.purchaserChecks(fullReturnPrelimPurchaserCompany) mustBe Seq(false)
      }

      "must fall through to the mandatory checks when the purchaser is not prelim" in {
        val result = PurchaserTaskList.purchaserChecks(fullReturnSomeMandatoryFieldsMissingMain)

        result mustBe PurchaserTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissingMain)
      }

      "must report progress once a field beyond the prelim answers is present" in {
        val result = PurchaserTaskList.purchaserChecks(fullReturnPrelimPurchaserPlusNino)

        result mustBe Seq(true, true, false, false, true, true, false)
      }

      "must return all passing checks when every purchaser is complete" in {
        val result = PurchaserTaskList.purchaserChecks(fullReturnCompleteWithMultiplePurchasers)

        result.forall(identity) mustBe true
      }

      "must return a single failing check when there are no purchasers" in {
        PurchaserTaskList.purchaserChecks(fullReturnMissingPurchaser) mustBe Seq(false)
      }
    }

    ".isPurchaserComplete" - {

      "must return true if purchaser exists and mandatory fields are defined" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnComplete)

        result mustBe true
      }

      "must return false if main purchaser exists but some mandatory field are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnSomeMandatoryFieldsMissingMain)

        result mustBe false
      }

      "must return false if other purchaser exists but some mandatory field are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnSomeMandatoryFieldsMissingOther)

        result mustBe false
      }

      "must return false if main purchaser exists but all mandatory fields are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnAllMandatoryFieldsMissingMain)

        result mustBe false
      }

      "must return false if other purchaser exists but all mandatory fields are missing" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnAllMandatoryFieldsMissingOther)

        result mustBe false
      }

      "must return false when the purchaser is prelim only" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnPrelimPurchaserIndividual)

        result mustBe false
      }

      "must return false when there are no purchasers" in {
        val result = PurchaserTaskList.isPurchaserComplete(fullReturnMissingPurchaser)

        result mustBe false
      }
    }

    ".incompletePurchasers" - {

      "must return no purchasers when every purchaser is complete" in {
        PurchaserTaskList.incompletePurchasers(fullReturnCompleteWithMultiplePurchasers) mustBe empty
      }

      "must return the main purchaser when the main purchaser is incomplete" in {
        val result = PurchaserTaskList.incompletePurchasers(fullReturnSomeMandatoryFieldsMissingMain)

        result.map(_.purchaserID) mustBe Seq(Some("PUR001"))
      }

      "must return only the incomplete purchaser when an 'other' purchaser is incomplete" in {
        val result = PurchaserTaskList.incompletePurchasers(fullReturnSomeMandatoryFieldsMissingOther)

        result.size mustBe 1
      }

      "must return the purchaser that exists but has no mandatory fields answered" in {
        val result = PurchaserTaskList.incompletePurchasers(fullReturnAllMandatoryFieldsMissingMain)

        result.map(_.purchaserID) mustBe Seq(Some("PUR001"))
      }

      "must return the prelim purchaser" in {
        val result = PurchaserTaskList.incompletePurchasers(fullReturnPrelimPurchaserIndividual)

        result.map(_.purchaserID) mustBe Seq(Some("PUR001"))
      }

      "must return no purchasers when there are no purchasers" in {
        PurchaserTaskList.incompletePurchasers(fullReturnMissingPurchaser) mustBe empty
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

      "must have Purchaser Incomplete Overview url and show 'In Progress' status when no mandatory fields are present in main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnAllMandatoryFieldsMissingMain)
          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Before You Start url and show 'Not yet started' status when only prelim fields are present in main purchaser for individual" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnPrelimPurchaserIndividual)
          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must have Before You Start url and show 'Not yet started' status when only prelim fields are present in main purchaser for company" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnPrelimPurchaserCompany)
          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          result.status mustBe TLNotStarted
        }
      }

      "must move from 'Not yet started' to 'In Progress' once an identity field is answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnPrelimPurchaserPlusNino)
          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must show 'In Progress' status when the prelim-shaped purchaser is not the main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnPrelimPurchaserNotMain)
          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must show 'In Progress' status when a prelim purchaser is joined by a second purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnPrelimPurchaserWithSecondPurchaser)
          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Purchaser Incomplete Overview url and show 'In Progress' status when no mandatory fields are present in other purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnAllMandatoryFieldsMissingOther)
          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Purchaser Incomplete Overview url and show 'In progress' status when some mandatory fields are present in main purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnSomeMandatoryFieldsMissingMain)

          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Purchaser Incomplete Overview url and show 'In progress' status when some mandatory fields are present in other purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnSomeMandatoryFieldsMissingOther)

          result.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Purchaser Overview url when and show 'Complete' status when all mandatory fields are present in all purchasers" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnCompleteWithMultiplePurchasers)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

          result.status mustBe TLCompleted
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

          val section = PurchaserTaskList.build(fullReturnCompleteWithMultiplePurchasers)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must build a TaskListSection routing an incomplete purchaser to the incomplete overview" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(fullReturnSomeMandatoryFieldsMissingMain)
          val row = section.rows.head

          row.status mustBe TLInProgress
          row.url mustBe controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url
        }
      }

      "must build a TaskListSection with a not started row when only prelim purchaser data is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(fullReturnPrelimPurchaserIndividual)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
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