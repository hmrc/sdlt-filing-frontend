resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.5.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.24.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.1.1")

addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.0.0")