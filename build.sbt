import org.typelevel.sbt.tpolecat.*

ThisBuild / organization := "com.github.pk65"
ThisBuild / scalaVersion := "3.3.3"
Compile / run / fork := true

// This disables fatal-warnings for local development. To enable it in CI set the `SBT_TPOLECAT_CI` environment variable in your pipeline.
// See https://github.com/typelevel/sbt-tpolecat/?tab=readme-ov-file#modes
ThisBuild / tpolecatDefaultOptionsMode := VerboseMode
val CatsEffectVersion = "3.5.4"
val Log4catsSlf4jVersion = "2.7.0"
val LogbackVersion = "1.5.8"

lazy val root = (project in file(".")).settings(
  name := "search_postfix_log",
  version := "0.1.0-SNAPSHOT",
  maintainer := "pawel.kuszynski@gmail.com",
  scalacOptions ++= {
    Seq(
      "-new-syntax",
      "explain-types",
      "-rewrite",
      "-source:future-migration",
      "-Yexplicit-nulls"
    )
  },
  scalacOptions --= {
    Seq("-Xfatal-warnings")
  },
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % CatsEffectVersion,
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % CatsEffectVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    // https://github.com/AsamK/signal-cli/issues/1584
    "org.typelevel" %% "log4cats-slf4j" % Log4catsSlf4jVersion,
    "com.github.scopt" %% "scopt" % "4.1.0",
    "org.scalameta" %% "munit" % "1.0.2" % Test,
    "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test)
)
enablePlugins(JavaAppPackaging)
