package models

import anorm._
import anorm.SqlParser._
import anorm.ToStatement
import anorm.ToStatement.uuidToStatement
import anorm.ParameterValue.toParameterValue
import java.util.UUID
import pgentity.pg_entity._
import pgentity.pg_entity.Entities._
import magnolia._
import play.api.libs.json._

import models.enums._

object service {
  sealed trait ServiceState { def name = this.toString }

  case object ACTIVE extends ServiceState
  case object INACTIVE extends ServiceState

  object ServiceState {
    val values = List(ACTIVE,INACTIVE)
    def unapply(s: String) = values.find(_.name == s.toLowerCase)
  }

  implicit val serviceStateEnum = new EnumAdt[ServiceState] {
    def values = ServiceState.values
    def valueAsString(x: ServiceState) = x.name
  }
  implicit val serviceStateToStatement = toStatement[ServiceState]
  implicit val serviceStateFromColumn = fromColumn[ServiceState]
  implicit val serviceStateWrites = jsonWrites[ServiceState]
  implicit val serviceStateReads = jsonReads[ServiceState]

  sealed trait ServiceCheck { def name = this.toString }

  case object HTTP_ONLY extends ServiceCheck
  case object HTTPS_ONLY extends ServiceCheck
  case object BOTH extends ServiceCheck

  object ServiceCheck {
    val values = List(HTTP_ONLY, HTTPS_ONLY, BOTH)
    def unapply(s: String) = values.find(_.name == s.toLowerCase)
  }

  implicit val serviceCheckEnum = new EnumAdt[ServiceCheck] {
    def values = ServiceCheck.values
    def valueAsString(x: ServiceCheck) = x.name
  }
  implicit val serviceCheckToStatement = toStatement[ServiceCheck]
  implicit val serviceCheckFromColumn = fromColumn[ServiceCheck]
  implicit val serviceCheckWrites = jsonWrites[ServiceCheck]
  implicit val serviceCheckReads = jsonReads[ServiceCheck]


  type ServiceId = UUID

  case class Service(
    service_id: ServiceId,
    ref: Option[String],
    owner: String,
    delegated_owner: Option[String],
    state: ServiceState,
    tags: List[String]
  )

  implicit val uuidEntity = PgEntity.columnToPgEntity[UUID]("uuid")
  implicit val serviceStateEntity = PgEntity.columnToPgEntity[ServiceState]("ServiceState")
  implicit val strListEntity = PgEntity.columnToPgEntity[List[String]]("text[]")
  implicit def ostrEntity = PgEntity.columnToPgEntity[Option[String]]("text")
  implicit val serviceEntity = PgEntity.gen[Service]

  implicit val serviceFormat = Json.format[Service]

  type EndpointId = UUID
  case class Endpoint(
    endpoint_id: EndpointId,
    service_id: ServiceId,
    domain_name: String,
    url: String,
    service_check: ServiceCheck
  )

  implicit val serviceCheckEntity = PgEntity.columnToPgEntity[ServiceCheck]("ServiceCheck")
  implicit val endpointEntity = PgEntity.gen[Endpoint]
  implicit val endpointFormat = Json.format[Endpoint]

  case class CompleteService(s: Service, eps: Seq[Endpoint])
  implicit val completeServiceFormat = Json.format[CompleteService]
}
