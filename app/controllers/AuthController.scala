package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.mvc.Results._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

case class AuthData(
  userId: java.util.UUID
)

object Auth {
  def extractUser(rh: RequestHeader): Option[AuthData] = {
    Some(AuthData(java.util.UUID.fromString("245e7009-05b5-4fb4-b15c-ed41ff123443")))
  }

  def withUser[A](
    f: => AuthData => Request[A] => Result,
    parser: BodyParser[A] 
  ) = {
   Authenticated(extractUser, _ => Unauthorized) { user =>
     Action(parser)(request => f(user)(request))
   }
  }
}

@Singleton
class AuthController @Inject()(
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

    case class LoginData(email: String, password: String)
    implicit val ldR = Json.reads[LoginData]
    def login = Action(parse.json[LoginData]) { request =>
      NotImplemented
    }
}