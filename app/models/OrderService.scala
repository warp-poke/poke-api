package models


import javax.inject._
import play.api.libs.json._

import service._


class OrderService @Inject() (warp10: Warp10) {
  def getHttpOrders(cs: CompleteService): Seq[HttpOrder] = {
    val CompleteService(s, eps) = cs
    eps.flatMap(e => Seq(
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