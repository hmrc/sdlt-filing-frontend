import sbt._

object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse
  import play.PlayImport.PlayKeys._
  import sbt.Keys._
  import com.typesafe.sbt.web.SbtWeb
  import com.typesafe.sbt.web.SbtWeb.autoImport._

  val appName = "sdltc-frontend"
  val appVersion = envOrElse("SDLTC_FRONTEND_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  override lazy val plugins : Seq[Plugins] = Seq(play.PlayScala, SbtWeb)

  override lazy val playSettings : Seq[Setting[_]] = Seq(
        // Turn off play's internal less compiler
        lessEntryPoints := Nil,
        // Turn off play's internal javascript compiler
        javascriptEntryPoints := Nil,
        // Add the views to the dist
        unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
        // Dont include the source assets in the dist package (public folder)
        excludeFilter in Assets := "js*" || "sass*",
        // Override the scala dependency (remove eviction warning) - remove this once sbt-settings updated
        dependencyOverrides += "org.scala-lang" % "scala-library" % "2.11.7"
        ) ++ JavaScriptBuild.javaScriptUiSettings

}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    filters,
    ws,
    "com.typesafe.play" %% "play" % PlayVersion.current,
    "uk.gov.hmrc" %% "play-filters" % "4.5.1",
    "uk.gov.hmrc" %% "play-ui" % "4.9.0",
    "uk.gov.hmrc" %% "play-graphite" % "2.0.0",
    "uk.gov.hmrc" %% "play-config" % "2.0.1",
    "uk.gov.hmrc" %% "play-health" % "1.1.0",
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
    "uk.gov.hmrc" %% "govuk-template" % "4.0.0",
    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8",
    "com.codahale.metrics" % "metrics-graphite" % "3.0.2"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


