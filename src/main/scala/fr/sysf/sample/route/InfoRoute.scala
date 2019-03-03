package fr.sysf.sample.route

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import buildinfo.BuildInfo

/**
  *
  */
object InfoRoute extends Directives {

  def apply(): Route =
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
