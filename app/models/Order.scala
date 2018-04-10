package models.orders

import play.api.libs.json._

sealed trait Order {
  def domain_name: String
  def warp10_endpoint: String
  def token: String
}

case class HttpOrder(
  domain_name: String,
  url: String,
  warp10_endpoint: String,
  token: String
) extends Order

case class DNSOrder(
  domain_name: String,
  warp10_endpoint: String,
  token: String
) extends Order

case class SSLOrder(
  domain_name: String,
  warp10_endpoint: String,
  token: String
) extends Order

object Order {
  val httpOrderFormat = Json.format[HttpOrder]
  val sslOrderFormat = Json.format[SSLOrder]
  val dnsOrderFormat = Json.format[DNSOrder]

  implicit val orderWrites = new Writes[Order] {
    def writes(o: Order) = o match {
      case httpOrder: HttpOrder => httpOrderFormat.writes(httpOrder)
      case sslOrder: SSLOrder => sslOrderFormat.writes(sslOrder)
      case dnsOrder: DNSOrder => dnsOrderFormat.writes(dnsOrder)
    }
  }
}