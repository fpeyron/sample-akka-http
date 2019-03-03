import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._
import com.typesafe.sbt.packager.docker.Cmd

// ----------------
// Docker packaging
// ----------------
enablePlugins(DockerPlugin, JavaAppPackaging)

mappings in Universal ++= contentOf(baseDirectory.value / "src" / "main" / "docker")
packageName in Docker := s"${name.value}"
version in Docker     := version.value
maintainer in Docker  := "technical support <florent.peyron@gmail.com>"
dockerBaseImage       := "openjdk:11.0.1-jre-slim"
daemonUser in Docker  := "daemon"
dockerExposedPorts    := Seq(8080)
dockerCommands        += Cmd("ENV", "JAVA_OPTS '-Xms256m -Xmx256m -XX:MaxGCPauseMillis=200 -XX:MaxMetaspaceSize=128m -Dfile.encoding=UTF-8'")
dockerUpdateLatest    := true
