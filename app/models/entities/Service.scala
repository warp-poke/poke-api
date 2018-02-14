package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import anorm._
import play.api.libs.json._

import models.entities.User.UserId

case class Service(
  service_id: Service.ServiceId,
  user_id: UserId,
  domain: String
)

case class Check(
  check_id: Service.CheckId,
  service_id: Service.ServiceId,
  path: String
)

case class CompleteService(
  service: Service,
  checks: Seq[Check]
)

object Service {
  type CheckId = UUID
  type ServiceId = UUID
}

object ServiceInstances {
  implicit val checkWrites = Json.writes[Check]
  implicit val serviceWrites = Json.writes[Service]
  implicit val completeServiceWrites = Json.writes[CompleteService]

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val httpCheckEntity = PgEntity.gen[Check]
  implicit val serviceEntity = PgEntity.gen[Service]
}