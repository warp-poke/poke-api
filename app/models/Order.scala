package models

import play.api.libs.json._
import models.service._

case class HttpCheck(
  class_name: String,
  labels: Option[Map[String,String]] = None
)

case class HttpChecks(
  latency: Option[HttpCheck],
  status:  Option[HttpCheck]
)

sealed trait Order

case class HttpOrder(
  labels: Map[String,String],
  url: String,
  checks: HttpChecks
) extends Order

case class DnsOrder(
  labels: Map[String,String],
  domain_name: String
) extends  Order

object Order {
  implicit val httpCheckFormat = Json.format[HttpCheck]
  implicit val httpChecksFormat = Json.format[HttpChecks]

  val httpOrderFormat = Json.format[HttpOrder]
  val dnsOrderFormat = Json.format[DnsOrder]

  implicit val orderWrites = new Writes[Order] {
    def writes(o: Order) = o match {
      case ho: HttpOrder => httpOrderFormat.writes(ho)
      case dnso: DnsOrder => dnsOrderFormat.writes(dnso)
    }
  }
}