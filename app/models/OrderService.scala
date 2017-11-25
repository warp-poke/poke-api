package models

import java.util.Properties
import org.nats._

import javax.inject._
import play.api.libs.json._

import service._


class OrderService @Inject() (warp10: Warp10) {
  var conn = Conn.connect(new Properties)

  def sendMessage[A](subject: String, msg: A)(implicit w: Writes[A]): Unit = {
    conn.publish(subject, Json.stringify(Json.toJson(msg)))
  }

/*
  def sendOrder(cs: CompleteService) = {
    val CompleteService(s, eps) = cs
    val order = Order(
      eps.head.domain_name,
      warp10.deliverWriteToken(s.service_id.toString),
      eps.head.service_check
    )
  }*/
}