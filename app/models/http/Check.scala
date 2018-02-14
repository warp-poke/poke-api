package models.http

import play.api.libs.json.Json

case class CheckCreationData(
  val path: String
)

object CheckCreationInstances {
  implicit val checkCreationReads = Json.reads[CheckCreationData]
}