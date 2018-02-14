package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import anorm._
import play.api.libs.json._

case class Service(
  service_id: UUID,
  user_id: UUID,
  domain: String
)

case class Check(
  check_id: UUID,
  service_id: UUID,
  path: String
)

case class CompleteService(
  service: Service,
  checks: Seq[Check]
)

object ServiceInstances {
  implicit val checkWrites = Json.writes[Check]
  implicit val serviceWrites = Json.writes[Service]

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val httpCheckEntity = PgEntity.gen[Check]
  implicit val serviceEntity = PgEntity.gen[Service]
}