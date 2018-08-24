package models

import javax.inject._
import akka.actor._
import models.Scheduler.Tick
import models.entities.CompleteService
import models.entities.Service.ServiceId
import models.entities.Shard

class StateAgent @Inject() () extends Actor {
  import StateAgent._

  def receive = active(State.empty)

  def active(state: State): Receive = {
    case GetAllServices(tick) => {
      sender ! AllServices(state.services, tick)
    }
    case SetAllServices(list) => {
      val filtered = list.filter(cs => state.shards.foldLeft(state.shards.isEmpty)((acc, shard) => acc || shard.contains(cs.service.service_id)))
      context.become(active(state.copy(services = filtered.iterator.map(cs => cs.service.service_id -> cs).toMap)))
    }
    case AddService(cs) => {
      context.become(active(state.copy(services = state.services + (cs.service.service_id -> cs))))
    }
    case UpdateService(cs) => {
      context.become(active(state.copy(services = state.services.updated(cs.service.service_id, cs))))
    }
    case RemoveService(id) => {
      context.become(active(state.copy(services = state.services - id)))
    }

    case GetShards => {
      sender ! Shards(state.shards)
    }
    case SetShards(shards) => {
      context.become(active(state.copy(shards = shards)))
    }
    case AddShard(shard) => {
      context.become(active(state.copy(shards = shard :: state.shards)))
    }
  }
}

object StateAgent {
  sealed trait StateAgentMessage
  case class    GetAllServices(tick: Tick)                                          extends StateAgentMessage
  case class    SetAllServices(services: Seq[CompleteService])                      extends StateAgentMessage
  case class    AllServices(services: Map[ServiceId, CompleteService], tick: Tick)  extends StateAgentMessage
  case class    FilteredServices(services: Map[ServiceId, CompleteService])         extends StateAgentMessage
  case class    AddService(service: CompleteService)                                extends StateAgentMessage
  case class    UpdateService(service: CompleteService)                             extends StateAgentMessage
  case class    RemoveService(serviceId: ServiceId)                                 extends StateAgentMessage
  case object   GetShards                                                           extends StateAgentMessage
  case class    SetShards(shards: List[Shard])                                      extends StateAgentMessage
  case class    Shards(shards: List[Shard])                                         extends StateAgentMessage
  case class    AddShard(shard: Shard)                                              extends StateAgentMessage
}

case class State(
  services: Map[ServiceId, CompleteService],
  shards: List[Shard]
)
object State {
  def empty = State(Map.empty, List())
}