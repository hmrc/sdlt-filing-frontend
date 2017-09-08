import sbt._

object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse
  import play.sbt.PlayImport._
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
    "uk.gov.hmrc" %% "play-filters" % "5.10.0",
    "uk.gov.hmrc" %% "play-ui" % "7.0.0",
    "uk.gov.hmrc" %% "play-graphite" % "3.2.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.0.0",
    "com.kenshoo" %% "metrics-play" % "3.2.0",
    "com.codahale.metrics" % "metrics-graphite" % "3.0.2"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


