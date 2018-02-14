package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import models.entities.Service.ServiceId
import models.entities.ServiceInstances.completeServiceWrites

import models.http.ServiceCreationData
import models.http.ServiceCreationDataInstances._

import models.repositories.ServiceRepository

@Singleton
class ServiceController @Inject()(
  authed: Authenticated,
  serviceRepo: ServiceRepository,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def listServices = authed.async { r =>
      serviceRepo
        .list(r.auth.userId)
        .map(x => Ok(r.auth.toString))
    }

    def createService = authed.async(parse.json[ServiceCreationData]) { implicit request =>
      serviceRepo
        .create(request.auth.userId, request.body)
        .map({ cs => Created(Json.toJson(cs)) })
    }

    def updateService(id: ServiceId) = authed.async(parse.json[ServiceCreationData]) { request =>
      Future.successful(NotImplemented)
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
        .map(_ => NoContent)
    }
}