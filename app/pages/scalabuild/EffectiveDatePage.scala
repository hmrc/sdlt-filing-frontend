/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild
import play.api.libs.json.JsPath

import java.time.LocalDate

case object EffectiveDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "effectiveDate"
}
