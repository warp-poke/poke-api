package models.repositories

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

import play.api.db.DBApi

import anorm._
import anorm.SqlParser._
import pgentity.pg_entity._

import models.entities.Hook
import models.entities.HookInput
import models.entities.Hook.HookId
import models.entities.HookInstances._
import models.entities.User
import models.entities.User.UserId

@Singleton
class HookRepository @Inject()(dbapi: DBApi)(implicit val ec: models.DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def create(hook: HookInput): Future[Hook] = Future(db.withConnection { implicit connection =>
    val createdHook = Hook(UUID.randomUUID, hook.user_id, hook.label, hook.webhook)

    SQL(insertSQL[Hook])
      .on(
        'hook_id -> createdHook.hook_id,
        'user_id -> createdHook.user_id,
        'label -> createdHook.label,
        'webhook -> createdHook.webhook
      )
      .execute()

    createdHook
  })

  def patch(hook_id: HookId, hook: HookInput): Future[Hook] = Future(db.withConnection { implicit connection =>
    SQL(updateSQL[Hook](ignored = List("user_id")) + " label = {label}, webhook = {webhook}")
      .on(
        'label -> hook.label,
        'webhook -> hook.webhook
      )
      .execute()

    Hook(hook_id, hook.user_id, hook.label, hook.webhook)
  })

  def getHooksByUserId(user_id: UserId): Future[List[Hook]] = Future(db.withConnection { implicit connection =>
    SQL(selectSQL[Hook] + " WHERE user_id = {user_id}:uuid")
      .on('user_id -> user_id)
      .as(parser[Hook]().*)
  })

  def delete(hook_id: HookId): Future[Unit] = Future(db.withConnection { implicit connection =>
    SQL(deleteSQL[Hook] + "WHERE hook_id = {hook_id}::uuid")
      .on('hook_id -> hook_id)
      .execute()
  })
}
