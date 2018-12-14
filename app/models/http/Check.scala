package models.http

import models.entities.kind.Kind
import play.api.libs.json.Json

case class CheckCreationData(
  val path: Option[String],
  val secure: Option[Boolean],
  val name: Option[String],
  val kind: Kind
)

object CheckCreationInstances {
  implicit val checkCreationReads = Json.reads[CheckCreationData]
}