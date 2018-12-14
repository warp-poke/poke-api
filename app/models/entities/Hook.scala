package models.entities

import java.util.UUID

import anorm.SqlParser._
import anorm._
import models.entities.hook_kind.SLACK_WEBHOOK
import models.enums._
import pgentity.pg_entity._
import play.api.libs.json._

object hook_kind {
  sealed trait HookKind {
    def name = this.toString.toUpperCase
  }
  case object SLACK_WEBHOOK extends HookKind

  object HookKind {
    val values = List(
      SLACK_WEBHOOK
    )
    def apply(s: String) = values.find(_.name == s.toUpperCase)
  }

  implicit val hookKindEnum = new EnumAdt[HookKind] {
    val values = HookKind.values
    def valueAsString(x: HookKind) = x.name
  }
  implicit val hookKindReads  = jsonReads[HookKind]
  implicit val hookKindWrites = jsonWrites[HookKind]

  implicit val hookKindToStatement = toStatement[HookKind]
  implicit val hookKindFromColumn = fromColumn[HookKind]
}

case class HookInput(
  user_id: User.UserId,
  label: String,
  kind: hook_kind.HookKind,
  webhook: String,
  template: String
)

case class Hook(
  hook_id: Hook.HookId,
  user_id: User.UserId,
  label: String,
  kind: hook_kind.HookKind,
  webhook: String,
) {
  import hook_kind._

  val template = kind match {
    case SLACK_WEBHOOK => """{"text": "$$BODY$$"}"""
    case _ => "Error: this kind is not defined."
  }
}

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
      PgField("hook_id", Some("uuid"), true),
      PgField("user_id"),
      PgField("label"),
      PgField("kind"),
      PgField("webhook")
    )
    def parser(prefix: String): RowParser[Hook] = {
      get[UUID](prefix + "hook_id") ~
      get[UUID](prefix + "user_id") ~
      get[String](prefix + "label") ~
      get[hook_kind.HookKind](prefix + "kind") ~
      get[String](prefix + "webhook") ~
      get[String](prefix + "template") map {
        case hook_id ~ user_id ~ label ~ kind ~ webhook ~ _ => {
          Hook(hook_id, user_id, label, kind, webhook)
        }
      }
    }
  }
}
