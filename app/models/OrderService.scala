package models


import javax.inject._
import play.api.libs.json._

import models.entities._
import models.orders._


class OrderService @Inject() (config: Config, warp10: Warp10) {
  def checkToOrder(service: Service)(check: Check): Order = {
    val labels = Map(
      "service_id" -> service.service_id.toString,
      "check_id" -> check.check_id.toString,
      "owner_id" -> service.user_id.toString
    )

    val token = warp10.deliverWriteToken(labels)

    val scheme = if(check.secure) "https" else "http"
    HttpOrder(
      domain_name = service.domain,
      url = s"${scheme}://${service.domain}${check.path}",
      warp10_endpoint = config.warp10.endpoint,
      token = token
    )
  }
  def getOrders(cs: CompleteService): Seq[Order] = {
    val CompleteService(service, checks) = cs
    checks.map(checkToOrder(service))
  }
}