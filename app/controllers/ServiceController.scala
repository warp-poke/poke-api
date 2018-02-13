package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class ServiceController @Inject()(
  auth: Auth,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def listServices = auth.withUser(parse.anyContent) { user => request =>
      NotImplemented
    }

    type ServiceId = java.util.UUID
    case class ServiceCreationData(toto: String)
    implicit val scdFormats = Json.format[ServiceCreationData]

    def createService = auth.withUser(parse.json[ServiceCreationData]) { user => request =>
      NotImplemented
    }

    def updateService(id: ServiceId) =
      auth.withUser(parse.json[ServiceCreationData])
      { user => request =>
      NotImplemented
    }

    def getService(id: ServiceId) = auth.withUser(parse.anyContent) { user => request =>
      NotImplemented
    }

    def deleteService(id: ServiceId) = auth.withUser(parse.anyContent) { user => request =>
      NotImplemented
    }
}