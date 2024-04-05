import play.sbt.PlayImport.{filters, ws}
import sbt.ModuleID
import sbt._

object AppDependencies {
  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30"         % "8.5.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"         % "8.5.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  val itDependencies: Seq[ModuleID] = Seq()

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"                  %% "bootstrap-test-play-30"      % "8.5.0"       % scope,
        "org.mockito"                  %% "mockito-scala"               % "1.17.31"     % scope,
        "org.jsoup"                    %  "jsoup"                       % "1.17.2"      % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}
