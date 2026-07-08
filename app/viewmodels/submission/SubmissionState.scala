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

package viewmodels.submission

sealed trait SubmissionState

object SubmissionState {
  case object InProgress extends SubmissionState
  case object AwaitingConfirmation extends SubmissionState
  case object Submitted extends SubmissionState
  case object SubmissionFailed extends SubmissionState
  case object ReSubmit extends SubmissionState

  def parse(status: Option[String]): Option[SubmissionState] =
    status match {
      case Some(s) if s.equalsIgnoreCase("STARTED") => Some(ReSubmit)
      case Some(s) if s.equalsIgnoreCase("ACCEPTED") => Some(AwaitingConfirmation)
      case Some(s) if s.equalsIgnoreCase("SUBMITTED") || s.equalsIgnoreCase("SUBMITTED_NO_RECEIPT") => Some(Submitted)
      case Some(s) if s.equalsIgnoreCase("DEPARTMENTAL_ERROR") || s.equalsIgnoreCase("FATAL_ERROR") => Some(SubmissionFailed)
      case None => Some(InProgress)
      case Some(_) => None
    }
}



