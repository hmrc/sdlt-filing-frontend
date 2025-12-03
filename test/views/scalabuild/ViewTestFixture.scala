/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package views.scalabuild

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat

abstract class ViewTestFixture extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val htmlContent:HtmlFormat.Appendable

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  lazy val htmlString = htmlContent.body
  lazy val document: Document = Jsoup.parse(htmlString)

  lazy val heading = document.select("h1").text()

  lazy val bodyText = document.select("p").text()
  lazy val hintText = document.select(".govuk-hint").text()
  lazy val summaryText = document.select(".govuk-details__summary-text").text()

  lazy val buttonText = document.select(".govuk-button").text()

  lazy val sign_in_href = document.select("a.govuk-button").attr("href")

  lazy val input_field = document.select("input.govuk-input")

  lazy val input_field_label = document.select("label.govuk-label").text()

  lazy val radios = document.select(".govuk-radios").text()

  lazy val dateField = document.select(".govuk-date-input__label").text()

}
