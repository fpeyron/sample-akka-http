import java.time.Instant

import sbt.enablePlugins

organization := "fr.sysf"
name         := "sample-akka-http"
description  := "Sample Akka http"

libraryDependencies ++= Dependencies.logging ++
  Dependencies.akka ++
  Dependencies.akkaHttp ++
  Dependencies.swagger ++
  Dependencies.testIt

// ----------------
// Run
// ----------------
mainClass in (Compile, run) := Some("fr.sysf.sample.Main")
fork in run                 := true

// ----------------
// Generate BuildInfo
// ----------------
enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion, description, "buildTime" → Instant.now)

// ----------------
// Test
// ----------------
parallelExecution in Test := true

// ----------------
// ScalaStyle
// ----------------
scalastyleFailOnError    := true
coverageExcludedPackages := ".*Main.*;.*Config.*;.*BuildInfo*;.*SwaggerRoute.*;.*SwaggerUiRoute.*"

// ----------------
// test it (gatling)
// ----------------
enablePlugins(GatlingPlugin)
