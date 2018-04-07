// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `streamarchitect-io-platform-domain` =
  project
    .in(file("."))
    .settings(settings)
    .settings(avroSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.scalaCheck % Test,
        library.scalaTest  % Test,
        library.typesafeConfig,
        library.avro
      ),
      libraryDependencies ++= library.log
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val Scala          = "2.11.12"
      val scalaCheck     = "1.13.5"
      val scalaTest      = "3.0.1"
      val log4j          = "2.8.1"
      val disruptor      = "3.3.0"
      val jackson        = "2.9.2"
      val typesafeConfig = "1.3.1"
      val avro           = "1.8.2"
    }
    val scalaCheck       = "org.scalacheck"           %% "scalacheck"               % Version.scalaCheck
    val scalaTest        = "org.scalatest"            %% "scalatest"                % Version.scalaTest
    val log4jCore        = "org.apache.logging.log4j" % "log4j-core"                % Version.log4j
    val log4j            = "org.apache.logging.log4j" % "log4j-api"                 % Version.log4j
    val log4jSlf4j       = "org.apache.logging.log4j" % "log4j-slf4j-impl"          % Version.log4j
    val disruptor        = "com.lmax"                 % "disruptor"                 % Version.disruptor
    val typesafeConfig   = "com.typesafe"             % "config"                    % Version.typesafeConfig

    val jacksonDatabind = "com.fasterxml.jackson.core"       % "jackson-databind"       % Version.jackson
    val jacksonCore     = "com.fasterxml.jackson.core"       % "jackson-core"           % Version.jackson
    val jacksonXml      = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % Version.jackson

    val avro            = "org.apache.avro"           % "avro"                      % Version.avro

    val log = Seq(log4j, log4jCore, log4jSlf4j, disruptor, jacksonCore, jacksonDatabind, jacksonXml)
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings ++
  publishSettings ++
  releaseSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    scalaVersion := "2.11.12",
    organization := "io.streamarchitect",
    organizationName := "Bastian Kraus",
    startYear := Some(2018),
    licenses += ("GPL-3.0", url("http://www.gnu.org/licenses/gpl-3.0.en.html")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    //sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue,
    credentials += credentialsProvider(),
    updateOptions := updateOptions.value.withGigahorse(false),
    wartremoverWarnings in (Compile, compile) ++= Warts.unsafe
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

val nexusHttpMethod     = Option(System.getenv("NEXUS_HTTP_METHOD")).getOrElse("http")
val nexusUrl            = Option(System.getenv("NEXUS_URL")).getOrElse("nexus.streamarchitect.io")
val nexusRepositoryPath = Option(System.getenv("NEXUS_REPOSITORY_PATH")).getOrElse("repository/streamarchitect-snapshot/")
val nexusColonPort      = Option(System.getenv("NEXUS_PORT")).map(":" + _).getOrElse("")
val nexusUsername       = System.getenv("NEXUS_USERNAME_VARIABLE")
val nexusPassword       = System.getenv("NEXUS_PASSWORD_VARIABLE")
val nexusAddress        = s"$nexusHttpMethod://$nexusUrl$nexusColonPort"
val publishRepository = MavenRepository(
  "Sonatype Nexus Repository Manager",
  s"$nexusAddress/$nexusRepositoryPath"
)

def credentialsProvider(): Credentials = {
  val fileExists = (Path.userHome / ".sbt" / ".credentials-streamarchitect").exists()

  if (fileExists) {
    Credentials(Path.userHome / ".sbt" / ".credentials-streamarchitect")
  } else {
    Credentials(
      "Sonatype Nexus Repository Manager",
      nexusUrl,
      nexusUsername,
      nexusPassword
    )
  }
}

def isSnapshot(): Boolean = nexusRepositoryPath.toLowerCase.contains("snapshot")

lazy val publishSettings = Seq(
  resolvers ++= Seq(
    "nexus" at s"$nexusAddress/repository/maven-public/"
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(publishRepository),
  updateOptions := updateOptions.value.withGigahorse(false)
)

// -----------------------------------------------------------------------------
// release settings

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

val nextVersion = "0.0.1"

releaseNextVersion := { ver =>
  import sbtrelease._

  println(s"Release Version: ${ver} - Preset next Version: ${nextVersion}")

  if (nextVersion > ver) {
    nextVersion
  } else {
    println(
      "nextVersion has not been defined, or been too low compared to current version, therefore it's bumped to next BugFix version"
    )
    Version(ver).map(_.bumpBugfix.asSnapshot.string).getOrElse(versionFormatError)
  }
}

def releaseStepsProvider(): Seq[ReleaseStep] = {
  ConsoleOut.systemOut.println(s"is snapshot: ${isSnapshot()}")
  if (isSnapshot) {
    Seq[ReleaseStep](
      inquireVersions,
      publishArtifacts
    )
  } else {
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }
}

lazy val releaseSettings = Seq(
  releaseProcess := releaseStepsProvider()
)

lazy val avroSettings = sbtavrohugger.SbtAvrohugger.specificAvroSettings