package fr.sysf.sample.route

import akka.http.scaladsl.server.{Directives, PathMatcher, Route}

/**
  * Companion object to implement route Swagger UI server
  */
object SwaggerUiRoute extends Directives {

  def apply(pathName: PathMatcher[Unit] = "swagger"): Route =
    path(pathName) {
      getFromResource("swagger/index.html")
    } ~ getFromResourceDirectory("swagger")
}
