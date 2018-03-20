package models

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Logger
import akka.actor.{Actor, ActorRef, ActorSystem}
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.Configuration
import org.apache.kafka.common.serialization.StringSerializer

import models.repositories.ServiceRepository
import models.ServicesAgent._
import models.entities.CompleteService
import models.entities.ActorMessages._

object toto {
  def bucketize[A](elems: Seq[A], buckets: Int): Seq[Seq[A]] = {
    elems.zipWithIndex
      .groupBy(_._2 % buckets)
      .toList
      .map(_._2.map(x => x._1))
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

  import Scheduler._

  val httpTopic = conf.get[String]("kafka.httpchecks.topic")

  def receive = {
    case HttpTick => httpTick()
    case AllServices(services) => handleHttpChecks(services.toList.map(_._2))
  }

  def httpTick() = servicesState ! GetAllServices

  def handleHttpChecks(services: List[CompleteService]) = {
    val orders = services.flatMap(or.getHttpOrders)
    val buckets = toto.bucketize(orders, 50)
    Logger.debug(s"${buckets.length} batchs to send..")
    schedulingActor ! OrderBuckets(buckets, httpTopic, 1.second)
  }

  def dnsTick() = {
  }

}

object Scheduler {
  case object HttpTick
}