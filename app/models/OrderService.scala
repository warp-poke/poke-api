package models


import javax.inject._
import models.entities._
import models.entities.kind._
import models.orders._
import kamon.Kamon
import kamon.prometheus.PrometheusReporter


class OrderService @Inject() (config: Config, warp10: Warp10) {
  def getOrders(cs: CompleteService, kind: Kind): Seq[Order] = {
    val CompleteService(service, checks) = cs
    checks
      .filter(check => check.kind == kind)
      .map(checkToOrder(service))
  }

  def checkToOrder(service: Service)(check: Check): Order = {
    val labels = Map(
      "service_id" -> service.service_id.toString,
      "check_id" -> check.check_id.toString,
      "owner_id" -> service.user_id.toString
    )

    val token = warp10.deliverWriteToken(labels, service.user_id)
    check.kind match {
      case HTTP => {
        Kamon.gauge("scheduler_http_orders").increment()
        val params = check.params match {
          case Some(x: HTTPCheckParams) => x
          case _ => HTTPCheckParams(true, "/")
        }

        val scheme = if(params.secure) "https" else "http"
        HttpOrder(
          domain_name = service.domain,
          url = s"${scheme}://${service.domain}${params.path}",
          warp10_endpoint = config.warp10.endpoint,
          token = token
        )
      }
      case SSL => {
        Kamon.gauge("scheduler_ssl_orders").increment()
        SSLOrder(
          domain_name = service.domain,
          warp10_endpoint = config.warp10.endpoint,
          token = token
        )
      }
      case DNS => {
        Kamon.gauge("scheduler_dns_orders").increment()
        DNSOrder(
          domain_name = service.domain,
          warp10_endpoint = config.warp10.endpoint,
          token = token
        )
      }
      case ICMP => {
        Kamon.gauge("scheduler_icmp_orders").increment()
        ICMPOrder(
          domain_name = service.domain,
          warp10_endpoint = config.warp10.endpoint,
          token = token
        )
      }
    }
  }
}