package fr.sysf.sample.actor

import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.Source
import fr.sysf.sample.actor.WorkerActor._

/**
  * Companion object to implement Worker Actor
  * I/O description
  */
object WorkerActor {

  type Dictionary = Map[Int, String]

  // command
  sealed trait Command
  final case class GetList(dictionary: Dictionary = Map.empty)(val replyTo: ActorRef[GetListReply]) extends Command
  final case class GetTopHit()(val replyTo: ActorRef[GetTopHitReply]) extends Command

  // reply
  sealed trait Reply
  final case class GetListReply(source: Source[String, NotUsed]) extends Reply
  final case class GetTopHitReply(top: Option[StatLine]) extends Reply

  // state
  final case class StatLine(dictionary: Dictionary, count: Long)
  final case class Stat(lines: List[StatLine] = List.empty) {
    def applyEvent(d: Dictionary): Stat =
      copy((StatLine(d, lines.find(_.dictionary == d).map(_.count + 1).getOrElse(1)) :: lines.filterNot(_.dictionary == d)).sortWith(_.count > _.count))
  }

  def translator(dictionary: Dictionary)(i: Int): String =
    dictionary.toList
      .sortBy(_._1)
      .flatMap(c ⇒ if (i % c._1 == 0) Some(c._2) else None)
      .reduceLeftOption((a: String, b: String) ⇒ s"$a$b")
      .getOrElse(i.toString)

  def apply(): Behavior[Command] = Behaviors.setup(ctx ⇒ new WorkerActor(ctx))
}

/**
  * Worker Actor
  */
class WorkerActor(ctx: ActorContext[Command]) extends AbstractBehavior[Command] {

  var statistic = Stat()

  override def onMessage(cmd: Command): Behavior[Command] = cmd match {

    case cmd: GetList ⇒
      ctx.log.debug("GetList( {} )", cmd.dictionary)
      val source: Source[String, NotUsed] = Source
        .repeat(1)
        .scan(1)(_ + _)
        .map(translator(cmd.dictionary))
      cmd.replyTo ! GetListReply(source)
      statistic = statistic.applyEvent(cmd.dictionary)
      Behaviors.same

    case cmd: GetTopHit ⇒
      ctx.log.debug("GetList()")
      cmd.replyTo ! GetTopHitReply(top = statistic.lines.headOption)
      Behaviors.same
  }
}
