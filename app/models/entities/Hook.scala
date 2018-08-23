package models.entities

import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import pgentity.pg_entity._

case class HookInput(
  user_id: User.UserId,
  label: String,
  webhook: String
)

case class Hook(
  hook_id: Hook.HookId,
  user_id: User.UserId,
  label: String,
  webhook: String
)

object Hook {
  type HookId = UUID
}

object HookInstances {
  implicit val hookInputReads = Json.reads[HookInput]
  implicit val hookReads = Json.reads[Hook]
  implicit val hookWrites = Json.writes[Hook]
  implicit val hookEntity = new PgEntity[Hook] {
    val tableName = "hook"
    val columns = List(
      PgField("hook_id", Some("uuid"), true), PgField("user_id"), PgField("label"), PgField("webhook")
    )
    def parser(prefix: String): RowParser[Hook] = {
      get[UUID](prefix + "hook_id") ~
      get[UUID](prefix + "user_id") ~
      get[String](prefix + "label") ~
      get[String](prefix + "webhook") map {
        case hook_id ~ user_id ~ label ~ webhook => {
          Hook(hook_id, user_id, label, webhook)
        }
      }
    }
  }
}
