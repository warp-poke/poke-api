package global

import akka.actor.{ActorSystem, ActorRef}
import com.google.inject.AbstractModule
import javax.inject._
import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.Configuration
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import models.Scheduler
import models.Scheduler.HttpTick
import models.ServicesAgent
import models.repositories.ServiceRepository

@Singleton
class Global @Inject() (
  app: Application,
  actorSystem: ActorSystem,
  conf: Configuration,
  @Named("services-state") servicesState: ActorRef,
  @Named("scheduler") scheduler: ActorRef,
  serviceRepo: ServiceRepository
  )(implicit ec: ExecutionContext) {

  Logger.info("ON START")
  actorSystem.scheduler.schedule(1.seconds, 30.minutes) {
    serviceRepo.listAll map { services => servicesState ! ServicesAgent.SetAllServices(services) }
  }
  if(conf.get[Boolean]("scheduler.enabled")) {
    Logger.info("Scheduler is enabled")
    actorSystem.scheduler.schedule(10.seconds, 1.minutes) {
      scheduler ! HttpTick
    }
  } else Logger.info("Scheduler is disabled")
}

class GlobalModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {
    bindActor[ServicesAgent]("services-state")
    bindActor[Scheduler]("scheduler")
    bind(classOf[Global]).asEagerSingleton
  }
}
