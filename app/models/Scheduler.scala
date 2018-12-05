package models

import akka.actor.{Actor, ActorRef, ActorSystem}
import javax.inject._
import models.Scheduler._
import models.ServicesAgent._
import models.entities.ActorMessages._
import models.entities.CompleteService
import models.entities.kind._
import models.repositories.ServiceRepository
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Scheduler {
  def bucketize[A](elems: Seq[A], buckets: Int): Seq[Seq[A]] = {
    elems.zipWithIndex
      .groupBy(_._2 % buckets)
      .toList
      .map(_._2.map(x => x._1))
  }

  sealed trait Tick {
    def kind: Kind
  }

  case class HTTPTick() extends Tick {
    val kind = HTTP
  }

  case class SSLTick() extends Tick {
    val kind = SSL
  }

  case class DNSTick() extends Tick {
    val kind = DNS
  }

  case class ICMPTick() extends Tick {
    val kind = ICMP
  }
}

@Singleton
class Scheduler @Inject() (
  sr: ServiceRepository,
  or: OrderService,
  actorSystem: ActorSystem,
  conf: Configuration,
  @Named("services-state") servicesState: ActorRef,
  @Named("scheduling-actor") schedulingActor: ActorRef
)(implicit ec: ExecutionContext) extends Actor {

  val httpTopic = conf.get[String]("kafka.httpchecks.topic")
  val sslTopic = conf.get[String]("kafka.sslchecks.topic")
  val dnsTopic = conf.get[String]("kafka.dnschecks.topic")
  val icmpTopic = conf.get[String]("kafka.icmpchecks.topic")

  val httpInterval = conf.get[Int]("kafka.httpchecks.interval")
  val sslInterval = conf.get[Int]("kafka.sslchecks.interval")
  val dnsInterval = conf.get[Int]("kafka.dnschecks.interval")
  val icmpInterval = conf.get[Int]("kafka.icmpchecks.interval")

  def receive = {
    case tick: Tick => servicesState ! GetAllServices(tick)
    case AllServices(services, tick) => handleChecks(services.toList.map(_._2), tick)
  }

  def handleChecks(services: List[CompleteService], tick: Tick) = {
    val orders = services.flatMap(service => or.getOrders(service, tick.kind))

    tick match {
      case _: HTTPTick => {
        val buckets = Scheduler.bucketize(orders, Math.floor(httpInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, httpTopic, 1.second)
      }
      case _: SSLTick => {
        val buckets = Scheduler.bucketize(orders, Math.floor(sslInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, sslTopic, 1.second)
      }
      case _: DNSTick => {
        val buckets = Scheduler.bucketize(orders, Math.floor(dnsInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, dnsTopic, 1.second)
      }
      case _: ICMPTick => {
        val buckets = Scheduler.bucketize(orders, Math.floor(icmpInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, icmpTopic, 1.second)
      }
    }
  }
}

