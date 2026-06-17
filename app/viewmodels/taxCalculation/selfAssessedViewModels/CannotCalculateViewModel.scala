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

package viewmodels.taxCalculation.selfAssessedViewModels

import play.api.i18n.Messages

case class CannotCalculateViewModel(
                                     paragraph: Option[String],
                                     additionalParagraph: Option[String],
                                     bulletPoints: Seq[String]
                                   )

object CannotCalculateViewModel {

  private val bulletPointReasons = Set("reason5", "reason7", "reason8")
  private val reasonsWithThirdBullet = Set("reason7", "reason8")

  def toViewModel(reasons: List[String])(implicit messages: Messages): CannotCalculateViewModel = {
    reasons match {
      case Nil           => noReason
      case reason :: Nil => singleReason(reason)
      case _             => multipleReasons(reasons)
    }
  }
  
  private def noReason(implicit messages: Messages): CannotCalculateViewModel =
    CannotCalculateViewModel(
      paragraph = Some(messages("taxCalculation.cannotCalculateSdltDue.noReason.p1")),
      additionalParagraph = Some(messages("taxCalculation.cannotCalculateSdltDue.noReason.p2")),
      bulletPoints = Nil
    )
    
  private def singleReason(reason: String)(implicit messages: Messages): CannotCalculateViewModel = {
    val bullets =
      if (bulletPointReasons.contains(reason)) {
        Seq(
          messages(s"taxCalculation.cannotCalculateSdltDue.$reason.b1"),
          messages(s"taxCalculation.cannotCalculateSdltDue.$reason.b2")
        ) ++
          (if (reasonsWithThirdBullet.contains(reason))
            Seq(messages(s"taxCalculation.cannotCalculateSdltDue.$reason.b3"))
          else
            Seq.empty)
      } else {
        Nil
      }

    CannotCalculateViewModel(
      paragraph = Some(messages("taxCalculation.cannotCalculateSdltDue.p1", messages(s"taxCalculation.cannotCalculateSdltDue.$reason"))),
      additionalParagraph = None,
      bulletPoints = bullets
    )
  }
  
  private def multipleReasons(reasons: List[String])(implicit messages: Messages): CannotCalculateViewModel =
    CannotCalculateViewModel(
      paragraph = Some(messages("taxCalculation.cannotCalculateSdltDue.p1.withColon")),
      additionalParagraph = None,
      bulletPoints = reasons.map(reason =>
        messages(s"taxCalculation.cannotCalculateSdltDue.multipleReasons.b${reason.stripPrefix("reason")}")
      )
    )
}
