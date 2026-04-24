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

package services.checkAnswers

import base.SpecBase
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import viewmodels.checkAnswers.summary.SummaryRowResult.{ Row, Missing }

class CheckAnswersServiceSpec extends SpecBase {

  private val service = new CheckAnswersService()

  "redirectOrRender" - {

    "return Left(call) when a Missing row exists" in {

      val call = controllers.routes.JourneyRecoveryController.onPageLoad()

      val rowResults = Seq(
        Row(SummaryListRow()),
        Missing(call),
        Row(SummaryListRow())
      )

      val result = service.redirectOrRender(rowResults)

      result mustBe Left(call)
    }

    "return the first Missing call when multiple exist" in {

      val call1 = controllers.routes.JourneyRecoveryController.onPageLoad()
      val call2 = controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()

      val rowResults = Seq(
        Missing(call1),
        Missing(call2)
      )

      val result = service.redirectOrRender(rowResults)

      result mustBe Left(call1)
    }

    "return Right(SummaryList) when all rows are present" in {

      val row1 = SummaryListRow()
      val row2 = SummaryListRow()

      val rowResults = Seq(
        Row(row1),
        Row(row2)
      )

      val result = service.redirectOrRender(rowResults)

      result.isRight mustBe true

      val summaryList = result.toOption.value

      summaryList.rows mustBe Seq(row1, row2)
    }
  }
}

