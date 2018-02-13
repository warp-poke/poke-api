package models


import javax.inject._
import play.api.libs.json._

import models.entities._
import models.orders._


class OrderService @Inject() (warp10: Warp10) {
  def getHttpOrders(cs: CompleteService): Seq[HttpOrder] = {
    val CompleteService(s, eps) = cs
    eps.flatMap(e => Seq(
      HttpOrder(
        labels = Map(
          "service_id" -> s.serviceId.toString,
          "endpoint_id" -> e.checkId.toString,
          "owner" -> s.userId.toString
        ),
        url = s.domain,
        checks = HttpChecks(
          latency = Some(HttpCheckOrder(
            class_name = "poke.http.latency"
          )),
          status = Some(HttpCheckOrder(
            class_name = "poke.http.status"
          ))
        )
      )
    ))
  }
}