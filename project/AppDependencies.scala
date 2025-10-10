import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val bootstrapVersion         = "10.2.0"
  private val playVersion              = "12.17.0"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playVersion
  )
  val itDependencies: Seq[ModuleID] = Seq()

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.mockito" %% "mockito-scala"          % "2.0.0"        % Test,
  )

  def apply(): Seq[ModuleID] = compile ++ test
}