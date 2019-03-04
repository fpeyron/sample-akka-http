package fr.sysf.sample.route

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import buildinfo.BuildInfo

import scala.concurrent.duration._

/**
  * Companion object to implement route info server (and health)
  */
object InfoRoute extends Directives {

  def apply(): Route = new InfoRoute().route
}

/**
  * Route info server (and health)
  */
class InfoRoute extends Directives {

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  def route: Route =
    path("health") {
      complete(StatusCodes.OK → HttpEntity(MediaTypes.`application/json`, s"""{"status": "UP"}""".stripMargin))
    } ~
      path("info") {
        complete(StatusCodes.OK → HttpEntity(MediaTypes.`application/json`, s"""{
              "group": "${BuildInfo.organization}",
              "name": "${BuildInfo.name}",
              "version": "${BuildInfo.version}",
              "buildTime": "${BuildInfo.buildTime}",
              "buildScalaVersion": "${BuildInfo.scalaVersion}",
              "buildSbtVersion": "${BuildInfo.sbtVersion}",
              "description": "${BuildInfo.description}"
              }""".stripMargin))
      }
}
