package models.repositories

import java.util.UUID

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future
import pgentity.pg_entity._
import pgentity.pg_entity.Entities._

import models.entities.{Check,Service,CompleteService}
import models.entities.User._
import models.entities.Service._
import models.entities.ServiceInstances._

import models.http.ServiceCreationData

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
    val service = Service(serviceId, userId, data.domain)
    val checks = data.checks.map({ check => Check(UUID.randomUUID(), serviceId, check.secure, check.path) })

    SQL(insertSQL[Service](serviceEntity)).on(
      'service_id -> service.service_id,
      'user_id    -> service.user_id,
      'domain     -> service.domain
    ).execute()

    checks.foreach(check => SQL(insertSQL[Check](checkEntity)).on(
      'check_id   -> check.check_id,
      'service_id -> check.service_id,
      'path       -> check.path,
      'secure     -> check.secure
    ).execute())

    CompleteService(service, checks)
  })(ec)

  def delete(serviceId: ServiceId, userId: UserId): Future[Unit] = Future(db.withTransaction { implicit connection =>
    val deleteChecksQuery = s"""
      delete from "${tableName[Check]}"
      where service_id = {serviceId}::uuid and user_id = {userId}::uuid
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