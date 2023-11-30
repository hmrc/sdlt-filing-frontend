import play.sbt.PlayImport.{filters, ws}
import sbt.ModuleID
import sbt._

object AppDependencies {
  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "8.1.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.29.0-play-28"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  val itDependencies: Seq[ModuleID] = Seq()

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"                  %% "bootstrap-test-play-28"      % "8.1.0"      % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala"        % "2.16.0"      % scope,
        "org.mockito"                  %%  "mockito-scala"              % "1.17.30"     % scope,
        "org.jsoup"                    %  "jsoup"                       % "1.17.1"      % scope
      )
    }.test
  }

  val jacksonVersion = "2.16.0"
  val jacksonDatabindVersion = "2.16.0"

  val jacksonOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-core",
    "com.fasterxml.jackson.core" % "jackson-annotations",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
  ).map(_ % jacksonVersion)

  val jacksonDatabindOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
  )

  val akkaSerializationJacksonOverrides = Seq(
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names",
    "com.fasterxml.jackson.module" %% "jackson-module-scala",
  ).map(_ % jacksonVersion)

  def apply() = compile ++ jacksonDatabindOverrides ++ jacksonOverrides ++ akkaSerializationJacksonOverrides ++ Test()
}
