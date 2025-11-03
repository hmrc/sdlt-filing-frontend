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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.tasklist.*
import views.html.components.TaskListRow

class TaskListRowViewSpec extends SpecBase {

  "TaskListSectionRowView" - {

    "when status is TLCannotStart" - {

      "must render with cannot start tag" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLCannotStart)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.text() mustBe messagesInstance("tasklist.cannotStartYet")
          tag.hasClass("govuk-tag--grey") mustBe true
        }
      }

      "must render message as span and not as link" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLCannotStart)
          val doc = Jsoup.parse(html.toString())

          val nameDiv = doc.select("div.govuk-task-list__name-and-hint").first()
          nameDiv.select("span").text() mustBe "Test Task"
        }
      }

      "must render as span (not link) when status is TLCannotStart" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "myCustomTag", TLCannotStart)
          val doc = Jsoup.parse(html.toString())

          // Link should not exist
          Option(doc.getElementById("task-list-link-test-task")) mustBe None

          // But the text should still be visible as a span
          doc.text() must include("Test Task")
          doc.select("a.govuk-task-list__link") mustBe empty
        }
      }
    }

    "when status is TLNotStarted" - {

      "must render with not started tag" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.text() mustBe messagesInstance("tasklist.notStarted")
          tag.hasClass("govuk-tag--grey") mustBe true
        }
      }

      "must render message as link when canEdit is false" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val link = doc.select("a.govuk-link").first()

          link.text() must include("Test Task")
          link.attr("href") mustBe "/test-url"
        }
      }
    }

    "when status is TLInProgress" - {

      "must render with in progress tag" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLInProgress)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.text() mustBe messagesInstance("tasklist.inProgress")
          tag.hasClass("govuk-tag--blue") mustBe true
        }
      }

      "must render message as link" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLInProgress)
          val doc = Jsoup.parse(html.toString())

          val link = doc.select("a.govuk-link").first()
          link.text() must include("Test Task")
          link.attr("href") mustBe "/test-url"
        }
      }
    }

    "when status is TLCompleted" - {

      "must render with completed tag" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLCompleted)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.text() mustBe messagesInstance("tasklist.complete")
          tag.hasClass("app-task-list__task-completed") mustBe true
        }
      }

      "must render message as link when canEdit is true" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "myCustomTag", TLCompleted, canEdit = true)
          val doc = Jsoup.parse(html.toString())

          val link = doc.getElementById("task-list-link-test-task")
          link must not be null
          link.attr("href") mustBe "/test-url"
          link.text() must include("Test Task")

          doc.select("a.govuk-task-list__link") must not be empty
        }
      }

      "must render message as span when canEdit is false" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLCompleted)
          val doc = Jsoup.parse(html.toString())

          val nameDiv = doc.select("div.govuk-task-list__name-and-hint").first()
          nameDiv.select("span").text() mustBe "Test Task"
        }
      }
    }

    "when status is TLFailed" - {

      "must render with failed tag" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLFailed)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.text() mustBe messagesInstance("tasklist.failed")
          tag.hasClass("govuk-tag--red") mustBe true
        }
      }

      "must render message as link" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLFailed)
          val doc = Jsoup.parse(html.toString())

          val link = doc.select("a.govuk-link").first()
          link.text() must include("Test Task")
        }
      }
    }

    "common structure" - {

      "must render as list item with correct classes" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val li = doc.select("li").first()
          li.hasClass("govuk-task-list__item") mustBe true
          li.hasClass("govuk-task-list__item--with-link") mustBe true
        }
      }

      "must have name and hint section" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val nameDiv = doc.select("div.govuk-task-list__name-and-hint").first()
          nameDiv must not be null
        }
      }

      "must have status section" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val statusDiv = doc.select("div.govuk-task-list__status").first()
          statusDiv must not be null
        }
      }

      "must render correct URL" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/custom/path/123", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val link = doc.select("a[href]").first()
          link.attr("href") mustBe "/custom/path/123"
        }
      }

      "must render correct message text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("My Custom Task Name", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          doc.text() must include("My Custom Task Name")
        }
      }

      "must include aria-describedby attribute on link" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "myTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val link = doc.select("a.govuk-link").first()
          link.attr("aria-describedby") mustBe "myTagId"
        }
      }

      "must render tag with no-wrap class" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          val view = application.injector.instanceOf[TaskListRow]

          val html = view("Test Task", "/test-url", "testTagId", TLNotStarted)
          val doc = Jsoup.parse(html.toString())

          val tag = doc.select("strong.govuk-tag").first()
          tag.hasClass("no-wrap") mustBe true
        }
      }
    }
  }
}