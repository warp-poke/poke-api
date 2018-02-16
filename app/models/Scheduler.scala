package models

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.Logger
import akka.actor.{Actor, ActorRef, ActorSystem}
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.Configuration
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.KafkaProducer.Conf
import cakesolutions.kafka.KafkaProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import models.repositories.ServiceRepository
import models.ServicesAgent._
import models.entities.CompleteService

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
  @Named("services-state") servicesState: ActorRef
)(implicit ec: ExecutionContext) extends Actor {

  import Scheduler._

  val producer = KafkaProducer(Conf(
    props = Map(
      "bootstrap.servers" -> conf.get[String]("kafka.bootstrap.servers"),
      "security.protocol" -> conf.get[String]("kafka.security.protocol"),
      "sasl.mechanism" -> "PLAIN"
    ),
    new StringSerializer(),
    new StringSerializer()
  ))
  val httpTopic = conf.get[String]("kafka.httpchecks.topic")

  def receive = {
    case HttpTick => httpTick()
    case AllServices(services) => handleHttpChecks(services.toList.map(_._2))
  }

  def httpTick() = servicesState ! GetAllServices

  def handleHttpChecks(services: List[CompleteService]) = {
    val orders = services.flatMap(or.getOrders)
    val buckets = toto.bucketize(orders, 50)
    Logger.debug(s"${buckets.length} batchs to send..")
    sendBatch(buckets)
  }

  def dnsTick() = {
  }

  def sendBatch[A: Writes](buckets: Seq[Seq[A]]): Unit = {
    actorSystem.scheduler.scheduleOnce(1.seconds) {
      producer.send(KafkaProducerRecord[String, String](
        httpTopic,
        Json.toJson(buckets.head).toString)
      )
      if(buckets.length > 1) {
        sendBatch(buckets.tail)
      } else {
        Logger.debug("All batchs sent")
      }
    }
  }
}

object Scheduler {
  case object HttpTick
}