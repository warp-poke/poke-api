package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import pgentity.pg_entity._
import anorm._
import anorm.SqlParser._
import play.api.libs.json._
import de.mkammerer.argon2._

case class User(
  user_id: User.UserId,
  email: String,
  hashed_password: String
)

object User {
  type UserId = UUID

  def hash(password: String): String =
    Argon2Factory.create().hash(2, 65536, 1, password.toCharArray())

  def verify(password: String, hashed: String): Boolean =
    Argon2Factory.create().verify(hashed, password)
}

object UserInstances {
  implicit val userWrites = Json.writes[User]

  implicit val userEntity = new PgEntity[User] {
    val tableName = "user"
    val columns = List(
      PgField("user_id", Some("uuid")), PgField("email"), PgField("hashed_password")
    )
    def parser(prefix: String): RowParser[User] = {
      get[UUID](prefix + "user_id") ~
      get[String](prefix + "email") ~
      get[String](prefix + "hashed_password") map {
        case id ~ email ~ pwd => User(id, email, pwd)
      }
    }
  }
}