package global

import actors._
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.AbstractModule
import javax.inject._
import models.Scheduler.{DNSTick, HTTPTick, ICMPTick, SSLTick}
import models.repositories.ServiceRepository
import models.{Scheduler, ServicesAgent}
import play.api._
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Global @Inject() (
  app: Application,
  actorSystem: ActorSystem,
  conf: Configuration,
  @Named("services-state") servicesState: ActorRef,
  @Named("scheduler") scheduler: ActorRef,
  serviceRepo: ServiceRepository
  )(implicit ec: ExecutionContext) {

  val httpInterval = conf.get[Int]("kafka.httpchecks.interval")
  val sslInterval = conf.get[Int]("kafka.sslchecks.interval")
  val dnsInterval = conf.get[Int]("kafka.dnschecks.interval")
  val icmpInterval = conf.get[Int]("kafka.icmpchecks.interval")

  Logger.info("ON START")
  actorSystem.scheduler.schedule(1.seconds, 30.minutes) {
    serviceRepo.listAll map { services => servicesState ! ServicesAgent.SetAllServices(services) }
  }

  if(conf.get[Boolean]("scheduler.enabled")) {
    Logger.info("Scheduler is enabled")
    actorSystem.scheduler.schedule(10.seconds, httpInterval.seconds) {
      scheduler ! HTTPTick
    }
    actorSystem.scheduler.schedule(10.seconds, dnsInterval.seconds) {
      scheduler ! DNSTick
    }
    actorSystem.scheduler.schedule(10.seconds, sslInterval.seconds) {
      scheduler ! SSLTick
    }
    actorSystem.scheduler.schedule(10.seconds, icmpInterval.seconds) {
      scheduler ! ICMPTick
    }
  } else Logger.info("Scheduler is disabled")
}

class GlobalModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {
    bindActor[ServicesAgent]("services-state")
    bindActor[Scheduler]("scheduler")
    bindActor[SchedulingActor]("scheduling-actor")
    bindActor[KafkaProducerActor]("kafka-producer")
    bind(classOf[Global]).asEagerSingleton
  }
}
