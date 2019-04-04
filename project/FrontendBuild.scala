import sbt._
import play.sbt.PlayImport._
import play.sbt.PlayScala
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin


object FrontendBuild extends Build with MicroService {
  import com.typesafe.sbt.web.SbtWeb
  import com.typesafe.sbt.web.SbtWeb.autoImport._
  import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
  import sbt.Keys._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import uk.gov.hmrc.SbtArtifactory

  val appName = "sdltc-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  override lazy val plugins : Seq[Plugins] = Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory, SbtWeb)

  override lazy val playSettings : Seq[Setting[_]] = Seq(

        unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
        // Dont include the source assets in the dist package (public folder)
        excludeFilter in Assets := "js*" || "sass*",
        dependencyOverrides += "org.scala-lang" % "scala-library" % "2.11.12"
        ) ++ JavaScriptBuild.javaScriptUiSettings

}

private object AppDependencies {

  import play.core.PlayVersion


  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.37.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.30.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "7.38.0-play-26"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.6.0-play-26" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.6.0-play-26" % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


