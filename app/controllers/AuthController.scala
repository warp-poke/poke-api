package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.mvc.Results._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

import models.MacaroonManager

case class AuthData(
  userId: java.util.UUID
)

class Auth @Inject() (
  macaroons: MacaroonManager
) {
  def extractUser(rh: RequestHeader): Option[AuthData] = {
    for {
      authHeader <- rh.headers.get("Authorization")
      if authHeader.startsWith("Bearer ")
      macaroon = authHeader.drop(7)
      userId <- macaroons.checkMacaroon(macaroon)
    } yield AuthData(userId = userId)
  }

  def withUser[A](parser: BodyParser[A])(
    f: => AuthData => Request[A] => Result
  ) = {
   Authenticated(extractUser, _ => Unauthorized) { user =>
     Action(parser)(request => f(user)(request))
   }
  }
}

@Singleton
class AuthController @Inject()(
  macaroons: MacaroonManager,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    case class LoginData(email: String, password: String)
    implicit val ldR = Json.reads[LoginData]
    def login = Action(parse.json[LoginData]) { request =>
      // TODO changeme
      val userId = java.util.UUID.fromString("245e7009-05b5-4fb4-b15c-ed41ff123443")
      val token = macaroons.deliverRootMacaroon(userId)
      Ok(Json.obj("token" -> token))
    }
}