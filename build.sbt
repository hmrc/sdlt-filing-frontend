import sbt.{Def, _}
import sbt.Keys._
import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.web.Import.{pipelineStages, Assets}
import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.PublishingSettings._
import uk.gov.hmrc.{SbtAutoBuildPlugin, SbtArtifactory}
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

val appName = "sdltc-frontend"
crossScalaVersions := Seq("2.11.12", "2.12.12")
lazy val playSettings: Seq[Setting[_]] = Seq(
  unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
  // Dont include the source assets in the dist package (public folder)
  excludeFilter in Assets := "js*" || "sass*",
  dependencyOverrides += "org.scala-lang" % "scala-library" % "2.12.12"
) ++ JavaScriptBuild.javaScriptUiSettings

lazy val plugins: Seq[Plugins] = Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory, SbtWeb)
lazy val appDependencies: Seq[ModuleID] = AppDependencies()
lazy val scoverageSettings: Seq[Def.Setting[_ >: String with Double with Boolean]] = {
  // Semicolon-separated list of regexs matching classes to exclude
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;view.*;config.*;.*(BuildInfo|Routes).*;journey.views.*",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(playSettings: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "2.12.12")
  .settings(majorVersion := 5)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    targetJvm := "jvm-1.8",
    libraryDependencies ++= appDependencies,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    pipelineStages in Assets := Seq(digest)
  )
  .settings(playPublishingSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo
  )

lazy val playPublishingSettings: Seq[sbt.Setting[_]] = Seq(

  credentials += SbtCredentials,

  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in(Compile, packageSrc) := false
) ++
  publishAllArtefacts