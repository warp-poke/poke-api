package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import anorm._
import play.api.libs.json._

case class Service(
  serviceId: UUID,
  userId: UUID,
  domain: String//,
  //createdAt: OffsetDateTime,
  //updatedAt: OffsetDateTime,
  //deletedAt: Option[OffsetDateTime]
)

case class HttpCheck(
  checkId: UUID,
  serviceId: UUID,
  path: String//,
  //headers: Map[String,String],
  //createdAt: OffsetDateTime,
  //updatedAt: OffsetDateTime,
  //deletedAt: Option[OffsetDateTime]
)

object ServiceInstances {
  implicit val checkWrites = Json.writes[HttpCheck]
  implicit val serviceWrites = Json.writes[Service]

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val httpCheckEntity = PgEntity.gen[HttpCheck]
  implicit val serviceEntity = PgEntity.gen[Service]
}