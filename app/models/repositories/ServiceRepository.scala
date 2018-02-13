package models.repositories

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future
import pgentity.pg_entity._
import pgentity.pg_entity.Entities._

import models.entities.{HttpCheck,Service,CompleteService}
import models.entities.ServiceInstances._


@javax.inject.Singleton
class ServiceRepository @Inject()(dbapi: DBApi)(implicit ec: models.DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def listAll: Future[Seq[CompleteService]] = Future(db.withConnection { implicit connection =>
    val x = s"""
      select ${columnList[Service](None)},
             ${columnList[HttpCheck](None)}
      from ${tableName[Service]}
      inner join ${tableName[HttpCheck]} using(service_id)
    """
    SQL(x)
      .as((parser[Service]() ~ parser[HttpCheck]()).*)
      .map({ case (s ~ e) => s -> e})
      .groupBy(_._1)
      .map({ case (s, vs) => CompleteService(s, vs.map(_._2))})
      .toList
  })(ec)

}