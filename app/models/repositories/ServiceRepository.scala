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

  def listAll: Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    val x = s"""
      select ${columnList[Service](None)},
             ${checkColumns}
      from "${tableName[Service]}"
      inner join "${tableName[Check]}" using(service_id)
    """
    SQL(x)
      .as((parser[Service]() ~ parser[Check]()).*)
      .map({ case (s ~ e) => s -> e})
      .groupBy(_._1)
      .map({ case (s, vs) => CompleteService(s, vs.map(_._2))})
      .toList
  })(ec)

  def list(userId: UUID): Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${columnList[Check](None)}
      from ${tableName[Service]}
      inner join ${tableName[Check]} using(service_id)
      where user_id = ${userId.toString}
    """
    SQL(query)
      .as((parser[Service]() ~ parser[Check]()).*)
      .map({ case (s ~ e) => s -> e})
      .groupBy(_._1)
      .map({ case (s, vs) => CompleteService(s, vs.map(_._2))})
      .toList
  })(ec)

  def get(serviceId: UUID): Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    var query = s"""
      select ${columnList[Service](None)},
             ${columnList[Check](None)}
      from ${tableName[Service]}
      inner join ${tableName[Check]} using(service_id)
      where service_id = ${serviceId.toString}
    """
    SQL(query)
      .as((parser[Service]() ~ parser[Check]()).*)
      .map({ case (s ~ e) => s -> e})
      .groupBy(_._1)
      .map({ case (s, vs) => CompleteService(s, vs.map(_._2))})
      .toList
  })(ec)

  def delete(serviceId: UUID): Future[Unit] = Future(db.withConnection { implicit connection =>
    var query = s"""
      delete from ${tableName[Service]}
      where service_id = ${serviceId.toString}
    """
    SQL(query)
    ()
  })(ec)

}