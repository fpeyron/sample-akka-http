package fr.sysf.sample.route

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.{Directives, PathMatcher0, PathMatchers, Route}
import buildinfo.BuildInfo
import com.github.swagger.akka.model.Info
import com.github.swagger.akka.{CustomMediaTypes, SwaggerGenerator, SwaggerHttpService}
import io.swagger.models.{ExternalDocs, Scheme}

/**
  * Companion object to implement route Swagger description server
  */
object SwaggerRoute extends Directives with SwaggerGenerator {

  import SwaggerHttpService._

  override val apiClasses: Set[Class[_]]        = Set(classOf[ApiRoute])
  override val info: Info                       = Info(version = BuildInfo.version, description = BuildInfo.description, title = BuildInfo.name)
  override val externalDocs                     = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
  override val unwantedDefinitions: Seq[String] = Seq("Function1", "Function1RequestContextFutureRouteResult")
  override val schemes                          = List(Scheme.HTTP, Scheme.HTTPS)

  def apply(): Route = {
    val base = apiDocsBase(apiDocsPath)
    path(base / "swagger.json") {
      get {
        complete(HttpEntity(MediaTypes.`application/json`, generateSwaggerJson))
      }
    } ~
      path(base / "swagger.yaml") {
        get {
          complete(HttpEntity(CustomMediaTypes.`text/vnd.yaml`, generateSwaggerYaml))
        }
      }
  }

  def apiDocsBase(path: String): PathMatcher0 = PathMatchers.separateOnSlashes(removeInitialSlashIfNecessary(path))
}
