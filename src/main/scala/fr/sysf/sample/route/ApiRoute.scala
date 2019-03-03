package fr.sysf.sample.route

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server._
import akka.util.{ByteString, Timeout}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import fr.sysf.sample.actor.WorkerActor
import fr.sysf.sample.actor.WorkerActor.{GetTopHitReply, StatLine}
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.duration._
import scala.util.Try

/**
  *
  */
object ApiRoute {

  def apply()(implicit system: ActorSystem, workerActor: ActorRef[WorkerActor.Command]): Route = new ApiRoute().route
}

/**
  *
  */
@Api(value = "/api")
@Path("/api")
class ApiRoute()(implicit system: ActorSystem, workerActor: ActorRef[WorkerActor.Command]) extends Directives with FailFastCirceSupport {

  import io.circe.generic.auto._

  implicit lazy val timeout: Timeout = Timeout(5.seconds)
  implicit val scheduler: Scheduler  = system.scheduler

  def route: Route = pathPrefix("api") {
    getList ~ getTopHit
  }

  @Path("list")
  @ApiOperation(
    value      = "download List of records with modulo replacements",
    notes      = "",
    nickname   = "downloadList",
    httpMethod = "GET",
    produces   = "text/plain; charset=UTF-8"
  )
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Return lines", response = classOf[String])))
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(name = "limit", value = "Limit of records", required = false, dataType = "integer", paramType = "query", defaultValue = "100"),
      new ApiImplicitParam(name = "int1", value  = "1: modulo", required        = false, dataType = "integer", paramType = "query", defaultValue = "3"),
      new ApiImplicitParam(name = "str1", value  = "1: value", required         = false, dataType = "string", paramType  = "query", defaultValue = "fizz"),
      new ApiImplicitParam(name = "int2", value  = "2: modulo", required        = false, dataType = "integer", paramType = "query", defaultValue = "5"),
      new ApiImplicitParam(name = "str2", value  = "2: value", required         = false, dataType = "string", paramType  = "query", defaultValue = "buzz")
    )
  )
  def getList: Route = (path("list") & get) {
    parameters(('limit.?, 'int1.?, 'int2.?, 'str1.?, 'str2.?)) { (limitOptional, int1, int2, str1, str2) ⇒
      // set limit value with query parameter `limit` with default value (100)
      val limit: Long = limitOptional.flatMap(s ⇒ Try(s.toLong).toOption).filter(_ > 0).getOrElse(100)

      // set dictionary value with query parameters `int1`, `int2`, `str1`, `str2` with default for value (none)
      val dictionary: WorkerActor.Dictionary = Seq((int1, str1), (int2, str2)).flatMap { t ⇒
        t._1.flatMap(s ⇒ Try(s.toInt).toOption).filter(_ > 0).map(i ⇒ (i, t._2.getOrElse("none")))
      }.toMap

      // set filename value for attach attachment value (http header)
      val filename = s"list_${limit.toString}.txt"

      respondWithHeader(RawHeader("Content-Disposition", s"attachment; filename=$filename")) {
        var isFirst = true
        onSuccess(workerActor ? WorkerActor.GetList(dictionary)) { r ⇒
          complete(
            HttpEntity(
              contentType = ContentTypes.`text/plain(UTF-8)`,
              data = r.source
                .take(limit)
                .map { r ⇒
                  if (isFirst) {
                    isFirst = false
                    ByteString.apply(r)
                  } else {
                    ByteString.apply(s",$r")
                  }
                }
            )
          )
        }
      }
    }
  }
  @Path("statistics/top")
  @ApiOperation(
    value      = "get the most used request for getList",
    notes      = "",
    nickname   = "getTopHit",
    httpMethod = "GET",
    produces   = "application/json; charset=UTF-8"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Return top", response = classOf[StatLine]),
      new ApiResponse(code = 204, message = "Not found", response  = classOf[Unit]),
    )
  )
  def getTopHit: Route = (path("statistics" / "top") & get) {

    onSuccess(workerActor ? WorkerActor.GetTopHit()) {
      case GetTopHitReply(None)    ⇒ complete(StatusCodes.NoContent)
      case GetTopHitReply(Some(r)) ⇒ complete(StatusCodes.OK → r)
    }
  }
}
