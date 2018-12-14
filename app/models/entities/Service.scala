package models.entities

import java.util.UUID

import anorm.SqlParser._
import anorm._
import models.entities.User.UserId
import models.entities.kind._
import models.enums.{EnumAdt, _}
import pgentity.pg_entity._
import play.api.libs.json._
import utils.AnormTypesInstances._

case class Service(
  service_id: Service.ServiceId,
  user_id: UserId,
  domain: String,
  name: Option[String]
)

sealed trait Params

case class HTTPCheckParams(
  val secure: Boolean,
  val path: String,
) extends Params

object kind {
  sealed trait Kind

  case object SSL extends Kind
  case object DNS extends Kind
  case object HTTP extends Kind
  case object ICMP extends Kind

  implicit val kindEnum = new EnumAdt[Kind] {
    val values = List(SSL, DNS, HTTP, ICMP)
    def valueAsString(x: Kind)  = x.toString
  }

  implicit val kindToStatement = toStatement[Kind]
  implicit val kindFromColumn  = fromColumn[Kind]

  implicit val kindWrites = jsonWrites[Kind]
  implicit val kindReads = jsonReads[Kind]
}

case class Check(
  check_id: Service.CheckId,
  service_id: Service.ServiceId,
  name: Option[String],
  kind: Kind,
  params: Option[Params]
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
  implicit val httpCheckParamsWrites = Json.writes[HTTPCheckParams]
  implicit val paramsWrites = Json.writes[Params]
  implicit val checkWrites = Json.writes[Check]
  implicit val serviceWrites = Json.writes[Service]
  implicit val completeServiceWrites = Json.writes[CompleteService]

  implicit val httpCheckParamsReads = Json.reads[HTTPCheckParams]
  implicit val paramsReads = Json.reads[Params]
  implicit val checkReads = Json.reads[Check]
  implicit val serviceReads = Json.reads[Service]
  implicit val completeServiceReads = Json.reads[CompleteService]

  implicit val checkEntity = new PgEntity[Check] {
    val tableName = "check"
    val columns = List(
      PgField("check_id", Some("uuid")), PgField("service_id", Some("uuid")),
      PgField("name"), PgField("kind", Some("kind")), PgField("params", Some("json"))
    )
    def parser(prefix: String): RowParser[Check] = {
      get[UUID](prefix + "check_id") ~
      get[UUID](prefix + "service_id") ~
      get[Option[String]](prefix + "name") ~
      get[Kind](prefix + "kind") ~
      get[JsValue](prefix + "params") map {
        case id ~ service ~ name ~ kind ~ params => {
          Check(id, service, name, kind, kind match {
            case HTTP => Some(params.as[HTTPCheckParams])
            case _ => None
          })
        }
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
