package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import akka.actor.ActorRef

import models.entities.Service.ServiceId
import models.entities.ServiceInstances.completeServiceWrites

import models.http.PaginatedService
import models.http.PaginatedServiceInstances._
import models.http.ServiceCreationData
import models.http.ServiceCreationDataInstances._

import models.repositories.ServiceRepository
import models.StateAgent._

@Singleton
class ServiceController @Inject()(
  authed: Authenticated,
  serviceRepo: ServiceRepository,
  cc: ControllerComponents,
  @Named("globalstate") globalState: ActorRef
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def listServices = authed.async { r =>
      serviceRepo
        .list(r.auth.userId)
        .map(cs => {
          val paginated = PaginatedService(cs)
          Ok(Json.toJson(paginated))
        })
    }

    def createService = authed.async(parse.json[ServiceCreationData]) { implicit request =>
      serviceRepo
        .create(request.auth.userId, request.body)
        .map({ cs =>
          globalState ! AddService(cs)
          Created(Json.toJson(cs))
        })
    }

    def updateService(id: ServiceId) = authed.async(parse.json[ServiceCreationData]) { request =>
      serviceRepo
        .update(request.auth.userId, id, request.body)
        .map({ cs => Ok(Json.toJson(cs)) })
    }

    def getService(id: ServiceId) = authed.async { request =>
      serviceRepo
        .get(id, request.auth.userId)
        .map({
          case Some(cs) => Ok(Json.toJson(cs))
          case None => NotFound
        })
    }

    def deleteService(id: ServiceId) = authed.async { request =>
      serviceRepo
        .delete(id, request.auth.userId)
        .map(_ => {
          globalState ! RemoveService(id)
          NoContent
        })
    }
}