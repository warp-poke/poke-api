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
import models.entities.ServiceInstances._


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
             ${checkColumns}
      from "${tableName[Service]}"
      inner join "${tableName[Check]}" using(service_id)
    """
  
    gatherResults(SQL(x).as(completeParser.*))
  })(ec)

  def list(userId: UUID): Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${checkColumns}
      from ${tableName[Service]}
      inner join ${tableName[Check]} using(service_id)
      where user_id = {userId}
    """
    gatherResults(
      SQL(query)
        .on(
          'userId -> userId.toString
        )
        .as(completeParser.*)
    )
  })(ec)

  def get(serviceId: UUID, userId: UUID): Future[Option[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${checkColumns}
      from ${tableName[Service]}
      inner join ${tableName[Check]} using(service_id)
      where service_id = {serviceId} and user_id = {userId}
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

  def delete(serviceId: UUID, userId: UUID): Future[Unit] = Future(db.withConnection { implicit connection =>
    var query = s"""
      delete from ${tableName[Service]}
      where service_id = {serviceId} and user_id = {userId}
    """
    SQL(query)
      .on(
        'serviceId -> serviceId.toString,
        'userId -> userId.toString
      )
    ()
  })(ec)

}