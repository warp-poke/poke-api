package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import models.repositories.ServiceRepository

@Singleton
class ServiceController @Inject()(
  authed: Authenticated,
  serviceRepo: ServiceRepository,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def listServices = authed.async { r =>
      serviceRepo.listAll.map(x => Ok(r.auth.toString))
    }

    type ServiceId = java.util.UUID
    case class ServiceCreationData(toto: String)
    implicit val scdFormats = Json.format[ServiceCreationData]

    def createService = authed.async(parse.json[ServiceCreationData]) { implicit request =>
      println(request.body.toString)
      Future.successful(NotImplemented)
    }

    def updateService(id: ServiceId) = authed(parse.json[ServiceCreationData]) { request =>
      NotImplemented
    }

    def getService(id: ServiceId) = authed { request =>
      NotImplemented
    }

    def deleteService(id: ServiceId) = authed { request =>
      NotImplemented
    }
}