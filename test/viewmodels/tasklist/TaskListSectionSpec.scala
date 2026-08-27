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

package viewmodels.tasklist

import base.SpecBase

class TaskListSectionSpec extends SpecBase {

  "TaskListSection" - {

    "isComplete" - {

      "must return true when all rows are completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLCompleted)
        val row3 = TaskListSectionRow("test3", "/url3", "tag3", TLCompleted)
        val row4 = TaskListSectionRow("test4", "/url4", "tag4", TLCompleted)
        val section = TaskListSection("Test Section", Seq(row1, row2, row3, row4))

        section.isComplete mustBe true
      }

      "must return false when some rows are not completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLNotStarted)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when no rows are completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLNotStarted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLInProgress)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return true when section has no rows" in {
        val section = TaskListSection("Empty Section", Seq())

        section.isComplete mustBe true
      }

      "must return false when any row has InProgress status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLInProgress)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when any row has CannotStart status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLCannotStart)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when any row has Failed status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLFailed)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }
    }
  }

  "TaskListSections" - {

    "allComplete" - {

      "must return true when all sections are complete" in {
        val sections = Seq(
          TaskListSection("Section 1", Seq(TaskListSectionRow("k1", "/url1", "tag1", TLCompleted))),
          TaskListSection("Section 2", Seq(TaskListSectionRow("k2", "/url2", "tag2", TLCompleted)))
        )

        TaskListSections.allComplete(sections) mustBe true
      }

      "must return false when at least one section is incomplete" in {
        val sections = Seq(
          TaskListSection("Section 1", Seq(TaskListSectionRow("k1", "/url1", "tag1", TLCompleted))),
          TaskListSection("Section 2", Seq(TaskListSectionRow("k2", "/url2", "tag2", TLNotStarted)))
        )

        TaskListSections.allComplete(sections) mustBe false
      }

      "must return true when there are no sections" in {
        TaskListSections.allComplete(Seq.empty) mustBe true
      }

      "must return false when all sections are incomplete" in {
        val sections = Seq(
          TaskListSection("Section 1", Seq(TaskListSectionRow("k1", "/url1", "tag1", TLNotStarted))),
          TaskListSection("Section 2", Seq(TaskListSectionRow("k2", "/url2", "tag2", TLCannotStart)))
        )

        TaskListSections.allComplete(sections) mustBe false
      }

      "must check every section, not just the first" in {
        val sections = Seq(
          TaskListSection("Section 1", Seq(TaskListSectionRow("k1", "/url1", "tag1", TLCompleted))),
          TaskListSection("Section 2", Seq(TaskListSectionRow("k2", "/url2", "tag2", TLCompleted))),
          TaskListSection("Section 3", Seq(TaskListSectionRow("k3", "/url3", "tag3", TLFailed)))
        )

        TaskListSections.allComplete(sections) mustBe false
      }
    }
  }

  "TaskListSectionRow" - {

    "must create row with all properties" in {
      val row = TaskListSectionRow(
        messageKey = "test.message",
        url = "/test/url",
        tagId = "testTagId",
        status = TLCompleted,
        canEdit = true
      )

      row.messageKey mustBe "test.message"
      row.url mustBe "/test/url"
      row.tagId mustBe "testTagId"
      row.status mustBe TLCompleted
      row.canEdit mustBe true
    }

    "must have default canEdit as false" in {
      val row = TaskListSectionRow(
        messageKey = "test.message",
        url = "/test/url",
        tagId = "testTagId",
        status = TLNotStarted
      )

      row.canEdit mustBe false
    }

    "must support equality" in {
      val row1 = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = true)
      val row2 = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = true)

      row1 mustEqual row2
    }

    "must not be equal when properties differ" in {
      val row1 = TaskListSectionRow("test", "/url", "tag", TLCompleted)
      val row2 = TaskListSectionRow("test", "/url", "tag", TLNotStarted)

      row1 must not equal row2
    }

    "must support copy" in {
      val original = TaskListSectionRow("test", "/url", "tag", TLNotStarted)
      val modified = original.copy(status = TLCompleted)

      modified.status mustBe TLCompleted
      modified.messageKey mustBe original.messageKey
      modified.url mustBe original.url
      modified.tagId mustBe original.tagId
    }

    "must support copy with canEdit" in {
      val original = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = false)
      val modified = original.copy(canEdit = true)

      modified.canEdit mustBe true
      modified.messageKey mustBe original.messageKey
      modified.status mustBe original.status
    }
  }

  "TaskListState" - {

    "TLInvalid" - {

      "must be a TaskListState" in {
        TLInvalid mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLInvalid
        val ref2 = TLInvalid

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLCannotStart" - {

      "must be a TaskListState" in {
        TLCannotStart mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLCannotStart
        val ref2 = TLCannotStart

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLNotStarted" - {

      "must be a TaskListState" in {
        TLNotStarted mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLNotStarted
        val ref2 = TLNotStarted

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLInProgress" - {

      "must be a TaskListState" in {
        TLInProgress mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLInProgress
        val ref2 = TLInProgress

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLOptional" - {

      "must be a TaskListState" in {
        TLOptional mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLOptional
        val ref2 = TLOptional

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLCompleted" - {

      "must be a TaskListState" in {
        TLCompleted mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLCompleted
        val ref2 = TLCompleted

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLFailed" - {

      "must be a TaskListState" in {
        TLFailed mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLFailed
        val ref2 = TLFailed

        ref1 must be theSameInstanceAs ref2
      }
    }

    "all states" - {

      "must be distinct" in {
        val states = Seq(TLCannotStart, TLNotStarted, TLInProgress, TLOptional, TLCompleted, TLFailed, TLInvalid)

        states.distinct.size mustBe 7
      }

      "must not be equal to each other" in {
        TLCannotStart must not equal TLNotStarted
        TLNotStarted must not equal TLInProgress
        TLInProgress must not equal TLOptional
        TLOptional must not equal TLCompleted
        TLCompleted must not equal TLFailed
        TLFailed must not equal TLInvalid
        TLInvalid must not equal TLCannotStart
      }
    }
  }
}