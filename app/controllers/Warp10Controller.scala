package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class Warp10Controller @Inject()(
  warp10: models.Warp10,
  cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {
    def writeToken = Action {
      val token = warp10.deliverWriteToken()
      Ok(Json.obj("token" -> token))
    }
}
