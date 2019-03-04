package fr.sysf.sample.route

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import fr.sysf.sample.actor.WorkerActor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class ApiRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  val testKit: ActorTestKit                            = ActorTestKit()
  val workerActorProbe: TestProbe[WorkerActor.Command] = testKit.createTestProbe[WorkerActor.Command]()

  val apiRoute: Route = ApiRoute()(testKit.system.toUntyped, workerActorProbe.ref)

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "getList (GET /api/list)" should {

    "get with no parameter" in {

      val request = HttpRequest(uri = "/api/list") ~> apiRoute

      val cmd = workerActorProbe.expectMessageType[WorkerActor.GetList]
      assertResult(Map.empty)(cmd.dictionary)

      cmd.replyTo ! WorkerActor.GetListReply(Source(101 to 500).map(_.toString))

      request ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        header(name = "Content-Disposition") should ===(Some(RawHeader("Content-Disposition", "attachment; filename=list_100.txt")))
        entityAs[String] should ===((101 to 200).mkString(","))
      }

    }

    "get with all parameters" in {

      val request = HttpRequest(uri = "/api/list?limit=15&int1=3&int2=5&str1=fizz&str2=buzz") ~> apiRoute

      val cmd = workerActorProbe.expectMessageType[WorkerActor.GetList]
      assertResult(Map(3 -> "fizz", 5 -> "buzz"))(cmd.dictionary)

      cmd.replyTo ! WorkerActor.GetListReply(Source("1,2,fizz,4,buzz,fizz,7,8,fizz,10,11,fizz,13,14,fizzbuzz,17,18".split(",").toList))

      request ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        header(name = "Content-Disposition") should ===(Some(RawHeader("Content-Disposition", "attachment; filename=list_15.txt")))
        entityAs[String] should ===("1,2,fizz,4,buzz,fizz,7,8,fizz,10,11,fizz,13,14,fizzbuzz")
      }
    }

    "get with some parameters" in {

      HttpRequest(uri = "/api/list?limit=15&int1=3&int2=5") ~> apiRoute

      val cmd = workerActorProbe.expectMessageType[WorkerActor.GetList]
      assertResult(Map(3 -> "none", 5 -> "none"))(cmd.dictionary)

      cmd.replyTo ! WorkerActor.GetListReply(Source("1,2,none,4,none,none,7,8,none,10,11,none,13,14,nonenone,17,18".split(",").toList))
    }

  }

  "getStatisticTop (GET /api/statistics/top)" should {

    "when statistic not exists" in {

      val request = HttpRequest(uri = "/api/statistics/top") ~> apiRoute

      val cmd = workerActorProbe.expectMessageType[WorkerActor.GetTopHit]

      cmd.replyTo ! WorkerActor.GetTopHitReply(None)

      request ~> check {
        status should ===(StatusCodes.NoContent)
      }

    }

    "when statistic exists" in {

      val request = HttpRequest(uri = "/api/statistics/top") ~> apiRoute

      val cmd = workerActorProbe.expectMessageType[WorkerActor.GetTopHit]

      cmd.replyTo ! WorkerActor.GetTopHitReply(Some(WorkerActor.StatLine(Map(3 -> "fizz", 5 -> "buzz"), 57)))

      request ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"dictionary":{"3":"fizz","5":"buzz"},"count":57}""")
      }
    }
  }
}
