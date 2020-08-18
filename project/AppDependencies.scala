import play.sbt.PlayImport.{filters, ws}
import sbt.ModuleID
import sbt._

object AppDependencies {
  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "2.24.0",
    "uk.gov.hmrc" %% "govuk-template"     % "5.55.0-play-27",
    "uk.gov.hmrc" %% "play-ui"            % "8.11.0-play-27"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "hmrctest"                     % "3.9.0-play-26" % scope,
        "org.scalamock"           %% "scalamock-scalatest-support"  % "3.6.0"         % scope,
        "org.mockito"             % "mockito-core"                  % "3.4.4"         % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play"           % "3.1.3"         % scope,
        "org.jsoup"               % "jsoup"                         % "1.13.1"        % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "hmrctest"           % "3.9.0-play-26" % scope,
        "com.github.tomakehurst"  % "wiremock-jre8"       % "2.27.1"        % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.3"         % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
