package models

import play.api.libs.json._
import models.service._

case class HttpCheck(
  class_name: String,
  labels: Option[Map[String,String]]
)

case class HttpChecks(
  latency: Option[HttpCheck],
  status:  Option[HttpCheck]
)

case class HttpOrder(
  labels: Map[String,String],
  url: String,
  checks: HttpChecks
)

object Order {
  implicit val httpCheckFormat = Json.format[HttpCheck]
  implicit val httpChecksFormat = Json.format[HttpChecks]
  implicit val httpOrderFormat = Json.format[HttpOrder]
}