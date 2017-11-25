package models

import javax.inject._
import play.api.Configuration
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

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
}
