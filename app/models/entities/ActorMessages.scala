package models.entities

import scala.concurrent.duration.FiniteDuration
import models.orders.Order

object ActorMessages {
  case class KafkaMessage(orders: Seq[Order], topic: String)
  case class OrderBuckets(buckets: Seq[Seq[Order]], topic: String, interval: FiniteDuration)
}
