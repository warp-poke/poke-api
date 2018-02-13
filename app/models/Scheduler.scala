package models

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.Logger
import akka.actor.ActorSystem
import scala.concurrent.duration._
import play.api.libs.json._

import models.repositories.ServiceRepository

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
  amqpClient: AmqpClient
)(implicit ec: ExecutionContext) {

  def httpTick() = {
    // ToDo maintain list in agent to avoid contant reloadings
    val services = sr.listAll
    services.map(s => {
      val orders = s.flatMap(or.getHttpOrders)
      toto.bucketize(orders, 50)
    }).map(buckets => {
      Logger.debug(s"${buckets.length} batchs to send..")
      sendBatch(buckets)
    })
  }

  def dnsTick() = {
  }

  def sendBatch[A: Writes](buckets: Seq[Seq[A]]): Unit = {
    actorSystem.scheduler.scheduleOnce(1.seconds) {
      amqpClient.sendMessageAsJson("checks.http", None, buckets.head)
      if(buckets.length > 1) {
        sendBatch(buckets.tail)
      } else {
        Logger.debug("All batchs sent")
      }
    }
  }
}
