package controllers

import javax.inject.Inject

import model.Db
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller, Security}

/**
  * Created by henri on 11/19/2016.
  */
class Auth @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def login = Action { implicit request =>
    Ok(views.html.login.login())
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login.login(formWithErrors)),
      user => Redirect(routes.Application.search()).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.login()).withNewSession.flashing("success" -> Messages("login.logout"))
  }

  val existingUserIdConstraint: Constraint[String] = Constraint("constraints.validUserId")({
    userId =>
      if (Db.fetchUserById(userId).isDefined)
        Valid
      else
        Invalid(ValidationError("error.existingUserId"))
  })

  private val existingUserId = nonEmptyText().verifying(existingUserIdConstraint)

  //noinspection MatchToPartialFunction
  private val validPasswordConstraint = Constraint[(String, String)]("constraints.validPassword")({
    res =>
      res match {
        case (userId, pw) =>
          if (check(userId, pw)) Valid
          else Invalid(ValidationError("error.validPassword"))
      }
  })

  val loginForm = Form {
    tuple(
      "userId" -> existingUserId,
      "pw" -> nonEmptyText
    ) verifying validPasswordConstraint
  }

  def check(userId: String, pw: String): Boolean = {
    Db.fetchUserById(userId).get.pw == pw
  }
}
