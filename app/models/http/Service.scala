package models.http

import play.api.libs.json.Json

case class ServiceCreationData(
  val domain: String,
  val name: Option[String],
  val checks: Array[CheckCreationData]
)

object ServiceCreationDataInstances {
  implicit val checkCreationReads = Json.reads[CheckCreationData]
  implicit val serviceCreationReads = Json.reads[ServiceCreationData]
}