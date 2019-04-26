package fr.sysf.sample

import java.net.InetAddress

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.discovery.awsapi.ecs.AsyncEcsServiceDiscovery
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.PermanentRedirect
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import buildinfo.BuildInfo
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import fr.sysf.sample.actor.WorkerActor
import fr.sysf.sample.route._

/**
  * Main class executable
  */
object Main extends App with LazyLogging {

  val privateAddress: InetAddress =
    AsyncEcsServiceDiscovery.getContainerAddress match {
      case Left(error) ⇒
        logger.error(s"$error Halting.")
        sys.exit(1)

      case Right(value) ⇒
        value
    }
  logger.info(s"privateAddress           : ${privateAddress.getHostAddress}")

  val config = ConfigFactory.load
    .withValue("akka.actor.management.http.hostname", ConfigValueFactory.fromAnyRef(privateAddress.getHostAddress))
    .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(privateAddress.getHostAddress))

  /*

    val config = ConfigFactory
      .systemProperties()
      .withFallback(
        ConfigFactory.parseString(s"""
                                     |akka {
                                     |  actor.provider = "cluster"
                                     |  management {
                                     |    cluster.bootstrap.contact-point.fallback-port = 8558
                                     |    http.hostname = "${privateAddress.getHostAddress}"
                                     |  }
                                     |  discovery.method = aws-api-ecs-async
                                     |  remote.netty.tcp.hostname = "${privateAddress.getHostAddress}"
                                     |}
             """.stripMargin)
      )
   */

  // Initialize systems actors (needed for worker and http server)
  implicit val system: akka.actor.ActorSystem = akka.actor.ActorSystem("system", config)
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

  logger.info(s"Name           : ${BuildInfo.name}")
  logger.info(s"Build version  : ${BuildInfo.version}")
  logger.info(s"Build time     : ${BuildInfo.buildTime}")
  logger.info(s"Service info   : http://${Config.api.hostname}:${Config.api.port}/info")
  logger.info(s"Service health : http://${Config.api.hostname}:${Config.api.port}/health")
  logger.info(s"Service api    : http://${Config.api.hostname}:${Config.api.port}/api")
  logger.info(s"Swagger json   : http://${Config.api.hostname}:${Config.api.port}/api-docs/swagger.json")
  logger.info(s"Swagger ui     : http://${Config.api.hostname}:${Config.api.port}/swagger")

  // Akka Management hosts the HTTP routes used by bootstrap
  AkkaManagement(system).start()

  // Starting the bootstrap process needs to be done explicitly
  ClusterBootstrap(system).start()

  // Needed to shutdown properly
  scala.sys.addShutdownHook {
    logger.info("Terminating...")
    system.terminate()
    logger.info("Terminated.")
  }

}
