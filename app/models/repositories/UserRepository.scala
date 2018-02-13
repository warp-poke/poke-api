package models.repositories

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future
import pgentity.pg_entity._
import pgentity.pg_entity.Entities._

import models.entities.User
import models.entities.UserInstances._


@javax.inject.Singleton
class UserRepository @Inject()(dbapi: DBApi)(implicit ec: models.DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val userColumns = columns[User].map(c => s""""${tableName[User]}".${c.name}""").mkString(",")

  def getUserByEmail(email: String): Future[Option[User]] = Future(db.withConnection { implicit connection =>
    val x = s"""
      select ${userColumns}
      from "${tableName[User]}"
      where "user".email = {email}
    """
    SQL(x)
      .on('email -> email)
      .as(parser[User]().*)
      .headOption
  })(ec)

}