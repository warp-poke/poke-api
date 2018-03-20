package actors

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka._
import akka.actor._
import models.entities.ActorMessages._
import models.orders.Order

class SchedulingActor @Inject() (
  val system: ActorSystem,
  implicit val ec: ExecutionContext,
  @Named("kafka-producer") kafkaProducer: ActorRef
) extends Actor {

  def receive = {
    case OrderBuckets(buckets, topic, interval) => delay(buckets, topic, interval)
  }

  def delay(buckets: Seq[Seq[Order]], topic: String, interval: FiniteDuration): Unit = {
    buckets.zipWithIndex foreach { case(b, i) =>
      system.scheduler.scheduleOnce(interval * i) {
        kafkaProducer ! KafkaMessage(b, topic) 
      }
    }
  }
}
