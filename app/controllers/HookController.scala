package controllers

import java.util.UUID
import javax.inject._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models.entities.Hook
import models.entities.HookInput
import models.entities.Hook.HookId
import models.entities.HookInstances._
import models.repositories.HookRepository
import models.entities.User.UserId

@Singleton
class HookController @Inject()(
  authed: Authenticated,
  cc: ControllerComponents,
  hookRepository: HookRepository,
  internalAuthed: InternalAuthenticated
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def createHook = authed.async(parse.json[HookInput]) { implicit r =>
    hookRepository.create(r.body)
      .map(hook => Created(Json.toJson(hook)))
  }

  def updateHook(hook_id: HookId) = authed.async(parse.json[HookInput]) { implicit r =>
    hookRepository.patch(hook_id, r.body)
      .map(hook => Ok(Json.toJson(hook)))
  }

  def deleteHook(hook_id: HookId) = authed.async { implicit r =>
    hookRepository.delete(hook_id)
      .map(_ => NoContent)
  }

  def listHooksInternalAuth(user_id: UserId) = internalAuthed.async { implicit r =>
    hookRepository.getHooksByUserId(user_id)
      .map(hooks => Ok(Json.toJson(hooks)))
  }

  def listHooks(user_id: UserId) = authed.async { implicit r =>
    hookRepository.getHooksByUserId(user_id)
      .map(hooks => Ok(Json.toJson(hooks)))
  }
}
