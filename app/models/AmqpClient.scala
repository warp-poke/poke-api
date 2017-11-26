package models

import javax.inject._
import play.api.Configuration
import play.api.Logger
import play.api.libs.json._
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.AMQP.BasicProperties

@Singleton
class AmqpClient @Inject() (
  configuration: Configuration
) {
  val connectionAndChannel: (Connection, Channel) = {
    val connFactory = new ConnectionFactory();
    val prefix = "rabbitmq"
    connFactory.setAutomaticRecoveryEnabled(true)
    connFactory.setHost(configuration.get[String](prefix + ".host"))
    connFactory.setPort(configuration.get[Int](prefix + ".port"))
    connFactory.setUsername(configuration.get[String](prefix + ".user"))
    connFactory.setPassword(configuration.get[String](prefix + ".password"))
    val co = connFactory.newConnection
    (co -> co.createChannel)
  }

  def sendMessageAsJson[A: Writes](
    exchange: String,
    routingKey: Option[String],
    message: A
  ): Unit = {
    val channel = connectionAndChannel._2
    val json = Json.toJson(message)
    val options = new BasicProperties.Builder().contentType("application/json").build
    channel.basicPublish(exchange, routingKey.getOrElse(""), options, Json.stringify(json).getBytes)
  }
}
