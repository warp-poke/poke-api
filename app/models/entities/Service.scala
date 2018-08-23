package models.entities

import java.util.UUID
import java.time.OffsetDateTime

import anorm._
import anorm.SqlParser._

import pgentity.pg_entity._

import play.api.libs.json._

import models.enums._
import models.entities.User.UserId

case class Service(
  service_id: Service.ServiceId,
  user_id: UserId,
  domain: String,
  name: Option[String]
)

case class Check(
  check_id: Service.CheckId,
  service_id: Service.ServiceId,
  secure: Boolean,
  path: String,
  name: Option[String]
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

  implicit val checkEntity = new PgEntity[Check] {
    val tableName = "check"
    val columns = List(
      PgField("check_id", Some("uuid")), PgField("service_id", Some("uuid")),
      PgField("secure"), PgField("path"), PgField("name")
    )
    def parser(prefix: String): RowParser[Check] = {
      get[UUID](prefix + "check_id") ~
      get[UUID](prefix + "service_id") ~
      get[Boolean](prefix + "secure") ~
      get[String](prefix + "path") ~
      get[Option[String]](prefix + "name") map {
        case id ~ service ~ secure ~ path ~ name => Check(
          id, service, secure, path, name
        )
      }
    }
  }
  implicit val serviceEntity = new PgEntity[Service] {
    val tableName = "service"
    val columns = List(
      PgField("service_id", Some("uuid")), PgField("user_id", Some("uuid")),
      PgField("domain"), PgField("name")
    )
    def parser(prefix: String): RowParser[Service] = {
      get[UUID](prefix + "service_id") ~
      get[UUID](prefix + "user_id") ~
      get[String](prefix + "domain") ~
      get[Option[String]](prefix + "name") map {
        case id ~ user ~ domain ~ name => Service(
          id, user, domain, name
        )
      }
    }
  }
}
