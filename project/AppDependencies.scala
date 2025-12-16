import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val bootstrapVersion         = "10.4.0"
  private val playVersion              = "12.24.0"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "3.4.0"
  )
  val itDependencies: Seq[ModuleID] = Seq()

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.mockito"         %% "mockito-scala"          % "2.0.0"        % Test,
    "org.scalacheck"      %% "scalacheck"             % "1.19.0"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}