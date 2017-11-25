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

  def getOrders(cs: CompleteService): Seq[Order] = {
    val CompleteService(s, eps) = cs
    eps.flatMap(e => Seq(
      DnsOrder(
        labels = Map(),
        domain_name = e.domain_name
      ),
      HttpOrder(
        labels = Map(
          "service_id" -> s.service_id.toString,
          "endpoint_id" -> e.endpoint_id.toString,
          "owner" -> s.owner
        ),
        url = e.url,
        checks = HttpChecks(
          latency = Some(HttpCheck(
            class_name = "poke.http.latency"
          )),
          status = Some(HttpCheck(
            class_name = "poke.http.status"
          ))
        )
      )
    ))
  }
}