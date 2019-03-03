// ----------------
// Dependencies
// ----------------

// Native packager to build docker distribution
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")
// BuildInfo to include compilation informations (info page)
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
// Almost util to analyse dependencies trees
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
// Util to nice code
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
// Util for other (hum !)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("io.gatling"    % "gatling-sbt"   % "3.0.0")
// Util to build fat Jar
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
