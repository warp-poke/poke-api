package models

import java.util.UUID
import java.nio.{ByteBuffer,ByteOrder}
import io.warp10.crypto._
import io.warp10.quasar.encoder.QuasarTokenEncoder
import collection.JavaConverters._
import io.warp10.quasar.token.thrift.data.ReadToken;
import io.warp10.quasar.token.thrift.data.TokenType;

import javax.inject.Inject

import scala.concurrent.duration._

import models.entities.User.UserId

class Warp10 @Inject() (config: Config) {
  val producerId = config.warp10.producer_id
  val application = config.warp10.app_name

  private val keyStore: KeyStore = new DummyKeyStore()
  keyStore.setKey(KeyStore.SIPHASH_APPID, keyStore.decodeKey(config.warp10.sip_hash_app))
  keyStore.setKey(KeyStore.SIPHASH_TOKEN, keyStore.decodeKey(config.warp10.sip_hash_token))
  keyStore.setKey(KeyStore.AES_TOKEN,     keyStore.decodeKey(config.warp10.aes_token))

  val qte = new QuasarTokenEncoder()

  def deliverReadToken(userId: UserId) = {
    qte.deliverReadToken(
      application,
      userId.toString,
      producerId.toString,
      List(application).asJava,
      48.hours.toMillis,
      keyStore
    )
  }

  def deliverWriteToken(labels: Map[String,String], userId: UserId) = {
    qte.deliverWriteToken(
      application,
      userId.toString,
      producerId.toString,
      labels.asJava,
      5.minutes.toMillis,
      keyStore
    )
  }

    def deliverInternalReadToken(duration: Duration) = {
    qte.deliverReadToken(
      application,
      producerId.toString,
      List[String]().asJava,
      List(application).asJava,
      Map[String, String]().asJava,
      duration.toMillis,
      keyStore
    )
  }
}
