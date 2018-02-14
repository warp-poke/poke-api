package global

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import javax.inject.Inject
import javax.inject.Singleton
import play.api._
import play.api.libs.concurrent.Akka
import play.api.Configuration
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import models.Scheduler
import models.AmqpClient

@Singleton
class Global @Inject() (
  app: Application,
  actorSystem: ActorSystem,
  scheduler: Scheduler,
  conf: Configuration
  )(implicit ec: ExecutionContext) {

  Logger.info("ON START")
  if(conf.get[Boolean]("scheduler.enabled")) {
    Logger.info("Scheduler is enabled")
    actorSystem.scheduler.schedule(10.seconds, 1.minutes) {
      scheduler.httpTick()
    }
  } else Logger.info("Scheduler is disabled")
}

class GlobalModule extends AbstractModule {
  def configure() = {
    bind(classOf[Global]).asEagerSingleton
  }
}
