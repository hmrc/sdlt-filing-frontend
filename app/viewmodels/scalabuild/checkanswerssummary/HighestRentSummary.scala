/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.RentPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.bigDecimalFormat
import viewmodels.scalabuild.govuk.summarylist.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.scalabuild.implicits._

object HighestRentSummary {

  def row(answers: UserAnswers) (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RentPage).map { answer =>
      val highestRent = bigDecimalFormat(answer.rents.max)
      SummaryListRowViewModel(
        key = KeyViewModel("leaseDate.highestRent.checkYourAnswersLabel"),
        value = ValueViewModel.withId(text = highestRent, id = "td2_highestRent")
      )
    }
}
