package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import models.Warp10

import models.entities.Service.ServiceId
import models.entities.ServiceInstances.completeServiceWrites

import models.http.PaginatedService
import models.http.PaginatedServiceInstances._
import models.http.ServiceCreationData
import models.http.ServiceCreationDataInstances._

import models.repositories.ServiceRepository

@Singleton
class TokenController @Inject()(
  authed: Authenticated,
  warp10: Warp10,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def getReadToken = authed { r =>
      val token = warp10.deliverReadToken(r.auth.userId)
      Ok(Json.obj(
        "token" -> token
      ))
    }
}