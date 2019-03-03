package fr.sysf.sample.actor

import akka.actor.testkit.typed.scaladsl
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.stream.scaladsl.Sink
import akka.stream.typed.scaladsl.ActorMaterializer
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  *
  */
class WorkerActorSpec extends WordSpecLike with BeforeAndAfterAll {

  import WorkerActor._

  val testKit: ActorTestKit           = ActorTestKit()
  implicit val mat: ActorMaterializer = ActorMaterializer()(testKit.system)

  override def afterAll(): Unit = testKit.shutdownTestKit()

  val probe: scaladsl.TestProbe[Reply] = testKit.createTestProbe[Reply]()

  "translator" should {
    "same where dictionary is empty (100 records)" in {
      assertResult((1 to 100).map(_.toString))((1 to 100).map(translator(Map.empty)))
    }
    "modulo 3 -> fizz (100 records)" in {
      assertResult((1 to 100).map(i ⇒ if (i % 3 == 0) "fizz" else i.toString))((1 to 100).map(translator(Map(3 -> "fizz"))))
    }
    "modulo 5 -> fuzz (100 records)" in {
      assertResult((1 to 100).map(i ⇒ if (i % 5 == 0) "buzz" else i.toString))((1 to 100).map(translator(Map(5 -> "buzz"))))
    }
    "modulo 3 -> fizz and modulo 5 -> fizz (100 records)" in {
      assertResult(
        (1 to 100).map(
          i ⇒
            if (i % 3 == 0 && i % 5 == 0) {
              "fizzbuzz"
            } else if (i % 3 == 0) {
              "fizz"
            } else if (i % 5 == 0) {
              "buzz"
            } else {
              i.toString
          }
        )
      )((1 to 100).map(translator(Map(3 -> "fizz", 5 -> "buzz"))))
    }
  }

  "stat.applyEvent" should {
    "empty Event" in {
      val dic = Map(1 -> "a", 2 -> "b")
      assertResult(List.empty)(Stat().lines)
      assertResult(Stat(List(StatLine(dic, 1))))(Stat().applyEvent(dic))
    }

    "same Type Event exists" in {
      val dic = Map(1 -> "a", 2 -> "b")
      assertResult(Stat(List(StatLine(dic, 2))))(Stat(List(StatLine(dic, 1))).applyEvent(dic))
    }

    "same Type Event non exists" in {
      val dic    = Map(1 -> "a", 2 -> "b")
      val dicNew = Map(3 -> "c", 2 -> "b")
      assertResult(Stat(List(StatLine(dic, 2), StatLine(dicNew, 1))))(Stat(List(StatLine(dic, 2))).applyEvent(dicNew))
    }

    "change order" in {
      val dic    = Map(1 -> "a", 2 -> "b")
      val dicNew = Map(3 -> "c", 2 -> "b")
      assertResult(Stat(List(StatLine(dicNew, 2), StatLine(dic, 1))))(Stat(List(StatLine(dic, 1))).applyEvent(dicNew).applyEvent(dicNew))
    }
  }

  "Cmd GetList" should {

    val actorRef: ActorRef[Command] = testKit.spawn(WorkerActor())

    "return with dictionary empty" in {
      actorRef ! GetList()(probe.ref)
      val result: GetListReply = probe.expectMessageType[GetListReply]
      result.source
        .limit(100)
        .runWith(Sink.seq[String])
        .value
        .contains((1 to 100).map(translator(Map.empty)))
    }

    "return with dictionary non empty" in {
      actorRef ! GetList(Map(3 -> "fizz", 5 -> "buzz"))(probe.ref)
      val result: GetListReply = probe.expectMessageType[GetListReply]
      result.source
        .limit(100)
        .runWith(Sink.seq[String])
        .value
        .contains((1 to 100).map(translator(Map(3 -> "fizz", 5 -> "buzz"))))
    }
  }

  "Cmd GetTopHit" should {

    val actorRef: ActorRef[Command] = testKit.spawn(WorkerActor())

    "return with state empty" in {
      actorRef ! GetTopHit()(probe.ref)
      val result: GetTopHitReply = probe.expectMessageType[GetTopHitReply]
      assertResult(None)(result.top)
    }

    "return with state non empty" in {
      val dic1 = Map(1 -> "a", 2 -> "b")
      val dic2 = Map(3 -> "c", 2 -> "b")

      // First
      actorRef ! GetList(dic1)(probe.ref)
      probe.receiveMessage()
      actorRef ! GetTopHit()(probe.ref)
      val result: GetTopHitReply = probe.expectMessageType[GetTopHitReply]
      assertResult(Some(StatLine(dic1, 1)))(result.top)

      // sec
      actorRef ! GetList(dic2)(probe.ref)
      actorRef ! GetList(dic2)(probe.ref)
      probe.receiveMessages(2)
      actorRef ! GetTopHit()(probe.ref)
      val result2: GetTopHitReply = probe.expectMessageType[GetTopHitReply]
      assertResult(Some(StatLine(dic2, 2)))(result2.top)
    }
  }
}
