import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.web.Import.{Assets, pipelineStages}
import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys
import sbt.Keys.*
import sbt.{Def, *}
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, *}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "sdltc-frontend"

ThisBuild / majorVersion := 5
ThisBuild / scalaVersion := "2.13.17"

lazy val playSettings: Seq[Setting[?]] = Seq(
  Assets / unmanagedResourceDirectories += baseDirectory.value / "app" / "assets",
  // Dont include the source assets in the dist package (public folder)
  Assets / excludeFilter := "js*" || "sass*",
  TwirlKeys.templateImports ++= Seq(
    "uk.gov.hmrc.govukfrontend.views.html.components._",
    "viewmodels.scalabuild.govuk.all._"
  ),
) ++ JavaScriptBuild.javaScriptUiSettings

lazy val plugins: Seq[Plugins] = Seq(PlayScala, SbtDistributablesPlugin, SbtWeb)
lazy val appDependencies: Seq[ModuleID] = AppDependencies()
lazy val scoverageSettings: Seq[Def.Setting[? >: String & Double & Boolean]] = {
  // Semicolon-separated list of regexs matching classes to exclude
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;view.*;config.*;.*(BuildInfo|Routes).*;journey.views.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

maintainer := "your.name@company.org"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins *)
  .settings(playSettings *)
  .settings((playSettings ++ scoverageSettings) *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    PlayKeys.playDefaultPort := 9953,
    libraryDependencies ++= appDependencies,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    Assets / pipelineStages := Seq(digest)
  )
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    resolvers ++= Seq(),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s")
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings(), libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  .settings(libraryDependencies ++= AppDependencies.itDependencies)

addCommandAlias("runAllChecks", ";clean;compile;coverage;test;it/test;coverageReport")
