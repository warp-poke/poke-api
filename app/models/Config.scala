package models

import java.util.UUID
import javax.inject._
import play.api.Configuration

case class Warp10Config(
  endpoint: String,
  owner_id: UUID,
  app_name: String,
  sip_hash_app: String,
  sip_hash_token: String,
  aes_token: String
)

@Singleton
class Config @Inject() (val configuration: Configuration) {

  val warp10 = Warp10Config(
    endpoint = getString("warp10.endpoint"),
    owner_id = getUuid("warp10.owner_id"),
    app_name = getString("warp10.app_name"),
    sip_hash_app = getString("warp10.sip_hash_app"),
    sip_hash_token = getString("warp10.sip_hash_token"),
    aes_token = getString("warp10.aes_token")
  )

  val temporaryTokensDeleteMe =
    (getString("warp10.temporary.read"), getString("warp10.temporary.write"))

  val macaroonsSecret = getString("macaroons.secret")

  private def getUuid(path: String): UUID = {
    val value = getString(path)
    UUID.fromString(value)
  }

  private def getString(path: String): String = {
    configuration.get[String](path)
  }
}
