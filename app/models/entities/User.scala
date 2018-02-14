package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import anorm._
import play.api.libs.json._
import de.mkammerer.argon2._

case class User(
  user_id: UUID,
  email: String,
  hashed_password: String
)

object UserInstances {
  implicit val userWrites = Json.writes[User]

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val userEntity = PgEntity.gen[User]

  def hash(password: String): String =
    Argon2Factory.create().hash(2, 65536, 1, password.toCharArray())

  def verify(password: String, hashed: String): Boolean =
    Argon2Factory.create().verify(hashed, password)
}