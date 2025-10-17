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

import models.FullReturn

import scala.annotation.tailrec

case class TaskListRowBuilder(messageKey: FullReturn => String,
                              url: FullReturn => TaskListState => String,
                              tagId: String,
                              checks: FullReturn => Seq[Boolean],
                              prerequisites: FullReturn => Seq[TaskListRowBuilder],
                              error: FullReturn => Boolean = _ => false,
                              canEdit: TaskListState => Boolean = _ => false) {

  def isComplete(fullReturn: FullReturn): Boolean = checks(fullReturn).forall(_ == true)

  def prerequisitesMet(fullReturn: FullReturn): Boolean = {
    @tailrec
    def checkCompleteness(rows: Seq[TaskListRowBuilder], result: Boolean = true): Boolean = {
      rows match {
        case Nil => result
        case row :: tail => checkCompleteness(tail ++ row.prerequisites(fullReturn), result && row.isComplete(fullReturn))
      }
    }

    checkCompleteness(prerequisites(fullReturn))
  }
  

  def build(fullReturn: FullReturn): TaskListSectionRow = {
    val preCheck: Boolean = prerequisitesMet(fullReturn)
    val status = preCheck match {
      case true if(error(fullReturn)) => TLFailed
      case true if(isComplete(fullReturn)) => TLCompleted
      case true if(checks(fullReturn).contains(true)) => TLInProgress
      case true => TLNotStarted
      case _ => TLCannotStart
    }

    TaskListSectionRow(messageKey(fullReturn), url(fullReturn)(status), tagId, status, canEdit(status))
  }
}