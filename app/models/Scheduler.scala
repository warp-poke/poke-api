package models

import akka.actor.{Actor, ActorRef, ActorSystem}
import javax.inject._
import models.Scheduler._
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.Configuration
import org.apache.kafka.common.serialization.StringSerializer
import models.repositories.ServiceRepository
import models.StateAgent._
import models.entities.CompleteService
import models.entities.ActorMessages._
import models.entities.CompleteService
import models.entities.kind._
import models.repositories.ServiceRepository
import play.api.Configuration
import kamon.Kamon
import kamon.prometheus.PrometheusReporter

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

  case object HTTPTick extends Tick {
    val kind = HTTP
  }

  case object SSLTick extends Tick {
    val kind = SSL
  }

  case object DNSTick extends Tick {
    val kind = DNS
  }

  case object ICMPTick extends Tick {
    val kind = ICMP
  }
}

@Singleton
class Scheduler @Inject() (
  sr: ServiceRepository,
  or: OrderService,
  actorSystem: ActorSystem,
  conf: Configuration,
  @Named("globalstate") globalState: ActorRef,
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
    case tick: Tick => globalState ! GetAllServices(tick)
    case AllServices(services, tick) => handleChecks(services.toList.map(_._2), tick)
  }

  def handleChecks(services: List[CompleteService], tick: Tick) = {
    val orders = services.flatMap(service => or.getOrders(service, tick.kind))

    tick match {
      case HTTPTick => {
        Kamon.gauge("scheduler_http_checks").increment()
        val buckets = Scheduler.bucketize(orders, Math.floor(httpInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, httpTopic, 1.second)
      }
      case SSLTick => {
        Kamon.gauge("scheduler_ssl_checks").increment()
        val buckets = Scheduler.bucketize(orders, Math.floor(sslInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, sslTopic, 1.second)
      }
      case DNSTick => {
        Kamon.gauge("scheduler_dns_checks").increment()
        val buckets = Scheduler.bucketize(orders, Math.floor(dnsInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, dnsTopic, 1.second)
      }
      case ICMPTick => {
        Kamon.gauge("scheduler_icmp_checks").increment()
        val buckets = Scheduler.bucketize(orders, Math.floor(icmpInterval * 0.9).toInt)
        schedulingActor ! OrderBuckets(buckets, icmpTopic, 1.second)
      }
    }
  }
}

