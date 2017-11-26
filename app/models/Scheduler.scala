package models

import javax.inject._
import scala.concurrent.ExecutionContext

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
  or: OrderService
)(implicit ec: ExecutionContext) {

  def httpTick(amqpClient: AmqpClient) = {
    // ToDo maintain list in agent to avoid contant reloadings
    val services = sr.listAll
    services.map(s => {
      val orders = s.flatMap(or.getHttpOrders)
      val buckets = toto.bucketize(orders, 50)
      amqpClient.sendMessageAsJson("checks.http", None, buckets)
    })
  }

  def dnsTick() = {
  }
}
