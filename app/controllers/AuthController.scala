package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.mvc.Results._
import play.api.libs.json._

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import models.MacaroonManager
import models.entities.{User,UserInstances}
import models.repositories.UserRepository

case class AuthData(
  userId: java.util.UUID
)

case class AuthRequest[A](
  auth: AuthData,
  request: Request[A]
) extends WrappedRequest(request)


class Authenticated @Inject() (
    cc: ControllerComponents,
    macaroons: MacaroonManager
  ) extends ActionBuilder[AuthRequest,AnyContent] with ActionRefiner[Request, WrappedRequest] {
  private def extractMacaroon(request: Request[Any]): Option[AuthData] = for {
    authHeader <- request.headers.get("Authorization")
    if authHeader.startsWith("Bearer ")
    macaroon = authHeader.drop(7)
    userId <- macaroons.checkMacaroon(macaroon)
  } yield AuthData(userId = userId)
  def executionContext: ExecutionContext = cc.executionContext
  def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  implicit val ec = executionContext
  def invokeBlock[A](r: Request[A], block: AuthRequest[A] => Future[Result]) = {
    refine(r).flatMap({
      case Left(r) => Future.successful(r)
      case Right(wr) => block(wr)
    })
  }
  def refine[A](request: Request[A]): Future[Either[Result,AuthRequest[A]]] = {
    val result = extractMacaroon(request)

    result
      .map(data => Future.successful(Right(AuthRequest(data, request))))
      .getOrElse(Future.successful(Left(Unauthorized)))
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
            if(User.verify(password, u.hashed_password)) {
              Created(Json.obj("token" -> macaroons.deliverRootMacaroon(u.user_id)))
            } else {
              Unauthorized
            }
          }
          case None => Unauthorized
        }
      })
    }

    def register = Action.async(parse.json[LoginData]) { request =>
      val user = User(
        user_id = UUID.randomUUID,
        email = request.body.email,
        hashed_password = User.hash(request.body.password)
      )

      userRepo.createUser(user).map(_ => {
        Created
      })
    }
}