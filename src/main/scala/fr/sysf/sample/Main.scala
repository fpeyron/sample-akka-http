package fr.sysf.sample
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.PermanentRedirect
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import buildinfo.BuildInfo
import com.typesafe.scalalogging.LazyLogging
import fr.sysf.sample.actor.WorkerActor
import fr.sysf.sample.route._

/**
  * Main class executable
  */
object Main extends App with LazyLogging {

  logger.info(s"Name           : ${BuildInfo.name}")
  logger.info(s"Build version  : ${BuildInfo.version}")
  logger.info(s"Build time     : ${BuildInfo.buildTime}")

  // Initialize systems actors (needed for worker and http server)
  implicit val system: akka.actor.ActorSystem = akka.actor.ActorSystem("system")
  implicit val mat: ActorMaterializer         = ActorMaterializer()(system)

  // Initialize actors
  implicit val workerActor: ActorRef[WorkerActor.Command] = system.spawn(WorkerActor(), "Typed")

  // start http server
  val route: Route =
    InfoRoute() ~
      ApiRoute() ~
      SwaggerRoute() ~
      SwaggerUiRoute() ~
      pathEndOrSingleSlash(redirect("/swagger", PermanentRedirect))

  Http().bindAndHandle(route, Config.api.hostname, Config.api.port)

  logger.info(s"Service info   : http://${Config.api.hostname}:${Config.api.port}/info")
  logger.info(s"Service health : http://${Config.api.hostname}:${Config.api.port}/health")
  logger.info(s"Service api    : http://${Config.api.hostname}:${Config.api.port}/api")
  logger.info(s"Swagger json   : http://${Config.api.hostname}:${Config.api.port}/api-docs/swagger.json")
  logger.info(s"Swagger ui     : http://${Config.api.hostname}:${Config.api.port}/swagger")

  // Needed to shutdown properly
  scala.sys.addShutdownHook {
    logger.info("Terminating...")
    system.terminate()
    logger.info("Terminated.")
  }
}
