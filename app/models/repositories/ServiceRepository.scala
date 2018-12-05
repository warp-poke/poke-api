package models.repositories

import java.util.UUID

import anorm._
import javax.inject.Inject
import models.entities.Service._
import models.entities.ServiceInstances._
import models.entities.User._
import models.entities.kind.HTTP
import models.entities.{Check, CompleteService, HTTPCheckParams, Service}
import models.http.ServiceCreationData
import pgentity.pg_entity._
import play.api.db.DBApi
import play.api.libs.json.Json
import utils.AnormTypesInstances._

import scala.concurrent.Future

@javax.inject.Singleton
class ServiceRepository @Inject()(dbapi: DBApi)(implicit ec: models.DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val checkColumns = columns[Check].map(c => s""""${tableName[Check]}".${c.name}""").mkString(",")

  private def completeParser = parser[Service]() ~ parser[Check]()
  private def gatherResults(lines: Seq[Service~Check]): Seq[CompleteService] = {
    lines
      .map({ case (s ~ e) => s -> e})
      .groupBy(_._1)
      .map({ case (s, vs) => CompleteService(s, vs.map(_._2))})
      .toList
  }

  def listAll: Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    val x = s"""
      select ${columnList[Service](None)},
             ${columnList[Check](None)}
      from "${tableName[Service]}"
      inner join "${tableName[Check]}" using(service_id)
    """
    gatherResults(SQL(x).as(completeParser.*))
  })(ec)

  def list(userId: UserId): Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${columnList[Check](None)}
      from ${tableName[Service]}
      inner join "${tableName[Check]}" using(service_id)
      where user_id = {userId}::uuid
    """
    gatherResults(
      SQL(query)
        .on(
          'userId -> userId.toString
        )
        .as(completeParser.*)
    )
  })(ec)

  def get(serviceId: ServiceId, userId: UserId): Future[Option[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${columnList[Check](None)}
      from ${tableName[Service]}
      inner join "${tableName[Check]}" using(service_id)
      where service_id = {serviceId}::uuid and user_id = {userId}::uuid
    """
    gatherResults(
      SQL(query)
        .on(
          'serviceId -> serviceId.toString,
          'userId -> userId.toString
        )
        .as(completeParser.*)
    ).headOption
  })(ec)

  def create(userId: UserId, data: ServiceCreationData): Future[CompleteService] = Future(db.withTransaction { implicit connection =>
    val serviceId = UUID.randomUUID()
    val service = Service(serviceId, userId, data.domain, data.name)
    val checks = data.checks.map({ check => {
      val params = check.kind match {
        case HTTP => Some(HTTPCheckParams(check.secure.getOrElse(true), check.path.getOrElse("/")))
        case _ => None
      }
      Check(UUID.randomUUID(), serviceId, check.name, check.kind, params)
    }})

    println(insertSQL[Service](serviceEntity))
    SQL(insertSQL[Service](serviceEntity)).on(
      'service_id -> service.service_id,
      'user_id    -> service.user_id,
      'domain     -> service.domain,
      'name       -> service.name
    ).execute()

    println(insertSQL[Check](checkEntity))
    checks.foreach(check => SQL(insertSQL[Check](checkEntity)).on(
      'check_id   -> check.check_id,
      'service_id -> check.service_id,
      'kind       -> check.kind,
      'name       -> check.name,
      'params     -> Json.toJson(check.params)
    ).execute())

    CompleteService(service, checks)
  })(ec)

  def update(userId: UserId, serviceId: ServiceId, data: ServiceCreationData): Future[CompleteService] = Future(db.withTransaction { implicit connection =>
    val service = Service(serviceId, userId, data.domain, data.name)
    val checks = data.checks.map({ check => {
      val params = check.kind match {
        case HTTP => Some(HTTPCheckParams(check.secure.getOrElse(true), check.path.getOrElse("/")))
        case _ => None
      }
      Check(UUID.randomUUID(), serviceId, check.name, check.kind, params)
    }})

    val deleteChecksQuery = s"""
      delete from "${tableName[Check]}"
      where service_id = {serviceId}::uuid
    """
    SQL(deleteChecksQuery)
      .on(
        'serviceId -> serviceId.toString,
        'userId -> userId.toString
      )
      .execute()

    SQL(updateSQL[Service]()(serviceEntity)).on(
      'service_id -> serviceId.toString,
      'user_id    -> userId.toString,
      'domain     -> data.domain,
      'name       -> data.name
    ).execute()

    checks.foreach(check => SQL(insertSQL[Check](checkEntity)).on(
      'check_id   -> check.check_id,
      'service_id -> check.service_id,
      'kind       -> check.kind,
      'name       -> check.name,
      'params     -> Json.toJson(check.params)
    ).execute())

    CompleteService(service, checks)
  })(ec)

  def delete(serviceId: ServiceId, userId: UserId): Future[Unit] = Future(db.withTransaction { implicit connection =>
    val deleteChecksQuery = s"""
      delete from "${tableName[Check]}"
      where service_id = {serviceId}::uuid
    """
    SQL(deleteChecksQuery)
      .on(
        'serviceId -> serviceId.toString,
        'userId -> userId.toString
      )
      .execute()
    val deleteServiceQuery = s"""
      delete from ${tableName[Service]}
      where service_id = {serviceId}::uuid and user_id = {userId}::uuid
    """
    SQL(deleteServiceQuery)
      .on(
        'serviceId -> serviceId.toString,
        'userId -> userId.toString
      )
      .execute()
    ()
  })(ec)

}
