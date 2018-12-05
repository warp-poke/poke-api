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

case class ICMPOrder(
 domain_name: String,
 warp10_endpoint: String,
 token: String
) extends Order

object Order {
  val httpOrderFormat = Json.format[HttpOrder]
  val dnsOrderFormat = Json.format[DNSOrder]
  val sslOrderFormat = Json.format[SSLOrder]
  implicit val orderWrites = new Writes[Order] {
    def writes(o: Order) = o match {
      case o: HttpOrder => httpOrderFormat.writes(o)
      case o: DNSOrder => dnsOrderFormat.writes(o)
      case o: SSLOrder => sslOrderFormat.writes(o)
    }
  }
}
