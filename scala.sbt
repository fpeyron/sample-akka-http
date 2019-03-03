scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Xlint",
  "-deprecation",
  "-Xfatal-warnings",
  "-feature",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-Ywarn-unused",
  "-language:_",
  "-Ycache-plugin-class-loader:last-modified",
  "-Ycache-macro-class-loader:last-modified",
  "-Ybackend-parallelism",
  "5"
)
