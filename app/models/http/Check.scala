package models.http

import play.api.libs.json.Json

case class CheckCreationData(
  val path: String,
  val secure: Boolean,
  val kind: String,
  val name: Option[String]
)

object CheckCreationInstances {
  implicit val checkCreationReads = Json.reads[CheckCreationData]
}