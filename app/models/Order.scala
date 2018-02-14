package models.orders

import play.api.libs.json._

sealed trait Order

case class HttpOrder(
  url: String,
  warp10_endpoint: String,
  token: String
) extends Order


object Order {
  val httpOrderFormat = Json.format[HttpOrder]
  implicit val orderWrites = new Writes[Order] {
    def writes(o: Order) = o match {
      case ho: HttpOrder => httpOrderFormat.writes(ho)
    }
  }
}