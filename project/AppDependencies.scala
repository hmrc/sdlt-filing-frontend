import play.sbt.PlayImport.{filters, ws}
import sbt.ModuleID
import sbt._

object AppDependencies {
  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.22.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.20.0-play-28"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"                  %% "bootstrap-test-play-28"      % "7.22.0"      % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala"        % "2.14.1"      % scope,
        "org.scalamock"                %% "scalamock-scalatest-support" % "3.6.0"       % scope,
        "org.mockito"                  %  "mockito-core"                % "5.5.0"       % scope,
        "org.scalatestplus"            %% "mockito-3-12"                % "3.2.10.0"    % scope,
        "org.jsoup"                    %  "jsoup"                       % "1.16.1"      % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc"                  %% "bootstrap-test-play-28" % "7.22.0"   % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.14.1"   % scope,
        "org.scalatestplus"            %% "mockito-3-12"           % "3.2.10.0" % scope,
        "com.vladsch.flexmark"         %  "flexmark-all"           % "0.62.2"   % scope // NB Added for scalatest
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
