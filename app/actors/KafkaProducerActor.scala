package actors

import javax.inject._
import play.api.Configuration
import play.api.libs.json._
import akka.actor._
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.KafkaProducer.Conf
import cakesolutions.kafka.KafkaProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import models.entities.ActorMessages._
import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import models.orders.Order // TODO: remove me

class KafkaProducerActor @Inject() (
  val conf: Configuration
) extends Actor {

  def receive = {
    case KafkaMessage(orders, topic) => orders.foreach(o => send(o, topic))
  }

  val producer = KafkaProducer(Conf(
    props = Map(
      "bootstrap.servers" -> conf.get[String]("kafka.bootstrap.servers"),
      "security.protocol" -> conf.get[String]("kafka.security.protocol"),
      "sasl.mechanism" -> "PLAIN"
    ),
    new StringSerializer(),
    new StringSerializer()
  ))

  def send(order: Order, topic: String): Unit = {
    Kamon.gauge("scheduler_kafka_producer_send").increment()
    producer.send(KafkaProducerRecord[String, String](
      topic,
      Json.toJson(order).toString
    ))
  }
}
