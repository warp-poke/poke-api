package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.mvc.Results._
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import models.MacaroonManager
import models.entities.{User,UserInstances}
import models.repositories.UserRepository

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
  userRepo: UserRepository,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    case class LoginData(email: String, password: String)
    implicit val ldR = Json.reads[LoginData]
    
    def login = Action.async(parse.json[LoginData]) { request =>

      val LoginData(email, password) = request.body
      val result = userRepo.getUserByEmail(email)

      result.map(oUser => {
        oUser match {
          case Some(u) => {
            if(UserInstances.verify(password, u.hashed_password)) {
              Created(Json.obj("token" -> macaroons.deliverRootMacaroon(u.userId)))
            } else {
              Unauthorized
            }
          }
          case None => Unauthorized
        }
      })
    }
}