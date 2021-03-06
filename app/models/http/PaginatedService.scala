package models.http

import play.api.libs.json._

import models.entities.CompleteService
import models.entities.ServiceInstances.completeServiceWrites

case class PaginatedService(
  items: Seq[CompleteService]
)

object PaginatedServiceInstances {
  implicit val psWrites = Json.writes[PaginatedService]
}