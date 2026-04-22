import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val bootstrapVersion         = "10.7.0"
  private val playVersion              = "12.32.0"
  private val hmrcMongoVersion         = "2.12.0"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"            % playVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"              % hmrcMongoVersion
  )
  val itDependencies: Seq[ModuleID] = Seq()

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30" % bootstrapVersion  % Test,
    "org.mockito"         %% "mockito-scala"          % "2.2.1"           % Test,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test,
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0"         % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}