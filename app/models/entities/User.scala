package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import anorm._
import play.api.libs.json._

case class User(
  userId: UUID,
  email: String,
  hashed_password: String//,
  //createdAt: OffsetDateTime,
  //updatedAt: OffsetDateTime,
  //deletedAt: Option[OffsetDateTime]
)

object UserInstances {
  implicit val userWrites = Json.writes[User]

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val userEntity = PgEntity.gen[User]
}