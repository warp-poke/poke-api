package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class ServiceController @Inject()(
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def listServices = Action { request =>
      NotImplemented
    }

    type ServiceId = java.util.UUID
    case class ServiceCreationData(toto: String)
    implicit val scdFormats = Json.format[ServiceCreationData]

    def createService = Action(parse.json[ServiceCreationData]) { request =>
      NotImplemented
    }

    def updateService(id: ServiceId) = Action(parse.json[ServiceCreationData]) { request =>
      NotImplemented
    }

    def getService(id: ServiceId) = Action { request =>
      NotImplemented
    }

    def deleteService(id: ServiceId) = Action { request =>
      NotImplemented
    }
}