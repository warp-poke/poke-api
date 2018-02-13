package models

import javax.inject._
import java.util.UUID
import scala.util.Try

import com.github.nitram509.jmacaroons.{MacaroonsBuilder, MacaroonsVerifier}


class MacaroonManager @Inject() (
  config: Config
) {
  def deliverRootMacaroon(userId: UUID): String = {
    MacaroonsBuilder.create(
      "https://example.org",
      config.macaroonsSecret,
      userId.toString
    ).serialize
  }

  def checkMacaroon(serializedMacaroon: String): Option[UUID] = {
    val macaroon = MacaroonsBuilder.deserialize(serializedMacaroon)
    val verifier = new MacaroonsVerifier(macaroon)

    Some(macaroon.identifier)
      .filter(_ => verifier.isValid(config.macaroonsSecret))
      .flatMap(id => Try(UUID.fromString(id)).toOption)

  }
}