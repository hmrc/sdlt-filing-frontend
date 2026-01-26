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
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.mongo.play.PlayMongoModule

abstract class ViewTestFixture extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite {

  private val sessionRepositoryStub: SessionRepository = mock[SessionRepository]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("play.http.router" -> "scalabuild.Routes")
      .disable[PlayMongoModule]
      .overrides(bind[SessionRepository].toInstance(sessionRepositoryStub))
      .build()

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val htmlContent:HtmlFormat.Appendable

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  lazy val htmlString = htmlContent.body
  lazy val document: Document = Jsoup.parse(htmlString)

  lazy val pagetitle = document.select("title").text()
  lazy val heading = document.select("h1").text()
  lazy val caption = document.select(".govuk-caption-xl").text()
  lazy val bodyText = document.select("p").text()
  lazy val hintText = document.select(".govuk-hint").text()
  lazy val summaryText = document.select(".govuk-details__summary-text").text()

  lazy val buttonText = document.select(".govuk-button").text()

  lazy val sign_in_href = document.select("a.govuk-button").attr("href")

  lazy val inputField = document.select("input.govuk-input")

  lazy val linkText = document.getElementsByClass("govuk-link").text

  lazy val inputFieldLabel = document.select("label.govuk-label").eachText()

  lazy val radios = document.select(".govuk-radios").text()

  lazy val radiosHint = document.select(".govuk-radios__hint").text()

  lazy val dateField = document.select(".govuk-date-input__label").text()

  lazy val bullets = document.select("ul.govuk-list--bullet li").eachText()

  lazy val bullet = document.select("li").text

}