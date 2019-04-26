import sbt._

object Dependencies {

  private lazy val akkaVersion        = "2.5.22"
  private lazy val akkaHttpVersion    = "10.1.8"
  private lazy val akkaSwaggerVersion = "1.0.0"
  private lazy val gatlingVersion     = "3.0.2"

  // --- akka core
  lazy val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed"        % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"               % akkaVersion,
    "org.scalatest"     %% "scalatest"                % "3.0.5" % Test,
    "org.scalamock"     %% "scalamock"                % "4.1.0" % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit"      % akkaVersion % Test
  )

  // -- akka cluster
  lazy val akkaCluster: Seq[ModuleID] = Seq(
    "com.typesafe.akka"             %% "akka-discovery"                    % akkaVersion,
    "com.typesafe.akka"             %% "akka-cluster-typed"                % akkaVersion,
    "com.typesafe.akka"             %% "akka-discovery"                    % akkaVersion,
    "com.lightbend.akka.management" %% "akka-management"                   % "1.0.0",
    "com.lightbend.akka.discovery"  %% "akka-discovery-aws-api-async"      % "1.0.0",
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.0"
  )

  // --- akka http
  lazy val akkaHttp: Seq[sbt.ModuleID] = akka ++ Seq(
    "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe"   % "1.25.2",
    "io.circe"          %% "circe-generic"     % "0.11.1",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  )

  // --- akka http swagger
  lazy val swagger: Seq[sbt.ModuleID] = akkaHttp ++ Seq(
    "javax.xml.bind" % "jaxb-api" % "2.3.0",
    ("com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6")
      .exclude("com.fasterxml.jackson.core", "jackson-annotations"),
    ("io.swagger" % "swagger-core" % "1.5.20")
      .exclude("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
      .exclude("com.fasterxml.jackson.core", "jackson-databind")
      .exclude("com.fasterxml.jackson.core", "jackson-annotations")
      .exclude("org.slf4j", "slf4j-api"),
    ("io.swagger" % "swagger-jaxrs" % "1.5.20")
      .exclude("com.fasterxml.jackson.core", "jackson-databind")
      .exclude("org.slf4j", "slf4j-api"),
    ("com.github.swagger-akka-http" %% "swagger-akka-http" % akkaSwaggerVersion)
      .exclude("com.typesafe.akka", "akka-stream_2.12")
      .exclude("com.typesafe.akka", "akka-http_2.12")
  )

  // --- logger
  lazy val logging: Seq[ModuleID] = Seq("com.typesafe.scala-logging" %% "scala-logging" % "3.9.0", "ch.qos.logback" % "logback-classic" % "1.2.3")

  // --- test IT
  lazy val testIt: Seq[ModuleID] = Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % IntegrationTest,
    "io.gatling"            % "gatling-test-framework"    % gatlingVersion % IntegrationTest
  )
}
