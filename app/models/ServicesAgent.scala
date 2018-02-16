package models

import javax.inject._
import akka.actor._
import models.entities.CompleteService
import models.entities.Service.ServiceId

class ServicesAgent @Inject() () extends Actor {
  import ServicesAgent._

  def receive = active(Map.empty)

  def active(services: Map[ServiceId, CompleteService]): Receive = {
    case GetAllServices => {
      sender ! AllServices(services)
    }
    case SetAllServices(list) => {
      context.become(active(list.iterator.map(cs => cs.service.service_id -> cs).toMap))
    }
    case AddService(cs) => {
      context.become(active(services + (cs.service.service_id -> cs)))
    }
    case UpdateService(cs) => {
      context.become(active(services.updated(cs.service.service_id, cs)))
    }
    case RemoveService(id) => {
      context.become(active(services - id))
    }
  }
}

object ServicesAgent {
  sealed trait ServicesAgentMessage
  case object   GetAllServices                                                extends ServicesAgentMessage
  case class    SetAllServices(services: Seq[CompleteService])                extends ServicesAgentMessage
  case class    AllServices(services: Map[ServiceId, CompleteService])        extends ServicesAgentMessage
  case class    AddService(service: CompleteService)                          extends ServicesAgentMessage
  case class    UpdateService(service: CompleteService)                       extends ServicesAgentMessage
  case class    RemoveService(serviceId: ServiceId)                           extends ServicesAgentMessage
}