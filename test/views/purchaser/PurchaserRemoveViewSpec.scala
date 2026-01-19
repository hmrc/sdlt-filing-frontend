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

package views.purchaser

import base.SpecBase
import forms.purchaser.PurchaserRemoveFormProvider
import models.NormalMode
import models.purchaser.PurchaserRemoveData
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.purchaser.PurchaserRemoveView

class PurchaserRemoveViewSpec extends SpecBase {

  private val formProvider = new PurchaserRemoveFormProvider()
  private val form = formProvider()

  "PurchaserRemoveView" - {

    "when removing single main purchaser (count = 1)" - {

      "must render heading with purchaser name" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val heading = doc.select("h1").first()
          heading.text() mustBe messagesInstance("purchaser.purchaserRemove.headingSingle", "John Smith")
        }
      }

      "must render Yes/No radio options" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val yesRadio = doc.getElementById("value_0")
          yesRadio must not be null
          yesRadio.attr("value") mustBe "REMOVE-PURCH001"
          yesRadio.parent().text() must include(messagesInstance("site.yes"))

          val noRadio = doc.getElementById("value_1")
          noRadio must not be null
          noRadio.attr("value") mustBe "no"
          noRadio.parent().text() must include(messagesInstance("site.no"))
        }
      }
    }

    "when removing non-main purchaser (count = 0, isMainPurchaser = false)" - {

      "must render heading with purchaser name" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH002",
            companyDetailsId = None,
            purchaserCount = 0,
            purchaserToRemoveName = Some("Jane Doe"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = false
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val heading = doc.select("h1").first()
          heading.text() mustBe messagesInstance("purchaser.purchaserRemove.headingSingle", "Jane Doe")
        }
      }

      "must render Yes/No radio options with correct remove value" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH002",
            companyDetailsId = None,
            purchaserCount = 0,
            purchaserToRemoveName = Some("Jane Doe"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = false
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val yesRadio = doc.getElementById("value_0")
          yesRadio.attr("value") mustBe "REMOVE-PURCH002"
        }
      }
    }

    "when removing main purchaser with exactly two purchasers (count = 2)" - {

      "must render double removal heading" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 2,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(("PURCH002", "Jane Doe")),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val heading = doc.select("h1").first()
          heading.text() mustBe messagesInstance("purchaser.purchaserRemove.double.heading", "John Smith")
        }
      }

      "must render hint text explaining the situation" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 2,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(("PURCH002", "Jane Doe")),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val hint = doc.select("p.govuk-hint").first()
          hint.text() mustBe messagesInstance(
            "purchaser.purchaserRemove.double.hint",
            "John Smith",
            "Jane Doe"
          )
        }
      }

      "must render keep and remove radio options" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 2,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(("PURCH002", "Jane Doe")),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val keepRadio = doc.getElementById("value_0")
          keepRadio must not be null
          keepRadio.attr("value") mustBe "keep"
          keepRadio.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.double.keep", "John Smith")
          )

          val removeRadio = doc.getElementById("value_1")
          removeRadio must not be null
          removeRadio.attr("value") mustBe "REMOVE-PURCH002"
          removeRadio.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.double.remove", "John Smith", "Jane Doe")
          )
        }
      }

      "must render subheading" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 2,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(("PURCH002", "Jane Doe")),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          doc.text() must include(messagesInstance("purchaser.purchaserRemove.double.subheading"))
        }
      }
    }

    "when removing main purchaser with multiple purchasers (count > 2)" - {

      "must render multi removal heading" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson"),
              ("PURCH004", "Alice Johnson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val heading = doc.select("h1").first()
          heading.text() mustBe messagesInstance("purchaser.purchaserRemove.multi.heading", "John Smith")
        }
      }

      "must render hint text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson"),
              ("PURCH004", "Alice Johnson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val hint = doc.select("p.govuk-hint").first()
          hint.text() mustBe messagesInstance("purchaser.purchaserRemove.multi.hint", "John Smith")
        }
      }

      "must render keep option as first radio" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson"),
              ("PURCH004", "Alice Johnson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val keepRadio = doc.getElementById("value_0")
          keepRadio must not be null
          keepRadio.attr("value") mustBe "keep"
          keepRadio.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.multi.keep", "John Smith")
          )
        }
      }

      "must render divider after keep option" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          doc.text() must include(messagesInstance("site.or"))
        }
      }

      "must render radio option for each remaining purchaser" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson"),
              ("PURCH004", "Alice Johnson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val radio1 = doc.getElementById("value_1")
          radio1 must not be null
          radio1.attr("value") mustBe "PROMOTE-PURCH002"
          radio1.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.multi.remove", "John Smith", "Jane Doe")
          )

          val radio2 = doc.getElementById("value_2")
          radio2 must not be null
          radio2.attr("value") mustBe "PROMOTE-PURCH003"
          radio2.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.multi.remove", "John Smith", "Bob Wilson")
          )

          val radio3 = doc.getElementById("value_3")
          radio3 must not be null
          radio3.attr("value") mustBe "PROMOTE-PURCH004"
          radio3.parent().text() must include(
            messagesInstance("purchaser.purchaserRemove.multi.remove", "John Smith", "Alice Johnson")
          )
        }
      }

      "must render subheading" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 4,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq(
              ("PURCH002", "Jane Doe"),
              ("PURCH003", "Bob Wilson")
            ),
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          doc.text() must include(messagesInstance("purchaser.purchaserRemove.multi.subheading"))
        }
      }
    }

    "common structure" - {

      "must render continue button" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val button = doc.select("button[type=submit]").first()
          button must not be null
          button.text() mustBe messagesInstance("site.continue")
        }
      }

      "must render error summary when form has errors" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val formWithErrors = form.bind(Map("value" -> ""))

          val html = view(formWithErrors, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val errorSummary = doc.select("div.govuk-error-summary").first()
          errorSummary must not be null
        }
      }

      "must render form with correct action" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val formElement = doc.select("form").first()
          formElement must not be null
          formElement.attr("action") mustBe controllers.purchaser.routes.PurchaserRemoveController.onSubmit().url
        }
      }

      "must render with correct page title" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[PurchaserRemoveView]

          val data = PurchaserRemoveData(
            purchaserId = "PURCH001",
            companyDetailsId = None,
            purchaserCount = 1,
            purchaserToRemoveName = Some("John Smith"),
            remainingPurchasers = Seq.empty,
            isMainPurchaser = true
          )

          val html = view(form, NormalMode, data)
          val doc = Jsoup.parse(html.toString())

          val title = doc.select("title").first()
          title must not be null
          title.text() must include(messagesInstance("purchaser.purchaserRemove.title"))
        }
      }
    }
  }
}