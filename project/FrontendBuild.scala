import sbt._
import play.sbt.PlayImport._


object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  import com.typesafe.sbt.web.SbtWeb
  import com.typesafe.sbt.web.SbtWeb.autoImport._
  import sbt.Keys._

  val appName = "sdltc-frontend"
  val appVersion = envOrElse("SDLTC_FRONTEND_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  override lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala, SbtWeb)

  override lazy val playSettings : Seq[Setting[_]] = Seq(

        unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
        // Dont include the source assets in the dist package (public folder)
        excludeFilter in Assets := "js*" || "sass*",
        // Override the scala dependency (remove eviction warning) - remove this once sbt-settings updated
        dependencyOverrides += "org.scala-lang" % "scala-library" % "2.11.7"
        ) ++ JavaScriptBuild.javaScriptUiSettings

}

private object AppDependencies {

  import play.core.PlayVersion


  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "8.10.0",
    "com.kenshoo" %% "metrics-play" % "2.4.0_0.4.1",
    "com.codahale.metrics" % "metrics-graphite" % "3.0.2"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
        "org.scalatest" %% "scalatest" % "3.0.1" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test",
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


