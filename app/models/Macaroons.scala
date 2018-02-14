package models

import javax.inject._
import scala.util.Try
import java.util.UUID

import com.github.nitram509.jmacaroons.{MacaroonsBuilder, MacaroonsVerifier}

import models.entities.User.UserId


class MacaroonManager @Inject() (
  config: Config
) {
  def deliverRootMacaroon(userId: UserId): String = {
    MacaroonsBuilder.create(
      "https://example.org",
      config.macaroonsSecret,
      userId.toString
    ).serialize
  }

  def checkMacaroon(serializedMacaroon: String): Option[UserId] = {
    val macaroon = MacaroonsBuilder.deserialize(serializedMacaroon)
    val verifier = new MacaroonsVerifier(macaroon)

    Some(macaroon.identifier)
      .filter(_ => verifier.isValid(config.macaroonsSecret))
      .flatMap(id => Try(UUID.fromString(id)).toOption)

  }
}